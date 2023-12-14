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
class PrescribeRequestTest : RestTest() {

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

    @Test
    fun shouldPrescribe() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .withDefaultRequest("http://127.0.0.1:$REST_PORT")

        removeAllMappings()
        val doc = clientFactory.getRandomDoctorClient()
        val kvnr = "X123123123"
        setupPositivePrescribe(kvnr)
        val prescription = assertDoesNotThrow {
            doc.perform(DoctorRequests.prescribe(kvnr)).asExpectedPayload()
        }
        assertEquals(kvnr, prescription.patient.kvnr)
    }
}