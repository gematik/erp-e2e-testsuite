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

package de.gematik.test.erezept.primsys.rest

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import de.gematik.test.erezept.primsys.data.actors.ActorDto
import de.gematik.test.erezept.primsys.data.info.InfoDto
import io.ktor.client.request.*
import io.ktor.http.*


abstract class PrimSysBaseRequest<R>(val responseType: TypeReference<R>) {
    abstract fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper)
}

abstract class PrimSysBaseGenericRequest<R>(responseType: TypeReference<R>) : PrimSysBaseRequest<R>(responseType)

abstract class PrimSysBaseDoctorRequest<R>(responseType: TypeReference<R>) : PrimSysBaseRequest<R>(responseType)

abstract class PrimSysBasePharmacyRequest<R>(responseType: TypeReference<R>) : PrimSysBaseRequest<R>(responseType)


class InfoRequest: PrimSysBaseGenericRequest<InfoDto>(object : TypeReference<InfoDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Get
        rb.url.appendEncodedPathSegments("info")
    }
}

class ActorsRequest: PrimSysBaseGenericRequest<List<ActorDto>>(object : TypeReference<List<ActorDto>>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Get
        rb.url.appendEncodedPathSegments("actors")
    }
}

class GetReadyPrescriptionByIdRequest(private val prescriptionId: String) :
    PrimSysBaseGenericRequest<PrescriptionDto>(object : TypeReference<PrescriptionDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Get
        rb.url.appendEncodedPathSegments("prescription", "prescribed", prescriptionId)
    }
}

class GetAllReadyPrescriptions :
    PrimSysBaseGenericRequest<List<PrescriptionDto>>(object: TypeReference<List<PrescriptionDto>>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Get
        rb.url.appendEncodedPathSegments("prescription", "prescribed")
    }
}

object BasicRequests {

    @JvmStatic
    fun getInfo() = InfoRequest()

    @JvmStatic
    fun getActors() = ActorsRequest()

    @JvmStatic
    fun getReadyPrescription(id: String) = GetReadyPrescriptionByIdRequest(id)

    @JvmStatic
    fun getReadyPrescriptions() = GetAllReadyPrescriptions()
}