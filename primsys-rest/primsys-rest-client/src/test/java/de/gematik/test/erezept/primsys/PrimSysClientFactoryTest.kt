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

package de.gematik.test.erezept.primsys

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.ktor.client.plugins.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.ConnectException


@WireMockTest(httpPort = RestTest.REST_PORT)
class PrimSysClientFactoryTest : RestTest() {

    @Test
    fun shouldGetBaseInformationOnStartup() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1:$REST_PORT").build()

        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))

        assertEquals(2, clientFactory.primsysInfo.doctors)
        assertEquals(2, clientFactory.primsysInfo.pharmacies)
        assertEquals(4, clientFactory.actorsInfo.size)
    }

    @Test
    fun shouldGetBaseInformationOnStartupWithEnv() {
        val env = "tu"
        setupPositiveStubs(env)
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1:$REST_PORT").env(env).apiKey("123").build()

        verify(exactly(1), getRequestedFor(urlPathEqualTo("/$env/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/$env/actors")))

        assertEquals(2, clientFactory.primsysInfo.doctors)
        assertEquals(2, clientFactory.primsysInfo.pharmacies)
        assertEquals(4, clientFactory.actorsInfo.size)
    }

    @Test
    fun shouldGetActorsByIndex() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        assertDoesNotThrow {
            clientFactory.getDoctorClient(0)
            clientFactory.getPharmacyClient(0)
        }
    }

    @Test
    fun shouldGetActorsByIdentifier() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

        assertDoesNotThrow {
            clientFactory.getDoctorClient("Doctor 0")
            clientFactory.getDoctorClient("DOCTOR 0")
            clientFactory.getDoctorClient("doc0")
            clientFactory.getPharmacyClient("Pharmacy 0")
            clientFactory.getPharmacyClient("pharmacy 0")
            clientFactory.getPharmacyClient("pharm0")
        }
    }

    @TestFactory
    fun shouldThrowOnUnknownDoctorClient() = actorIdentifierData().map { actorId ->
        dynamicTest("Should throw on unknown Doctor with ID $actorId") {
            setupPositiveStubs()
            val clientFactory = PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()

            assertThrows<UnknownActorException> {
                clientFactory.getDoctorClient(actorId)
            }
        }
    }

    @TestFactory
    fun shouldThrowOnUnknownPharmacyClient() = actorIdentifierData().map { actorId ->
        dynamicTest("Should throw on unknown Pharmacy with ID $actorId") {
            setupPositiveStubs()
            val clientFactory = PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()

            assertThrows<UnknownActorException> {
                clientFactory.getPharmacyClient(actorId)
            }
        }
    }

    fun actorIdentifierData() = arrayOf(
        "Doctor 00",
        "doc00",
        "Pharmacy 00",
        "pharm00"
    )

    @Test
    fun shouldGetRandomActors() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(REST_PORT).build()

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
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
    }

    @Test
    fun shouldFailOnErrorAtActors() {
        setupPositiveInfo()
        setupErrorActors()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleHtmlErrorResponseAtActors() {
        setupPositiveInfo()
        setupErrorActorsHtml()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleEmptyErrorResponseAtActors() {
        setupPositiveInfo()
        setupErrorActorsEmpty()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleInvalidPrimSysErrorResponse() {
        setupPositiveInfo()
        setupActorsInvalidErrorResponse(400)
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleInvalidPrimSysResponse() {
        setupPositiveInfo()
        setupActorsInvalidErrorResponse(200)
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleValidPrimSysErrorResponse() {
        setupPositiveInfo()
        setupActorsValidErrorResponse()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).build()
        }
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldThrowOnConnectionTimeout() {
        setupConnectionTimeout()
        val ce = assertThrows<HttpRequestTimeoutException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(REST_PORT).timeoutMillis(200).build()
        }
        assertTrue(ce.message!!.contains("timeout"))
        assertTrue(ce.message!!.contains("http://127.0.0.1:$REST_PORT/info"))

        verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        verify(exactly(0), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldThrowOnUnreachable() {
        assertThrows<ConnectException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").build()  // missing the port!!
        }
    }
}