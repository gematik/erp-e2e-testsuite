/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import de.gematik.test.erezept.primsys.PrimSysClientFactory
import de.gematik.test.erezept.primsys.RestTest
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto
import de.gematik.test.erezept.primsys.data.PatientDto
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.io.path.Path

@WireMockTest(httpPort = RestTest.REST_PORT)
class DoctorRequestTest : RestTest() {

    private fun setupPositivePrescribe(kvnr: String): StubMapping {
        val prescriptionDto = PrescriptionDto()
        prescriptionDto.prescriptionId = "160.100.000.000.011.09"
        prescriptionDto.patient = PatientDto.withKvnr(kvnr).build()

        val prescription = om.writeValueAsString(prescriptionDto)

        return stubFor(
            post(urlPathTemplate("/doc/{docId}/prescribe"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescription)
                )
        )
    }

    private fun setupPositivePrescribeKbvBundle(): StubMapping {
        val prescriptionDto = PrescriptionDto()
        prescriptionDto.prescriptionId = "160.100.000.000.011.09"
        prescriptionDto.patient = PatientDto.withKvnr("X123123123").build()

        val prescription = om.writeValueAsString(prescriptionDto)

        return stubFor(
            post(urlPathTemplate("/doc/{docId}/xml/prescribe"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(prescription)
                )
        )
    }

    private fun setupPositiveReadyPrescriptionById(): StubMapping {
        val prescriptionDto = PrescriptionDto()
        prescriptionDto.prescriptionId = "{{request.path.[2]}}"
        prescriptionDto.patient = PatientDto.withKvnr("X123123123").build()

        val prescription = om.writeValueAsString(prescriptionDto)

        return stubFor(
            get(urlPathTemplate("/prescription/prescribed/{prescriptionId}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescription)
                        .withTransformers("response-template")
                )
        )
    }

    private fun setupPositiveAcceptedPrescriptionById(): StubMapping {
        val prescriptionDto = AcceptedPrescriptionDto()
        prescriptionDto.prescriptionId = "{{request.path.[2]}}"
        prescriptionDto.forKvnr = "X123123123"

        val prescription = om.writeValueAsString(prescriptionDto)

        return stubFor(
            get(urlPathTemplate("/prescription/accepted/{prescriptionId}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescription)
                        .withTransformers("response-template")
                )
        )
    }

    private fun setupPositiveDispensedPrescriptionById(): StubMapping {
        val prescriptionDto = DispensedMedicationDto()
        prescriptionDto.prescriptionId = "{{request.path.[2]}}"

        val prescription = om.writeValueAsString(prescriptionDto)

        return stubFor(
            get(urlPathTemplate("/prescription/dispensed/{prescriptionId}"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescription)
                        .withTransformers("response-template")
                )
        )
    }

    private fun setupPositiveReadyPrescriptions(prescriptionIds: List<String>): StubMapping {
        val prescriptionDtos = prescriptionIds.map {
            val prescriptionDto = PrescriptionDto()
            prescriptionDto.prescriptionId = it
            prescriptionDto.patient = PatientDto.withKvnr("X123123123").build()
            prescriptionDto
        }
        val prescriptions = om.writeValueAsString(prescriptionDtos)

        return stubFor(
            get(urlPathTemplate("/prescription/prescribed"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescriptions)
                )
        )
    }

    private fun setupPositiveAcceptedPrescriptions(prescriptionIds: List<String>): StubMapping {
        val prescriptionDtos = prescriptionIds.map {
            val prescriptionDto = AcceptedPrescriptionDto()
            prescriptionDto.prescriptionId = it
            prescriptionDto.forKvnr = "X123123123"
            prescriptionDto
        }
        val prescriptions = om.writeValueAsString(prescriptionDtos)

        return stubFor(
            get(urlPathTemplate("/prescription/accepted"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescriptions)
                )
        )
    }

    private fun setupPositiveDispensedPrescriptions(prescriptionIds: List<String>): StubMapping {
        val prescriptionDtos = prescriptionIds.map {
            val prescriptionDto = DispensedMedicationDto()
            prescriptionDto.prescriptionId = it
            prescriptionDto
        }
        val prescriptions = om.writeValueAsString(prescriptionDtos)

        return stubFor(
            get(urlPathTemplate("/prescription/dispensed"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prescriptions)
                )
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    @NullSource
    fun shouldPrescribe(asDirectAssignment: Boolean?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        val doc = clientFactory.getRandomDoctorClient()
        val kvnr = "X123123123"
        setupPositivePrescribe(kvnr)
        val cmd = asDirectAssignment?.let {
            DoctorRequests.prescribe(kvnr, it)
        } ?: DoctorRequests.prescribe(kvnr)
        val prescription = assertDoesNotThrow {
            doc.performBlocking(cmd).asExpectedPayload()
        }
        assertEquals(kvnr, prescription.patient.kvnr)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    @NullSource
    fun shouldPrescribeKbvBundle(asDirectAssignment: Boolean?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        val doc = clientFactory.getRandomDoctorClient()

        val kbvBundlePath =
            this.javaClass.getResource("/kbv/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml")?.let { Path(it.path) }
        setupPositivePrescribeKbvBundle()
        val cmd = asDirectAssignment?.let {
            DoctorRequests.prescribeKbvBundle(kbvBundlePath!!, it)
        } ?: DoctorRequests.prescribeKbvBundle(kbvBundlePath!!)
        assertDoesNotThrow {
            doc.performBlocking(cmd).asExpectedPayload()
        }
    }

    @Test
    fun shouldGetOpenPrescriptionsById() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveReadyPrescriptionById()

        val doc = clientFactory.getRandomDoctorClient()
        val prescription = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getReadyPrescription("160.100.000.000.011.10")).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", prescription.prescriptionId)
    }

    @ParameterizedTest
    @ValueSource(strings = ["X123123123"])
    @NullSource
    fun shouldGetAllOpenPrescriptions(kvnr: String?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveReadyPrescriptions(
            listOf(
                "160.100.000.000.011.11",
                "160.100.000.000.011.12",
                "160.100.000.000.011.13"
            )
        )

        val doc = clientFactory.getRandomDoctorClient()
        val prescriptions = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getReadyPrescriptions(kvnr)).asExpectedPayload()
        }
        assertEquals(3, prescriptions.size)
    }

    @Test
    fun shouldGetAcceptedPrescriptionsById() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveAcceptedPrescriptionById()

        val doc = clientFactory.getRandomDoctorClient()
        val prescription = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getAcceptedPrescription("160.100.000.000.011.10")).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", prescription.prescriptionId)
    }

