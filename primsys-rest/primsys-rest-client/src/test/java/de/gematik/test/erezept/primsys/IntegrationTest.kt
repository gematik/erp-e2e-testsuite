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
import de.gematik.test.erezept.primsys.rest.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class IntegrationTest {

    @Test
    @Disabled(value = "only for manual test and demonstration purposes")
    fun shouldRunAgainstLocalPrimSys() {
//        val clientFactory = PrimSysClientFactory.forRemote("https://erpps-test.dev.gematik.solutions").env("tu").apiKey(System.getenv("ERPIONE_API_KEY")).build()
        val clientFactory = PrimSysClientFactory.forRemote("http://127.0.0.1").port(9095).build()
        println(clientFactory.actorsInfo)

        val doc = clientFactory.getDoctorClient(0)
        val pharm = clientFactory.getPharmacyClient(0)

        val createResponse = doc.performBlocking(DoctorRequests.prescribe("X110407071", true))
//        val d: PrimSysResponse<PrescriptionDto> = doc.perform(
//            DoctorRequests.prescribe(
//                PrescribeRequestDto.forKvnr("X110407073").coveredBy(
//                    CoverageDto.ofType(InsuranceTypeDto.PKV).insurantState(InsurantStateDto.MEMBERS)
//                        .resident(WopDto.BERLIN)
//                        .build()
//                )
//            )
//        )

        val createDto = printResponse("Create", createResponse)

        val getResponse = doc.performBlocking(BasicRequests.getReadyPrescription(createDto.prescriptionId))
        val getDto = printResponse("GET", getResponse)

        val getAllResponse = doc.performBlocking(BasicRequests.getReadyPrescriptions())
        val getAll = printResponse("Got all prescriptions: ", getAllResponse)
        getAll.forEach {
            println("\t-> $it")
        }

        val acceptResponse = pharm.performBlocking(PharmacyRequests.accept(getDto.prescriptionId, getDto.accessCode))
        val acceptDto = printResponse("Accept", acceptResponse)

        // Note: no proper DTO for the body, needs to be implemented on the backend first!
        val replyBody = """
            {
              "version": 1,
              "supplyOptionsType": "onPremise",
              "info_text": "Die Abholung ist ab sofort vor Ort möglich",
              "url": "https://www.adler-apotheke-berlin-tegel.de/",
              "pickUpCodeHR": "12345",
              "pickUpCodeDMC": "6a3acd69-a01d-4780-885e-ea970b6aacdb"
            }
        """.trimIndent()
        val replyResponse =
            pharm.performBlocking(PharmacyCommunicationRequests.reply(getDto.prescriptionId, getDto.patient.kvnr, replyBody))
        val replyDto = printResponse("Reply", replyResponse)

        val dispenseResponse =
            pharm.performBlocking(PharmacyRequests.dispense(acceptDto.prescriptionId, acceptDto.secret, 3))
        val dispenseDto = printResponse("Dispense", dispenseResponse)

        val om = ObjectMapper().writerWithDefaultPrettyPrinter()
        println(om.writeValueAsString(dispenseDto))

        println("Done")
    }

    private fun <T> printResponse(operation: String, response: PrimSysResponse<T>): T {
        println(">> $operation : ${response.statusCode}")
        response.body
            .onLeft { println("${it.type} Error: ${it.message}") }
            .onRight { println(it) }

        return response.asExpectedPayload()
    }
}