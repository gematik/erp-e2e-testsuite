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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.MedicationDispenseGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.time.LocalDate;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetMedicationDispenseTest extends ErpFhirBuildingTest {
  private static PharmacyActor pharmacist;
  private static PatientActor sina;

  @BeforeAll
  static void setup() {
    val useErpClient = mock(UseTheErpClient.class);
    pharmacist = new PharmacyActor("PhaMoc");
    pharmacist.can(useErpClient);

    sina = new PatientActor("sina");
    sina.can(useErpClient);
    val mockResponse =
        ErpResponse.forPayload(new ErxMedicationDispenseBundle(), ErxMedicationDispenseBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(MedicationDispenseGetCommand.class))).thenReturn(mockResponse);
  }

  @Test
  void shouldGetMedicationDispenseWhenHandedOver() {
    assertDoesNotThrow(
        () ->
            sina.performs(GetMedicationDispense.whenHandedOver(SearchPrefix.EQ, LocalDate.now())));
  }

  @Test
  void shouldGetMedicationDispenseWhenHandedOverDefault() {
    assertDoesNotThrow(() -> sina.performs(GetMedicationDispense.whenHandedOverDefault()));
  }

  @Test
  void shouldGetMedicationDispenseWhenPrepared() {
    assertDoesNotThrow(
        () -> sina.performs(GetMedicationDispense.whenPrepared(SearchPrefix.EQ, LocalDate.now())));
  }

  @Test
  void shouldGetMedicationDispenseFromPerformer() {
    assertDoesNotThrow(
        () -> sina.performs(GetMedicationDispense.fromPerformer(TelematikID.from("performerId"))));
  }

  @Test
  void shouldGetMedicationDispenseWithCount() {
    assertDoesNotThrow(() -> sina.performs(GetMedicationDispense.withCount(5)));
  }

  @Test
  void shouldGetMedicationDispenseQueryParams() {
    assertDoesNotThrow(
        () ->
            sina.performs(
                GetMedicationDispense.withQueryParams(
                    IQueryParameter.search()
                        .withCount(5)
                        .withOffset(10)
                        .sortedBy("date", SortOrder.ASCENDING)
                        .createParameter())));
  }
}
