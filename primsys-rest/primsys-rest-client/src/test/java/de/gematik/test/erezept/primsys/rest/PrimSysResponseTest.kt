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
 */

package de.gematik.test.erezept.primsys.rest

import arrow.core.Either
import de.gematik.test.erezept.primsys.PrimSysRestException
import de.gematik.test.erezept.primsys.data.error.ErrorDto
import de.gematik.test.erezept.primsys.data.error.ErrorType
import de.gematik.test.erezept.primsys.data.info.InfoDto
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PrimSysResponseTest {

    @Test
    fun shouldGetAsExpectedResponsePayload() {
        val response = PrimSysResponse(200, Headers.build {
            append("Content-Type", "application/json")
        }, Either.Right("Hello World"))
        assertTrue(response.successful())
        assertDoesNotThrow { response.asExpectedPayload() }
    }

    @Test
    fun shouldContainErrorOnUnexpectedResponsePayload() {
        val response = PrimSysResponse(400, Headers.build {
            append("Content-Type", "application/json")
        }, Either.Left(ErrorDto(ErrorType.INTERNAL, "test error message")))
        assertFalse(response.successful())
        val psre = assertThrowsExactly(PrimSysRestException::class.java) { response.asExpectedPayload() }
        psre.message?.contains("test error message")?.let { assertTrue(it) }
        psre.message?.contains(ErrorType.INTERNAL.name)?.let { assertTrue(it) }
    }

    @Test
    fun shouldGetExpectedErrorResponsePayload() {
        val response = PrimSysResponse(400, Headers.build {
            append("Content-Type", "application/json")
        }, Either.Left(ErrorDto(ErrorType.INTERNAL, "test error message")))
        assertFalse(response.successful())
        val error = response.asError()
        assertEquals(ErrorType.INTERNAL, error.type)
        assertTrue(error.message.contains("test error message"))
    }

    @Test
    fun shouldContainBodyOnUnexpectedPositiveResponse() {
        val info = InfoDto()
        info.doctors = 2
        info.pharmacies = 3
        val response = PrimSysResponse(200, Headers.build {
            append("Content-Type", "application/json")
        }, Either.Right(info))

        assertTrue(response.successful())
        val psre = assertThrowsExactly(PrimSysRestException::class.java) { response.asError() }
        assertNotNull(psre.message)
    }
}