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
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto
import de.gematik.test.erezept.primsys.data.PznMedicationDto
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

class AcceptRequest(private val prescriptionId: String, private val accessCode: String) :
    PrimSysBasePharmacyRequest<AcceptedPrescriptionDto>(object : TypeReference<AcceptedPrescriptionDto>() {}) {

    override fun performOn(pharmacyId: String, reqSpec: RequestSpecification): Response {
        return reqSpec.post("pharm/$pharmacyId/accept?taskId=$prescriptionId&ac=$accessCode")
    }
}

class CloseRequest(private val prescriptionId: String, private val secret: String, private val body: List<PznMedicationDto>) :
        PrimSysBasePharmacyRequest<DispensedMedicationDto>(object : TypeReference<DispensedMedicationDto>() {}) {
    override fun performOn(pharmacyId: String, reqSpec: RequestSpecification): Response {
        if (body.isNotEmpty()) {
            reqSpec.body(body)
        }
        return reqSpec.post("pharm/$pharmacyId/close?taskId=$prescriptionId&secret=$secret")
    }

}

class RejectRequest(private val prescriptionId: String, private val accessCode: String, private val secret: String) :
        PrimSysBasePharmacyRequest<Unit>(object : TypeReference<Unit>() {}) {
    override fun performOn(pharmacyId: String, reqSpec: RequestSpecification): Response {
        return reqSpec.post("pharm/$pharmacyId/reject?taskId=$prescriptionId&ac=$accessCode&secret=$secret")
    }
}

object PharmacyRequests {

    @JvmStatic
    fun accept(prescriptionId: String, accessCode: String) = AcceptRequest(prescriptionId, accessCode)

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String) = CloseRequest(prescriptionId, secret, listOf())

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, medication: PznMedicationDto) {
        val body = listOf(medication)
        CloseRequest(prescriptionId, secret, body)
    }

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, num: Int) : CloseRequest {
        val body = mutableListOf<PznMedicationDto>()
        for (i in 1..num) {
            body.add(PznMedicationDto())
        }
        return CloseRequest(prescriptionId, secret, body)
    }

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, body: List<PznMedicationDto>) = CloseRequest(prescriptionId, secret, body)

    @JvmStatic
    fun reject(prescriptionId: String, accessCode: String, secret: String) = RejectRequest(prescriptionId, accessCode, secret)
}