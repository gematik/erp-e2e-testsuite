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

package de.gematik.test.erezept.actions.rawhttp;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetOcspListResponse;
import kong.unirest.core.Unirest;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest()
class GetOcspListResponseTest {
  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @AfterEach
  void clearUp() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldGetResponse() {
    val actor = OnStage.theActor("Leonie");
    val client = Unirest.spawnInstance();
    client
        .config()
        .defaultBaseUrl(wiremockExtension.baseUrl())
        .addDefaultHeader("X-api-key", "123456789abc");
    val rawHttpAbility = new RawHttpAbility(client);
    actor.can(rawHttpAbility);
    wiremockExtension.stubFor(get(urlEqualTo("/OCSPList")).willReturn(aResponse().withStatus(200)));
    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
    assertEquals(200, response.getStatus());
  }
}
