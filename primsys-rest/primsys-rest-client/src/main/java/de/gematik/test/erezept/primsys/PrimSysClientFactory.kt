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
import de.gematik.test.erezept.primsys.data.actors.ActorDto
import de.gematik.test.erezept.primsys.data.actors.ActorType
import de.gematik.test.erezept.primsys.data.info.InfoDto
import de.gematik.test.erezept.primsys.rest.ActorsRequest
import de.gematik.test.erezept.primsys.rest.InfoRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import java.util.function.Supplier
import kotlin.random.Random


class PrimSysClientFactory(
    requestSpecSupplier: Supplier<RequestSpecification>
) : PrimSysClient(requestSpecSupplier) {

    val primsysInfo: InfoDto
    val actorsInfo: List<ActorDto>
    private val doctors: List<ActorDto>
    private val pharmacies: List<ActorDto>

    init {
        val infoResponse = perform(InfoRequest())
        primsysInfo = when (infoResponse.body) {
            is Either.Left -> throw PrimSysRestException(infoResponse.statusCode, infoResponse.body.value)
            is Either.Right -> infoResponse.body.value
        }

        val actorsResponse = perform(ActorsRequest())
        actorsInfo = when (actorsResponse.body) {
            is Either.Left -> throw PrimSysRestException(actorsResponse.statusCode, actorsResponse.body.value)
            is Either.Right -> actorsResponse.body.value
        }

        doctors = actorsInfo.filter { it.type == ActorType.DOCTOR }
        pharmacies = actorsInfo.filter { it.type == ActorType.PHARMACY }
    }

    fun getDoctorClient(identifier: String): PrimSysDoctor {
        val doctorDto = findActorDto(ActorType.DOCTOR, identifier)
        return PrimSysDoctor(doctorDto.id, requestSpecSupplier)
    }

    fun getDoctorClient(idx: Int): PrimSysDoctor =
        PrimSysDoctor(doctors[idx].id, requestSpecSupplier)


    fun getRandomDoctorClient(): PrimSysDoctor =
        getDoctorClient(Random.nextInt(doctors.size))

    fun getPharmacyClient(idx: Int): PrimSysPharmacy =
        PrimSysPharmacy(pharmacies[idx].id, requestSpecSupplier)

    fun getPharmacyClient(identifier: String): PrimSysPharmacy {
        val pharmacyDto = findActorDto(ActorType.PHARMACY, identifier)
        return PrimSysPharmacy(pharmacyDto.id, requestSpecSupplier)
    }

    fun getRandomPharmacyClient(): PrimSysPharmacy = getPharmacyClient(Random.nextInt(pharmacies.size))

    private fun findActorDto(type: ActorType, identifier: String): ActorDto =
        actorsInfo.find { it.type == type && (it.id == identifier || it.name.lowercase() == identifier.lowercase()) }
            ?: throw UnknownActorException(type, identifier)

    companion object {

        @JvmStatic
        fun withDefaultRequest(baseUri: String, env: String = "", apiKey: String = ""): PrimSysClientFactory {
            return withCustomRequest(baseUri, env, apiKey) {
                RestAssured.given()
            }
        }

        @JvmStatic
        fun withCustomRequest(
            baseUri: String,
            env: String = "",
            apiKey: String = "",
            reqSpec: Supplier<RequestSpecification>
        ): PrimSysClientFactory {
            val envUri: String = if (env.isEmpty()) baseUri else "$baseUri/$env"

            return PrimSysClientFactory {
                reqSpec.get().baseUri(envUri)
                    .header("apiKey", apiKey)
                    .accept(ContentType.JSON)       // by default, JSON can be changed later on
                    .contentType(ContentType.JSON)  // by default, JSON can be changed later on
            }
        }
    }
}




