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

package de.gematik.test.erezept.primsys.rest

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.File
import java.nio.file.Path

class PrescribeRequest(private val body: PrescribeRequestDto, private val asDirectAssignment: Boolean = false) :
    PrimSysBaseDoctorRequest<PrescriptionDto>(object : TypeReference<PrescriptionDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("prescribe")
        rb.url.parameters.append("direct", asDirectAssignment.toString())

        val requestBody = bodyMapper.writeValueAsString(body)
        rb.setBody(requestBody)
    }
}

class PrescribeKbvBundleRequest(private val body: String, private val asDirectAssignment: Boolean = false) :
    PrimSysBaseDoctorRequest<PrescriptionDto>(object : TypeReference<PrescriptionDto>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Post
        rb.url.appendEncodedPathSegments("xml", "prescribe")
        rb.url.parameters.append("direct", asDirectAssignment.toString())

        // we are sending an XML file:
        // so we need to set the content type to XML and remove the default JSON content type
        rb.headers.remove(HttpHeaders.ContentType)
        rb.header(HttpHeaders.ContentType, ContentType.Application.Xml)
        rb.setBody(body)
    }
}

class AbortDoctorPrescriptionRequest(private val prescriptionId: String, private val accessCode: String) :
    PrimSysBaseDoctorRequest<Unit>(object : TypeReference<Unit>() {}) {

    override fun finalizeRequest(rb: HttpRequestBuilder, bodyMapper: ObjectMapper) {
        rb.method = HttpMethod.Delete
        rb.url.appendEncodedPathSegments("abort")
        rb.url.parameters.append("taskId", prescriptionId)
        rb.url.parameters.append("ac", accessCode)
    }
}


object DoctorRequests {

    @JvmStatic
    fun prescribeKbvBundle(file: Path, asDirectAssignment: Boolean = false) = prescribeKbvBundle(file.toFile(), asDirectAssignment)

    @JvmStatic
    fun prescribeKbvBundle(file: File, asDirectAssignment: Boolean = false) = prescribeKbvBundle(file.readText(), asDirectAssignment)

    @JvmStatic
    fun prescribeKbvBundle(kbvBundle: String, asDirectAssignment: Boolean = false) = PrescribeKbvBundleRequest(kbvBundle, asDirectAssignment)

    @JvmStatic
    fun prescribe(kvnr: String, asDirectAssignment: Boolean = false) =
        prescribe(PrescribeRequestDto.forKvnr(kvnr), asDirectAssignment)

    @JvmStatic
    fun prescribe(bodyBuilder: PrescribeRequestDto.Builder, asDirectAssignment: Boolean = false) =
        prescribe(bodyBuilder.build(), asDirectAssignment)

    @JvmStatic
    fun prescribe(body: PrescribeRequestDto, asDirectAssignment: Boolean = false) =
        PrescribeRequest(body, asDirectAssignment)

    @JvmStatic
    fun abort(prescriptionId: String, accessCode: String) =
        AbortDoctorPrescriptionRequest(prescriptionId, accessCode)

    @JvmStatic
    fun abort(prescription: PrescriptionDto) = abort(prescription.taskId, prescription.accessCode)
}