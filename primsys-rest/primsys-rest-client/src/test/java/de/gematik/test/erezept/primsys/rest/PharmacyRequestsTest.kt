/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import de.gematik.test.erezept.primsys.PrimSysClientFactory
import de.gematik.test.erezept.primsys.RestTest
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto
import de.gematik.test.erezept.primsys.data.PznMedicationDto
import de.gematik.test.erezept.primsys.data.valuesets.SupplyFormDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

@WireMockTest(httpPort = RestTest.REST_PORT)
class PharmacyRequestsTest : RestTest() {

    private fun setupPositiveAccept(): StubMapping {
        val acceptResponseDto = AcceptedPrescriptionDto()
        acceptResponseDto.accessCode = "{{request.query.ac}}"
        acceptResponseDto.prescriptionId = "{{request.query.taskId}}"

        val response = om.writeValueAsString(acceptResponseDto)

        return stubFor(
            post(urlMatching("/pharm/([a-zA-Z0-9]*)/accept\\?taskId=(.*)&ac=(.*)"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response).withTransformers("response-template")
                )
        )
    }

    private fun setupPositiveDispense(): StubMapping {
        val dispenseResponseDto = DispensedMedicationDto()
        dispenseResponseDto.secret = "{{request.query.secret}}"
        dispenseResponseDto.prescriptionId = "{{request.query.taskId}}"

        val response = om.writeValueAsString(dispenseResponseDto)

        return stubFor(
            post(urlMatching("/pharm/([a-zA-Z0-9]*)/close\\?taskId=(.*)&secret=(.*)"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response).withTransformers("response-template")
                )
        )
    }

    private fun setupPositiveReject(): StubMapping {
        return stubFor(
            post(urlMatching("/pharm/([a-zA-Z0-9]*)/reject\\?taskId=(.*)&ac=(.*)&secret=(.*)"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")
                )
        )
    }

    private fun setupPositiveReply(): StubMapping {
        return stubFor(
            post(urlMatching("/pharm/([a-zA-Z0-9]*)/reply\\?taskId=(.*)&kvnr=(.*)"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")
                )
        )
    }

    private fun setupPositiveCommunicationSearch(): StubMapping {
        return stubFor(
            get(urlPathMatching("/pharm/([a-zA-Z0-9]*)/communications"))
                .withQueryParam("sender", or(absent(), matching("[A-Z][0-9]{9}")))
                .withQueryParam("receiver", or(absent(), matching("[A-Z][0-9]{9}")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                )
        )
    }

    private fun setupPositiveCommunicationDelete(): StubMapping {
        return stubFor(
            delete(urlPathMatching("/pharm/([a-zA-Z0-9]*)/communication/([a-zA-Z0-9]*)"))
                .willReturn(
                    aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                )
        )
    }

    @Test
    fun shouldAccept() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveAccept()
        val pharm = clientFactory.getRandomPharmacyClient()
        val acceptResponse = assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyRequests.accept("160.100.000.000.011.10", "accessCode")
            ).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", acceptResponse.prescriptionId)
    }

    @Test
    fun shouldDispensePrescribed() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispense()
        val pharm = clientFactory.getRandomPharmacyClient()
        val dispenseResponse = assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyRequests.dispense("160.100.000.000.011.10", "secret")
            ).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", dispenseResponse.prescriptionId)
    }

    @Test
    fun shouldDispenseNegativeAmount() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispense()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertThrows<AssertionError> {
            pharm.performBlocking(
                PharmacyRequests.dispense("160.100.000.000.011.10", "secret", -1)
            ).asExpectedPayload()
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 3, 10])
    fun shouldDispenseNRandom(num: Int) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispense()
        val pharm = clientFactory.getRandomPharmacyClient()
        val dispenseResponse = assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyRequests.dispense("160.100.000.000.011.10", "secret", num)
            ).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", dispenseResponse.prescriptionId)
    }

    @Test
    fun shouldDispenseCustom() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispense()
        val pharm = clientFactory.getRandomPharmacyClient()
        val dispenseResponse = assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyRequests.dispense(
                    "160.100.000.000.011.10",
                    "secret",
                    PznDispensedMedicationDto.dispensed(
                        PznMedicationDto.medicine("13946948", "Hysan Pflegespray 20 ml")
                            .supplyForm(SupplyFormDto.NAS).asPrescribed()
                    ).withBatchInfo("123456", Date())
                )
            ).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", dispenseResponse.prescriptionId)
    }

    @Test
    fun shouldReject() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveReject()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyRequests.reject("160.100.000.000.011.10", "accessCode", "secret")
            ).asExpectedPayload()
        }
    }

    @Test
    fun shouldSendReply() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveReply()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyCommunicationRequests.reply(
                    "160.100.000.000.011.10",
                    "X110407073",
                    "just some random body (which is currently not valid!!)"
                )
            ).asExpectedPayload()
        }
    }

    @ParameterizedTest
    @MethodSource("shouldSearchCommunications")
    fun shouldSearchCommunications(sender: String?, receiver: String?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveCommunicationSearch()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyCommunicationRequests.search(sender, receiver)
            ).asExpectedPayload()
        }
    }

    @Test
    fun shouldSearchCommunicationsWithoutFilters() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveCommunicationSearch()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyCommunicationRequests.search()
            ).asExpectedPayload()
        }
    }

    @Test
    fun shouldDeleteCommunication() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveCommunicationDelete()
        val pharm = clientFactory.getRandomPharmacyClient()
        assertDoesNotThrow {
            pharm.performBlocking(
                PharmacyCommunicationRequests.delete("123123")
            ).asExpectedPayload()
        }
    }

    companion object TestDataProvider {
        @JvmStatic
        fun shouldSearchCommunications() = listOf(
            arrayOf("X110407073", "X110407073"),
            arrayOf("X110407073", null),
            arrayOf(null, "X110407073"),
            arrayOf<String?>(null, null)
        )
    }
}