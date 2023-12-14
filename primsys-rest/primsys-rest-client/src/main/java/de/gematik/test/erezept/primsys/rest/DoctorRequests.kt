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
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

class PrescribeRequest(private val body: PrescribeRequestDto, private val asDirectAssignment: Boolean = false) :
    PrimSysBaseDoctorRequest<PrescriptionDto>(object : TypeReference<PrescriptionDto>() {}) {

    override fun performOn(doctorId: String, reqSpec: RequestSpecification): Response {
        return reqSpec.queryParam("direct", asDirectAssignment)
            .body(body)
            .post("/doc/$doctorId/prescribe")
    }
}

class GetReadyPrescriptionByIdRequest(private val prescriptionId: String) :
    PrimSysBaseGenericRequest<PrescriptionDto>(object : TypeReference<PrescriptionDto>() {}) {
    override fun performOn(reqSpec: RequestSpecification): Response {
        return reqSpec.get("/prescription/prescribed/$prescriptionId")
    }
}

class GetAllReadyPrescriptions :
    PrimSysBaseGenericRequest<List<PrescriptionDto>>(object : TypeReference<List<PrescriptionDto>>() {}) {
    override fun performOn(reqSpec: RequestSpecification): Response {
        return reqSpec.get("/prescription/prescribed")
    }
}

object DoctorRequests {

    @JvmStatic
    fun prescribe(kvnr: String, asDirectAssignment: Boolean = false) = prescribe(PrescribeRequestDto.forKvnr(kvnr), asDirectAssignment)

    @JvmStatic
    fun prescribe(bodyBuilder: PrescribeRequestDto.Builder, asDirectAssignment: Boolean = false) = prescribe(bodyBuilder.build(), asDirectAssignment)

    @JvmStatic
    fun prescribe(body: PrescribeRequestDto, asDirectAssignment: Boolean = false) = PrescribeRequest(body, asDirectAssignment)


    @JvmStatic
    fun getReadyPrescription(id: String) = GetReadyPrescriptionByIdRequest(id)

    @JvmStatic
    fun getReadyPrescriptions() = GetAllReadyPrescriptions()
}