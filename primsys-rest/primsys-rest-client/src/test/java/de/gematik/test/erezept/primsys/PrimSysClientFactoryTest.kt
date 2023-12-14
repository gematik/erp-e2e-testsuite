package de.gematik.test.erezept.primsys

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


@WireMockTest(httpPort = RestTest.REST_PORT)
class PrimSysClientFactoryTest : RestTest() {

    @Test
    fun shouldGetBaseInformationOnStartup() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .withDefaultRequest("http://127.0.0.1:$REST_PORT")

        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldGetActorsByIndex() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .withDefaultRequest("http://127.0.0.1:$REST_PORT")

        assertDoesNotThrow {
            clientFactory.getDoctorClient(0)
            clientFactory.getPharmacyClient(0)
        }
    }

    @Test
    fun shouldGetActorsByIdentifier() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .withDefaultRequest("http://127.0.0.1:$REST_PORT")

        assertDoesNotThrow {
            clientFactory.getDoctorClient("Doctor 0")
            clientFactory.getDoctorClient("doc0")
            clientFactory.getPharmacyClient("Pharmacy 0")
            clientFactory.getPharmacyClient("pharm0")
        }
    }

    @Test
    fun shouldGetRandomActors() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .withDefaultRequest("http://127.0.0.1:$REST_PORT")

        assertDoesNotThrow {
            clientFactory.getRandomDoctorClient()
            clientFactory.getRandomPharmacyClient()
        }
    }

    @Test
    fun shouldFailOnErrorAtInformation() {
        setupErrorInfo()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .withDefaultRequest("http://127.0.0.1:$REST_PORT")
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
    }

    @Test
    fun shouldFailOnErrorAtActors() {
        setupPositiveInfo()
        setupErrorActors()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .withDefaultRequest("http://127.0.0.1:$REST_PORT")
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }
}