/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.primsys

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import lombok.extern.slf4j.Slf4j
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@Slf4j
@WireMockTest
class PrimSysClientFactoryTest : RestTest() {

    @Test
    fun shouldGetBaseInformationOnStartup() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1:${port}").build()

        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))

        assertEquals(2, clientFactory.primsysInfo.doctors)
        assertEquals(2, clientFactory.primsysInfo.pharmacies)
        assertEquals(4, clientFactory.actorsInfo.size)
    }

    @Test
    fun shouldGetBaseInformationOnStartupWithEnv() {
        val env = "tu"
        setupPositiveStubs(env)
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1:${port}").env(env).apiKey("123").build()

        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/$env/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/$env/actors")))

        assertEquals(2, clientFactory.primsysInfo.doctors)
        assertEquals(2, clientFactory.primsysInfo.pharmacies)
        assertEquals(4, clientFactory.actorsInfo.size)
    }

    @Test
    fun shouldGetActorsByIndex() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(port).build()

        assertDoesNotThrow {
            clientFactory.getDoctorClient(0)
            clientFactory.getPharmacyClient(0)
        }
    }

    @Test
    fun shouldGetActorsByIdentifier() {
        setupPositiveStubs()
        val clientFactory = PrimSysClientFactory
            .forRemote("http://127.0.0.1").port(port).build()

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
                .forRemote("http://127.0.0.1").port(port).build()

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
                .forRemote("http://127.0.0.1").port(port).build()

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
            .forRemote("http://127.0.0.1").port(port).build()

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
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
    }

    @Test
    fun shouldFailOnErrorAtActors() {
        setupPositiveInfo()
        setupErrorActors()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleHtmlErrorResponseAtActors() {
        setupPositiveInfo()
        setupErrorActorsHtml()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleEmptyErrorResponseAtActors() {
        setupPositiveInfo()
        setupErrorActorsEmpty()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleInvalidPrimSysErrorResponse() {
        setupPositiveInfo()
        setupActorsInvalidErrorResponse(400)
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleInvalidPrimSysResponse() {
        setupPositiveInfo()
        setupActorsInvalidErrorResponse(200)
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldHandleValidPrimSysErrorResponse() {
        setupPositiveInfo()
        setupActorsValidErrorResponse()
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).build()
        }
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(1), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldThrowOnConnectionTimeout() {
        val timeout = 100   // max timeout 100ms
        setupConnectionTimeout(timeout * 2)  // delay 200ms
        val ce = assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").port(port).timeoutMillis(timeout.toLong()).build()
        }

        log.warn("Request to port $port produced timeout exception: ${ce.message}")
        assertTrue(ce.message!!.contains("timeout"))
        assertTrue(ce.message!!.contains("http://127.0.0.1:${port}/info"))

        // in CI the info request is not detected reliably, so we allow 0 or 1 requests to be detected
        wiremockExtension.verify(lessThanOrExactly(1), getRequestedFor(urlPathEqualTo("/info")))
        wiremockExtension.verify(exactly(0), getRequestedFor(urlPathEqualTo("/actors")))
    }

    @Test
    fun shouldThrowOnUnreachable() {
        assertThrows<PrimSysRestException> {
            PrimSysClientFactory
                .forRemote("http://127.0.0.1").build()  // missing the port!!
        }
    }
}