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

package de.gematik.test.erezept.primsys

import arrow.core.Either
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.gematik.test.erezept.primsys.PrimSysClientFactory.ClientData
import de.gematik.test.erezept.primsys.data.error.ErrorDto
import de.gematik.test.erezept.primsys.rest.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

abstract class PrimSysClient(
    val httpClient: HttpClient, val clientData: ClientData
) {

    protected val objectMapper: ObjectMapper = ObjectMapper()
    private val contentType: ContentType = ContentType.Application.Json


    protected fun initRequestBuilder(): HttpRequestBuilder {
        val rb = HttpRequestBuilder()
        rb.url.protocol = clientData.protocol
        rb.url.host = clientData.host

        clientData.port?.let { rb.url.port = it }
        clientData.env?.let { rb.url.appendEncodedPathSegments(it) }

        clientData.apiKey?.let { rb.header("apiKey", it) }

        rb.header(HttpHeaders.ContentType, contentType)
        rb.header(HttpHeaders.Accept, contentType)
        return rb
    }

    suspend fun <T> perform(request: PrimSysBaseGenericRequest<T>): PrimSysResponse<T> {
        val rb = this.initRequestBuilder()
        request.finalizeRequest(rb, this.objectMapper)
        return performHttpRequest(request.responseType, rb)
    }

    fun <T> performBlocking(request: PrimSysBaseGenericRequest<T>): PrimSysResponse<T> = runBlocking {
        perform(request)
    }

    protected suspend fun <T> performHttpRequest(
        expectedType: TypeReference<T>, rb: HttpRequestBuilder
    ): PrimSysResponse<T> {
        val response = httpClient.prepareRequest(rb).execute()
        return unwrapResponse(expectedType, response)
    }


    private suspend fun <R : PrimSysBaseRequest<T>, T> unwrapResponse(
        expectedType: TypeReference<T>, response: HttpResponse
    ): PrimSysResponse<T> {
        try {
            return when (response.status.value < 300) {
                true -> {
                    when (expectedType.type) {
                        Unit::class.java -> return PrimSysResponse(
                            response.status.value,
                            response.headers,
                            @Suppress("UNCHECKED_CAST")
                            Either.Right(Unit as T)
                        )

                        else -> {
                            val body = objectMapper.readValue(response.bodyAsText(), expectedType)
                            return PrimSysResponse(response.status.value, response.headers, Either.Right(body))
                        }
                    }
                }

                false -> handleErrorResponse(response)
            }
        } catch (e: Exception) {
            return handleErrorResponse(response)
        }
    }


    private suspend fun <T> handleErrorResponse(response: HttpResponse): PrimSysResponse<T> =
        when {
            response.isHtml() -> PrimSysResponse(
                response.status.value, response.headers,
                Either.Left(ErrorDto.internalError(response.bodyAsText()))
            )

            response.isEmpty() ->
                PrimSysResponse(
                    response.status.value,
                    response.headers,
                    Either.Left(ErrorDto.internalError("no error message"))
                )

            else -> handlePrimsysErrorResponse(response)
        }


    private suspend fun <T> handlePrimsysErrorResponse(response: HttpResponse): PrimSysResponse<T> {
        return try {
            val body = this.objectMapper.readValue(response.bodyAsText(), ErrorDto::class.java)
            PrimSysResponse(response.status.value, response.headers, Either.Left(body))
        } catch (e: Exception) {
            PrimSysResponse(response.status.value, response.headers, Either.Left(ErrorDto.internalError(e.message)))
        }
    }
}


class PrimSysDoctor(
    val doctorId: String, httpClient: HttpClient, clientData: ClientData
) : PrimSysClient(httpClient, clientData) {

    private fun setActorId(rb: HttpRequestBuilder) {
        rb.url.appendEncodedPathSegments("doc", doctorId)
    }

    suspend fun <T> perform(request: PrimSysBaseDoctorRequest<T>): PrimSysResponse<T> {
        val rb = this.initRequestBuilder()
        this.setActorId(rb)
        request.finalizeRequest(rb, this.objectMapper)
        return performHttpRequest(request.responseType, rb)
    }

    fun <T> performBlocking(request: PrimSysBaseDoctorRequest<T>): PrimSysResponse<T> = runBlocking {
        perform(request)
    }
}

class PrimSysPharmacy(private val pharmacyId: String, httpClient: HttpClient, clientData: ClientData) :
    PrimSysClient(httpClient, clientData) {

    private fun setActorId(rb: HttpRequestBuilder) {
        rb.url.appendEncodedPathSegments("pharm", pharmacyId)
    }

    suspend fun <T> perform(request: PrimSysBasePharmacyRequest<T>): PrimSysResponse<T> {
        val rb = this.initRequestBuilder()
        this.setActorId(rb)
        request.finalizeRequest(rb, this.objectMapper)
        return performHttpRequest(request.responseType, rb)
    }

    fun <T> performBlocking(request: PrimSysBasePharmacyRequest<T>): PrimSysResponse<T> = runBlocking {
        perform(request)
    }
}

fun HttpResponse.isEmpty(): Boolean {
    return this.headers[HttpHeaders.ContentLength]?.equals("0") ?: true // no Content-Length will result in empty body
}

fun HttpResponse.isHtml(): Boolean {
    return this.headers[HttpHeaders.ContentType]?.contains("html") ?: false
}