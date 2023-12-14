/*
 * Copyright 2023 gematik GmbH
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

import arrow.core.Either
import de.gematik.test.erezept.primsys.data.error.ErrorDto
import de.gematik.test.erezept.primsys.rest.*
import io.restassured.http.ContentType
import io.restassured.mapper.ObjectMapper
import io.restassured.mapper.ObjectMapperDeserializationContext
import io.restassured.mapper.ObjectMapperSerializationContext
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import java.util.function.Supplier

abstract class PrimSysClient(val requestSpecSupplier: Supplier<RequestSpecification>) {

    fun <T> perform(request: PrimSysBaseGenericRequest<T>): PrimSysResponse<T> {
        val response = request.performOn(requestSpec())
        return unwrapResponse(request, response)
    }

    protected fun requestSpec(): RequestSpecification =
         requestSpecSupplier.get()
            .accept(ContentType.JSON)       // by default, JSON can be changed later on
            .contentType(ContentType.JSON)  // by default, JSON can be changed later on

    protected fun <R: PrimSysBaseRequest<T>, T> unwrapResponse(request: R, response: Response): PrimSysResponse<T> {
        try {
            val body = when(request.responseType.type == Unit.javaClass && response.isEmpty()) {
                true -> response.body.`as`(request.responseType.type, EmptyResponseMapper())
                false -> response.body.`as`<T>(request.responseType.type)
            }
            return PrimSysResponse(response.statusCode, response.headers, Either.Right(body))
        } catch (e: Exception) {
            return handleErrorResponse(response)
        }
    }


    private fun <T> handleErrorResponse(response: Response): PrimSysResponse<T> =
        when {
            response.isEmpty() ->
                PrimSysResponse(response.statusCode, response.headers, Either.Left(ErrorDto.internalError("no error message")))

            response.isHtml() -> PrimSysResponse(
                response.statusCode, response.headers,
                Either.Left(ErrorDto.internalError(response.then().extract().htmlPath().getString("")))
            )

            else -> handlePrimsysErrorResponse(response)
        }


    private fun <T> handlePrimsysErrorResponse(response: Response): PrimSysResponse<T> {
        return try {
            val body = response.body.`as`(ErrorDto::class.java)
            PrimSysResponse(response.statusCode, response.headers, Either.Left(body))
        } catch (e: Exception) {
            PrimSysResponse(response.statusCode, response.headers, Either.Left(ErrorDto.internalError(e.message)))
        }
    }
}

class PrimSysDoctor(val doctorId: String, requestSpecSupplier: Supplier<RequestSpecification>) :
    PrimSysClient(requestSpecSupplier) {

    fun <T> perform(request: PrimSysBaseDoctorRequest<T>): PrimSysResponse<T> {
        val response = request.performOn(doctorId, requestSpec())
        return unwrapResponse(request, response)
    }
}

class PrimSysPharmacy(val pharmacyId: String, requestSpecSupplier: Supplier<RequestSpecification>) :
    PrimSysClient(requestSpecSupplier) {

    fun <T> perform(request: PrimSysBasePharmacyRequest<T>): PrimSysResponse<T> {
        val response = request.performOn(pharmacyId, requestSpec())
        return unwrapResponse(request, response)
    }
}

fun Response.isEmpty(): Boolean {
    val bodyString = this.body.asString()
    return bodyString.isNullOrEmpty() || bodyString.isBlank()
}

fun Response.isHtml(): Boolean {
    return ContentType.HTML.matches(this.contentType)
}

private class EmptyResponseMapper() : ObjectMapper {
    override fun deserialize(p0: ObjectMapperDeserializationContext?) {
        return  // deserialize empty responses
    }

    override fun serialize(p0: ObjectMapperSerializationContext?): Any {
        return Unit
    }

}