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

package de.gematik.test.core.expectations;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.extensions.ErpTestExtension;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetOcspListResponse;
import java.util.List;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.thucydides.core.steps.StepEventBus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(ErpTestExtension.class)
@WireMockTest()
class HttpResponseExpectationTest {

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  private Actor actor;
  private UnirestInstance client;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
    actor = OnStage.theActor("Leonie");
    client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new RawHttpAbility(client));
  }

  @AfterEach
  void clearUp() {
    OnStage.drawTheCurtain();
  }

  @Test
  @TestcaseId("ut_httpExpectation_01")
  @DisplayName("Positive Unit Test for an Expectation")
  void shouldHasResponseWith() {
    val actor = OnStage.theActor("Leonie");
    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPList")).willReturn(aResponse().withHeader("1", "2").withStatus(200)));

    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
    val exp =
        HttpResponseExpectation.expectFor(response, String.class)
            .responseWith(containsHeaderWith("1", "2", ErpAfos.A_19230))
            .hasResponseWith(containsHeaderWith("1", "2", ErpAfos.A_19230));
    exp.ensure();
  }

  @Test()
  @TestcaseId("ut_httpExpectation_02")
  @DisplayName("Positive Unit Test for an Expectation")
  void shouldHasResponseWithReturnCode() {
    val actor = OnStage.theActor("Leonie");
    wiremockExtension.stubFor(get(urlEqualTo("/OCSPList")).willReturn(aResponse().withStatus(200)));
    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
    val expectation =
        HttpResponseExpectation.expectFor(response)
            .hasResponseWith(returnCode(200))
            .andResponse(containsHeaderWith("1", "TestValue2", ErpAfos.A_19248))
            .toString();
    assertTrue(expectation.contains("(A_19514-03; A_19248-02)"));
  }

  @Test()
  @TestcaseId("ut_httpExpectation_03")
  @DisplayName("Positive and negative Unit Test for an Expectation")
  void shouldThrowException() {
    val actor = OnStage.theActor("Leonie");
    wiremockExtension.stubFor(get(urlEqualTo("/OCSPList")).willReturn(aResponse().withStatus(210)));
    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
    val expectation =
        HttpResponseExpectation.expectFor(response, String.class)
            .hasResponseWith(returnCode(200))
            .responseWith(returnCodeIs(201))
            .andResponse(returnCodeIsIn(List.of(202, 203, 204)));

    StepEventBus.getParallelEventBus().disableSoftAsserts();
    assertThrows(AssertionError.class, expectation::ensure);
    int[] list = {1, 210};
    val expectation210 =
        HttpResponseExpectation.expectFor(response).hasResponseWith(returnCodeIsIn(list));
    assertDoesNotThrow(expectation210::ensure);
  }

  @Test()
  @TestcaseId("ut_httpExpectation_03")
  @DisplayName("Unit Test for an Expectation.toString")
  void shouldBuildString() {
    val actor = OnStage.theActor("Leonie");
    wiremockExtension.stubFor(get(urlEqualTo("/OCSPList")).willReturn(aResponse().withStatus(210)));
    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
    val expectation = HttpResponseExpectation.expectFor(response).hasResponseWith(returnCode(200));
    val res = expectation.toString();
    assertTrue(res.contains("Erwartung (A_"));
    val secondExp = HttpResponseExpectation.expectFor(response, String.class).toString();
    assertTrue(secondExp.contains("keine Anforderung"));
  }
}
