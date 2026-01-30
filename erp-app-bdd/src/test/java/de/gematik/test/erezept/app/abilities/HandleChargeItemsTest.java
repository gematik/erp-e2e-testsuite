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

package de.gematik.test.erezept.app.abilities;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsentBundle;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class HandleChargeItemsTest {

  @Test
  void shouldNotThrow() {
    val alice = new Actor("Alice");

    val providePatientBaseData = mock(ProvidePatientBaseData.class);
    val useTheErpClient = mock(UseTheErpClient.class);

    val erxConsentBundle = mock(ErxConsentBundle.class);
    val erpResponse =
        ErpResponse.forPayload(erxConsentBundle, ErxConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useTheErpClient.request(any(ConsentGetCommand.class))).thenReturn(erpResponse);

    givenThat(alice).can(providePatientBaseData);
    givenThat(alice).can(useTheErpClient);

    val handleChargeItems = HandleChargeItems.forHerself();

    assertInstanceOf(HandleChargeItems.class, handleChargeItems);
    assertDoesNotThrow(handleChargeItems::toString);
    assertDoesNotThrow(() -> handleChargeItems.asActor(alice));
    assertDoesNotThrow(handleChargeItems::tearDown);
  }
}
