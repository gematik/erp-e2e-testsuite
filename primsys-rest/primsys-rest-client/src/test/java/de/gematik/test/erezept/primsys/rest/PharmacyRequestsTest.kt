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
                PharmacyRequests.reply(
                    "160.100.000.000.011.10",
                    "X110407073",
                    "just some random body (which is currently not valid!!)"
                )
            ).asExpectedPayload()
        }
    }
}