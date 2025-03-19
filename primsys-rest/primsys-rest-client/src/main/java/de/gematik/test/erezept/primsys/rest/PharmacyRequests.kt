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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto
import de.gematik.test.erezept.primsys.data.communication.CommunicationDto
import io.ktor.client.request.*
import io.ktor.http.*

class AcceptRequest(private val prescriptionId: String, private val accessCode: String) :
    PrimSysBasePharmacyRequest<AcceptedPrescriptionDto>(object : TypeReference<AcceptedPrescriptionDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("accept")
        rb.url.parameters.append("taskId", prescriptionId)
        rb.url.parameters.append("ac", accessCode)
    }
}

class CloseRequest(private val prescriptionId: String, private val secret: String, private val body: List<PznDispensedMedicationDto>) :
        PrimSysBasePharmacyRequest<DispensedMedicationDto>(object : TypeReference<DispensedMedicationDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("close")
        rb.url.parameters.append("taskId", prescriptionId)
        rb.url.parameters.append("secret", secret)

        val requestBody = bodyMapper.writeValueAsString(body)
        rb.setBody(requestBody)
    }
}

class RejectRequest(private val prescriptionId: String, private val accessCode: String, private val secret: String) :
        PrimSysBasePharmacyRequest<Unit>(object : TypeReference<Unit>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("reject")
        rb.url.parameters.append("taskId", prescriptionId)
        rb.url.parameters.append("ac", accessCode)
        rb.url.parameters.append("secret", secret)
    }
}

class ReplyRequest(private val prescriptionId: String, private val kvnr: String, private val body: String): PrimSysBasePharmacyRequest<Unit>(object : TypeReference<Unit>() {}) {
    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("reply")
        rb.url.parameters.append("taskId", prescriptionId)
        rb.url.parameters.append("kvnr", kvnr)
        rb.setBody(body)
    }
}

class CommunicationSearchRequest(private val sender: String?, private val receiver: String?): PrimSysBasePharmacyRequest<List<CommunicationDto>>(object : TypeReference<List<CommunicationDto>>() {}) {
    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Get
        rb.url.appendEncodedPathSegments("communications")
        sender?.let { rb.url.parameters.append("sender", it) }
        receiver?.let { rb.url.parameters.append("receiver", it) }
    }
}

class CommunicationDeleteRequest(private val id: String): PrimSysBasePharmacyRequest<Unit>(object : TypeReference<Unit>() {}) {
    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Delete
        rb.url.appendEncodedPathSegments("communication")
        rb.url.appendEncodedPathSegments(id)
    }
}

object PharmacyRequests {

    @JvmStatic
    fun accept(prescriptionId: String, accessCode: String) = AcceptRequest(prescriptionId, accessCode)

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String) = dispense(prescriptionId, secret, listOf())

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, medication: PznDispensedMedicationDto) = dispense(prescriptionId, secret, listOf(medication))

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, num: Int) : CloseRequest {
        val body = mutableListOf<PznDispensedMedicationDto>()
        assert (num >= 0) { "Amount of dispensed Medications must be >= 0" }
        for (i in 1..num) {
            body.add(PznDispensedMedicationDto())
        }
        return dispense(prescriptionId, secret, body)
    }

    @JvmStatic
    fun dispense(prescriptionId: String, secret: String, body: List<PznDispensedMedicationDto>) = CloseRequest(prescriptionId, secret, body)

    @JvmStatic
    fun reject(prescriptionId: String, accessCode: String, secret: String) = RejectRequest(prescriptionId, accessCode, secret)
}

object PharmacyCommunicationRequests {
    @JvmStatic
    fun reply(prescriptionId: String, kvnr: String, body: String) = ReplyRequest(prescriptionId, kvnr, body)

    @JvmStatic
    fun search(sender: String? = null, receiver: String? = null) = CommunicationSearchRequest(sender, receiver)

    @JvmStatic
    fun delete(id: String) = CommunicationDeleteRequest(id)
}