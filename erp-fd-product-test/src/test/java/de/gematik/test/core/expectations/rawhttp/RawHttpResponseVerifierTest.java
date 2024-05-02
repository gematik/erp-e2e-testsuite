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

package de.gematik.test.core.expectations.rawhttp;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.test.core.expectations.rawhttpverifier.RawHttpResponseVerifier;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetOcspListResponse;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import java.util.ArrayList;
import java.util.List;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest()
class RawHttpResponseVerifierTest {

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  private final ErpAfos unspecificAfo = ErpAfos.A_19514_02;
  private HttpResponse<?> response;
  private Actor actor;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");
    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPList"))
            .willReturn(
                aResponse().withHeader("1", "2").withBody("123456_TestBody").withStatus(200)));
    actor = OnStage.theActor("Leonie");
    UnirestInstance client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new RawHttpAbility(client));
    response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldNotInstantiateTaskVerifier() {
    assertTrue(
        PrivateConstructorsUtil.throwsInvocationTargetException(RawHttpResponseVerifier.class));
  }

  @Test
  void shouldVerifyHeaderContent() {
    val step = RawHttpResponseVerifier.containsHeaderWith("1", "2", unspecificAfo);
    assertDoesNotThrow(() -> step.apply(response));
  }

  @Test
  void shouldThrowWhileVerifyHeaderContent() {
    val step = RawHttpResponseVerifier.containsHeaderWith("1", "Wrong Value", unspecificAfo);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void shouldVerifyBody() {
    val step = RawHttpResponseVerifier.stringBodyContains("123456_TestBody", unspecificAfo);
    assertDoesNotThrow(() -> step.apply((String) response.getBody()));
  }

  @Test
  void shouldThrowExceptionWhileVerifyBody() {
    val step = RawHttpResponseVerifier.stringBodyContains("1234567890", unspecificAfo);
    val responseBody = (String) response.getBody();
    assertThrows(AssertionError.class, () -> step.apply(responseBody));
  }

  @Test
  void shouldVerifyReturnCodeWithAfo() {
    val step = RawHttpResponseVerifier.returnCode(200, unspecificAfo);
    assertDoesNotThrow(() -> step.apply(response));
  }

  @Test
  void shouldVerifyReturnCode() {
    val step = RawHttpResponseVerifier.returnCode(200);
    assertDoesNotThrow(() -> step.apply(response));
  }

  @Test
  void shouldThrowExceptionWhileVerifyReturnCodeWithAfo() {
    val step = RawHttpResponseVerifier.returnCode(201, unspecificAfo);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void shouldThrowExceptionWhileVerifyReturnCode() {
    val step = RawHttpResponseVerifier.returnCode(201);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void shouldThrowWhileMissingReturnCode() {
    val emptyList = new ArrayList<Integer>();
    assertThrows(
        IllegalArgumentException.class,
        () -> RawHttpResponseVerifier.returnCodeIsIn(emptyList, unspecificAfo));
  }

  @Test
  void shouldWorkWithListOfReturnCodesBigger10() {

    val step =
        RawHttpResponseVerifier.returnCodeIsIn(
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 200, 500), unspecificAfo);
    assertDoesNotThrow(() -> step.apply(response));
  }
}
