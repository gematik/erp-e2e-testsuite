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
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import de.gematik.test.erezept.primsys.data.actors.ActorDto
import de.gematik.test.erezept.primsys.data.actors.ActorType
import de.gematik.test.erezept.primsys.data.error.ErrorDto
import de.gematik.test.erezept.primsys.data.error.ErrorType
import de.gematik.test.erezept.primsys.data.info.BuildInfoDto
import de.gematik.test.erezept.primsys.data.info.FhirInfoDto
import de.gematik.test.erezept.primsys.data.info.InfoDto
import de.gematik.test.erezept.primsys.data.info.TelematikInfoDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension

open class RestTest {

    @RegisterExtension
    val wiremockExtension = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build()

    lateinit var om: ObjectMapper

    @BeforeEach
    fun setup() {
        om = ObjectMapper()
    }

    fun setupPositiveInfo(env: String? = null): StubMapping {
        val infoDto = InfoDto()
        infoDto.doctors = 2
        infoDto.pharmacies = 2
        infoDto.build = BuildInfoDto()
        infoDto.fhir = FhirInfoDto()
        infoDto.ti = TelematikInfoDto()

        val info = om.writeValueAsString(infoDto)

        val testUrl = env?.let { "/$it/info" } ?: "/info"
        return stubFor(
            get(urlPathEqualTo(testUrl)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(info)
            )
        )
    }

    fun setupErrorInfo(errorType: ErrorType = ErrorType.INTERNAL): StubMapping {
        val error = om.writeValueAsString(ErrorDto(errorType, "internal error"))
        return stubFor(
            get(urlPathEqualTo("/info")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody(error)
            )
        )
    }

    fun setupPositiveActors(env: String? = null): StubMapping {
        val actorsDto = mutableListOf<ActorDto>()
        for (i in 0..1) actorsDto.add(ActorDto(ActorType.DOCTOR, "Doctor $i", "doc$i"))
        for (i in 0..1) actorsDto.add(ActorDto(ActorType.PHARMACY, "Pharmacy $i", "pharm$i"))

        val actors = om.writeValueAsString(actorsDto)
        val testUrl = env?.let { "/$it/actors" } ?: "/actors"
        return stubFor(
            get(urlPathEqualTo(testUrl)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(actors)
            )
        )
    }

    fun setupErrorActors(errorType: ErrorType = ErrorType.INTERNAL): StubMapping {
        val error = om.writeValueAsString(ErrorDto(errorType, "internal error"))
        return stubFor(
            get(urlPathEqualTo("/actors")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody(error)
            )
        )
    }

    fun setupErrorActorsHtml(): StubMapping {
        val error = "<html><body>internal error</body></html>"
        return stubFor(
            get(urlPathEqualTo("/actors")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "text/html").withBody(error)
            )
        )
    }

    fun setupErrorActorsEmpty(): StubMapping {
        return stubFor(
            get(urlPathEqualTo("/actors")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "application/json")
            )
        )
    }

    fun setupActorsInvalidErrorResponse(statusCode: Int): StubMapping {
        return stubFor(
            get(urlPathEqualTo("/actors")).willReturn(
                aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json")
                    .withHeader("content-length", "20").withBody("{'a': 'b'}")
            )
        )
    }

    fun setupActorsValidErrorResponse(): StubMapping {
        val errorJson = "{\"type\": \"INTERNAL\", \"message\": \"Test Message\"}"
        return stubFor(
            get(urlPathEqualTo("/actors")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", "application/json")
                    .withHeader("content-length", errorJson.length.toString()).withBody(errorJson)
            )
        )
    }

    fun setupConnectionTimeout(): StubMapping {
        return stubFor(
            get(urlPathEqualTo("/info")).willReturn(
                aResponse().withStatus(200).withFixedDelay(20000)
            )
        )
    }

    fun setupPositiveStubs(env: String? = null) {
        setupPositiveInfo(env)
        setupPositiveActors(env)
    }

    companion object {
        const val REST_PORT = 9099
    }
}