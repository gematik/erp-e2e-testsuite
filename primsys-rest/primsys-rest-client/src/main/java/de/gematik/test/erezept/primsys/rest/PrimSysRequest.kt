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
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

abstract class PrimSysBaseRequest<R>(val responseType: TypeReference<R>) {

    fun getAcceptContentType(): ContentType {
        return ContentType.JSON
    }

    open fun getContentType(): ContentType {
        return ContentType.JSON
    }
}

abstract class PrimSysBaseGenericRequest<R>(responseType: TypeReference<R>): PrimSysBaseRequest<R>(responseType) {

    abstract fun performOn(reqSpec: RequestSpecification): Response
}

abstract class PrimSysBaseDoctorRequest<R>(responseType: TypeReference<R>): PrimSysBaseRequest<R>(responseType) {

    abstract fun performOn(doctorId: String, reqSpec: RequestSpecification): Response
}

abstract class PrimSysBasePharmacyRequest<R>(responseType: TypeReference<R>): PrimSysBaseRequest<R>(responseType) {

    abstract fun performOn(pharmacyId: String, reqSpec: RequestSpecification): Response
}