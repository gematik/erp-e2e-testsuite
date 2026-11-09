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

package de.gematik.test.erezept.common.screenplay.abilities;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.cfg.ErpClientFactoryUtil;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.reports.AndContent;
import net.serenitybdd.core.reports.WithTitle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@Slf4j
class UseTheErpClientTest extends ErpFhirBuildingTest {

  private static final FhirCodec MOCK_FHIR_CODEC = mock(FhirCodec.class);
  private static final ErpClientFactory FACTORY =
      ErpClientFactoryUtil.createMockedErpClientFactory(MOCK_FHIR_CODEC);

  @BeforeAll
  static void setup() {
    when(MOCK_FHIR_CODEC.encode(any(), any(), eq(true))).thenReturn("mock fhir resource");
  }

  @Test
  void shouldReportRequestResponse() {
    val mockErpClient =
        FACTORY.prepareStagedClientFor(new PsActorConfiguration()).createWithSmcBAuthentication();

    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    val cmd = new ChargeItemPostCommand(chargeItem, Secret.from("123"));
    val expectedResponse = ErpResponseUtil.createErpResponse(chargeItem, ErxChargeItem.class, 201);

    when(mockErpClient.request(cmd)).thenReturn(expectedResponse);

    try (MockedStatic<Serenity> serenityMockedStatic = mockStatic(Serenity.class)) {
      val mockWithTitle = mock(WithTitle.class);
      val mockAndContent = mock(AndContent.class);
      serenityMockedStatic.when(Serenity::recordReportData).thenReturn(mockWithTitle);
      when(mockWithTitle.withTitle(anyString())).thenReturn(mockAndContent);

      val ability = UseTheErpClient.with(mockErpClient);
      val response = ability.request(cmd);
      val requestTitle = format("{0} {1}", cmd.getMethod(), cmd.getRequestLocator());

      verify(mockWithTitle, times(1)).withTitle(requestTitle);
      verify(mockWithTitle, times(3)).withTitle(anyString());
      verify(mockAndContent, times(2)).andContents("mock fhir resource");
    }
  }

  @Test
  void shouldForwardDecodeToClient() {
    val mockErpClient =
        FACTORY.prepareStagedClientFor(new PsActorConfiguration()).createWithSmcBAuthentication();

    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    when(MOCK_FHIR_CODEC.decode(eq(ErxChargeItem.class), any())).thenReturn(chargeItem);

    val ability = UseTheErpClient.with(mockErpClient);
    val ret = ability.decode(ErxChargeItem.class, "don't care");
    assertEquals(chargeItem, ret);

    verify(MOCK_FHIR_CODEC, times(1)).decode(ErxChargeItem.class, "don't care");
  }
}
