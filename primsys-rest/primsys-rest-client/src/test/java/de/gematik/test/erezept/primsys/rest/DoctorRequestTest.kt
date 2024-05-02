package de.gematik.test.erezept.primsys.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import de.gematik.test.erezept.primsys.PrimSysClientFactory
import de.gematik.test.erezept.primsys.RestTest
import de.gematik.test.erezept.primsys.data.PatientDto
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

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

    private fun setupPositivePrescriptionById(): StubMapping {
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

    private fun setupPositivePrescriptions(prescriptionIds: List<String>): StubMapping {
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

    @Test
    fun shouldPrescribe() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        val doc = clientFactory.getRandomDoctorClient()
        val kvnr = "X123123123"
        setupPositivePrescribe(kvnr)
        val prescription = assertDoesNotThrow {
            doc.performBlocking(DoctorRequests.prescribe(kvnr)).asExpectedPayload()
        }
        assertEquals(kvnr, prescription.patient.kvnr)
    }

    @Test
    fun shouldGetOpenPrescriptionsById() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositivePrescriptionById()

        val doc = clientFactory.getRandomDoctorClient()
        val prescription = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getReadyPrescription("160.100.000.000.011.10")).asExpectedPayload()
        }
        assertEquals("160.100.000.000.011.10", prescription.prescriptionId)
    }

    @Test
    fun shouldGetAllOpenPrescriptions() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        removeAllMappings()
        setupPositivePrescriptions(listOf("160.100.000.000.011.11", "160.100.000.000.011.12", "160.100.000.000.011.13"))

        val doc = clientFactory.getRandomDoctorClient()
        val prescriptions = assertDoesNotThrow {
            doc.performBlocking(BasicRequests.getReadyPrescriptions()).asExpectedPayload()
        }
        assertEquals(3, prescriptions.size)
    }
}