    @ParameterizedTest
    @ValueSource(strings = ["X123123123"])
    @NullSource
    fun shouldGetAllAcceptedPrescriptions(kvnr: String?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveAcceptedPrescriptions(
            listOf(
                "160.100.000.000.011.11",
                "160.100.000.000.011.12",
                "160.100.000.000.011.13"
            )
        )

        val doc = clientFactory.getRandomDoctorClient()
        val prescriptions = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getAcceptedPrescriptions(kvnr)).asExpectedPayload()
        }
        assertEquals(3, prescriptions.size)
    }

    @Test
    fun shouldGetDispensedPrescriptionsById() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispensedPrescriptionById()

        val doc = clientFactory.getRandomDoctorClient()
        val prescription = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getDispensedPrescription("160.100.000.000.011.10")).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", prescription.prescriptionId)
    }

    @ParameterizedTest
    @ValueSource(strings = ["X123123123"])
    @NullSource
    fun shouldGetAllDispensedPrescriptions(kvnr: String?) {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositiveDispensedPrescriptions(
            listOf(
                "160.100.000.000.011.11",
                "160.100.000.000.011.12",
                "160.100.000.000.011.13"
            )
        )

        val doc = clientFactory.getRandomDoctorClient()
        val prescriptions = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getDispensedPrescriptions(kvnr)).asExpectedPayload()
        }
        assertEquals(3, prescriptions.size)
    }
}