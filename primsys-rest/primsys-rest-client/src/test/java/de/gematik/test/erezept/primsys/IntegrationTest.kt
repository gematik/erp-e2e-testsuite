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

import com.fasterxml.jackson.databind.ObjectMapper
import de.gematik.test.erezept.primsys.data.CoverageDto
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto
import de.gematik.test.erezept.primsys.data.PrescriptionDto
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto
import de.gematik.test.erezept.primsys.data.valuesets.InsurantStateDto
import de.gematik.test.erezept.primsys.data.valuesets.WopDto
import de.gematik.test.erezept.primsys.rest.DoctorRequests
import de.gematik.test.erezept.primsys.rest.PharmacyRequests
import de.gematik.test.erezept.primsys.rest.PrimSysResponse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class IntegrationTest {

    @Test
    @Disabled(value = "only for manual test and demonstration purposes")
    fun shouldRunAgainstLocalPrimSys() {
        val clientFactory = PrimSysClientFactory.withDefaultRequest("http://127.0.0.1:9095", apiKey = "dummy value")
        println(clientFactory.actorsInfo)

        val doc = clientFactory.getDoctorClient(0)
        val pharm = clientFactory.getPharmacyClient(0)

        val createResponse = doc.perform(DoctorRequests.prescribe("X110407073", true))
        val d: PrimSysResponse<PrescriptionDto> = doc.perform(
            DoctorRequests.prescribe(
                PrescribeRequestDto.forKvnr("X110407073").coveredBy(
                    CoverageDto.ofType(InsuranceTypeDto.PKV).insurantState(InsurantStateDto.MEMBERS)
                        .resident(WopDto.BERLIN)
                        .build()
                )
            )
        )

        val createDto = printResponse("Create", createResponse)

        val getResponse = doc.perform(DoctorRequests.getReadyPrescription(createDto.prescriptionId))
        val getDto = printResponse("GET", getResponse)

        val getAllResponse = doc.perform(DoctorRequests.getReadyPrescriptions())
        val getAll = printResponse("Got all prescriptions: ", getAllResponse)
        getAll.toList().forEach {
            println("\t-> $it")
        }

        val acceptResponse = pharm.perform(PharmacyRequests.accept(getDto.prescriptionId, getDto.accessCode))
        val acceptDto = printResponse("Accept", acceptResponse)

        val dispenseResponse = pharm.perform(PharmacyRequests.dispense(acceptDto.prescriptionId, acceptDto.secret, 3))
        val dispenseDto = printResponse("Dispense", dispenseResponse)

        val om = ObjectMapper().writerWithDefaultPrettyPrinter()
        println(om.writeValueAsString(dispenseDto))

        println("Done")
    }

    fun <T> printResponse(operation: String, response: PrimSysResponse<T>): T {
        println(">> $operation : ${response.statusCode}")
        response.body
            .onLeft { println("Error: ${it.message}") }
            .onRight { println(it) }

        return response.asExpectedPayload()
    }
}