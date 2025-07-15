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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys

import arrow.core.Either
import de.gematik.test.erezept.primsys.data.actors.ActorDto
import de.gematik.test.erezept.primsys.data.actors.ActorType
import de.gematik.test.erezept.primsys.data.info.InfoDto
import de.gematik.test.erezept.primsys.rest.BasicRequests
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlin.random.Random


class PrimSysClientFactory(
    httpClient: HttpClient,
    clientData: ClientData
) : PrimSysClient(httpClient, clientData) {

    val primsysInfo: InfoDto
    val actorsInfo: List<ActorDto>
    private val doctors: List<ActorDto>
    private val pharmacies: List<ActorDto>

    init {
        val infoResponse = performBlocking(BasicRequests.getInfo())
        primsysInfo = when (infoResponse.body) {
            is Either.Left -> throw PrimSysRestException(infoResponse.statusCode, infoResponse.body.value)
            is Either.Right -> infoResponse.body.value
        }

        val actorsResponse = performBlocking(BasicRequests.getActors())
        actorsInfo = when (actorsResponse.body) {
            is Either.Left -> throw PrimSysRestException(actorsResponse.statusCode, actorsResponse.body.value)
            is Either.Right -> actorsResponse.body.value
        }

        doctors = actorsInfo.filter { it.type == ActorType.DOCTOR }
        pharmacies = actorsInfo.filter { it.type == ActorType.PHARMACY }
    }

    fun getDoctorClient(identifier: String): PrimSysDoctor {
        val doctorDto = findActorDto(ActorType.DOCTOR, identifier)
        return PrimSysDoctor(doctorDto.id, httpClient, clientData)
    }

    fun getDoctorClient(idx: Int): PrimSysDoctor =
        PrimSysDoctor(doctors[idx].id, httpClient, clientData)


    fun getRandomDoctorClient(): PrimSysDoctor =
        getDoctorClient(Random.nextInt(doctors.size))

    fun getPharmacyClient(idx: Int): PrimSysPharmacy =
        PrimSysPharmacy(pharmacies[idx].id, httpClient, clientData)

    fun getPharmacyClient(identifier: String): PrimSysPharmacy {
        val pharmacyDto = findActorDto(ActorType.PHARMACY, identifier)
        return PrimSysPharmacy(pharmacyDto.id, httpClient, clientData)
    }

    fun getRandomPharmacyClient(): PrimSysPharmacy = getPharmacyClient(Random.nextInt(pharmacies.size))

    private fun findActorDto(type: ActorType, identifier: String): ActorDto =
        actorsInfo.find { it.type == type && (it.id == identifier || it.name.equals(identifier, ignoreCase = true)) }
            ?: throw UnknownActorException(type, identifier)

    companion object {
        @JvmStatic
        fun forRemote(baseUrl: String): ClientBuilder {
            val url = URLBuilder(baseUrl)
            val rd = ClientBuilder(url.protocol, url.host)

            if (url.port > 0) {
                rd.port(url.port)
            }

            return rd
        }
    }

    class ClientBuilder(protocol: URLProtocol, host: String) {

        val data: ClientData = ClientData(protocol, host)

        private var timeoutMillis: Long = 15000

        fun env(env: String) = apply { data.env(env.lowercase()) }
        fun port(port: Int) = apply { data.port(port) }
        fun apiKey(apiKey: String) = apply { data.apiKey(apiKey) }

        fun timeoutMillis(timeout: Long) = apply {
            this.timeoutMillis = timeout
        }

        fun build(): PrimSysClientFactory {
            val httpClient = HttpClient {
                install(HttpTimeout) {
                    connectTimeoutMillis = timeoutMillis
                    requestTimeoutMillis = timeoutMillis
                }
            }
            return build(httpClient)
        }

        fun build(httpClient: HttpClient): PrimSysClientFactory {
            return PrimSysClientFactory(httpClient, data)
        }
    }

    data class ClientData(
        val protocol: URLProtocol,
        val host: String,
        var port: Int? = null,
        var env: String? = null,
        var apiKey: String? = null
    ) {
        fun env(env: String) = apply { this.env = env.lowercase() }
        fun port(port: Int) = apply { this.port = port }
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }
    }
}




