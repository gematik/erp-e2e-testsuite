/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.lei.integration;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.HasChargeItemBundle;
import de.gematik.test.erezept.screenplay.questions.HasDispensedDrugs;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DrugDispensationTest {

  private PrescriptionId prescriptionId;
  private Actor patient;
  private UseTheErpClient useMockClientAbility;
  private ProvideEGK egkAbility;
  private ReceiveDispensedDrugs dispensedDrugs;

  @BeforeEach
  void setUp() {
    this.prescriptionId = new PrescriptionId("123");
    this.useMockClientAbility = mock(UseTheErpClient.class);
    this.dispensedDrugs = new ReceiveDispensedDrugs();

    OnStage.setTheStage(Cast.ofStandardActors());
    patient = OnStage.theActorCalled("Marty");
    patient.can(useMockClientAbility);
    givenThat(patient).can(dispensedDrugs);

    val mockEgk = mock(Egk.class);
    when(mockEgk.getKvnr()).thenReturn("X123456789");
    egkAbility = ProvideEGK.sheOwns(mockEgk);
    givenThat(patient).can(egkAbility);
    givenThat(patient).can(ManageChargeItems.heReceives());
  }

  @Test
  void thenReceivedDrugsWithChargeItem() {
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withSubject(egkAbility.getKvnr(), GemFaker.insuranceName())
            .withReceipt(erxReceipt)
            .fake();
    val chargeItemBundle = mock(ErxChargeItemBundle.class);
    val response =
        ErpResponse.forPayload(chargeItemBundle, ErxChargeItemBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(chargeItemBundle.getChargeItem()).thenReturn(chargeItem);
    when(chargeItemBundle.getReceipt()).thenReturn(Optional.of(erxReceipt));

    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId, Instant.now());
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertTrue(then(patient).asksFor(HasChargeItemBundle.forLastDispensedDrug().asPatient()));
  }

  @Test
  void thenReceivedDrugsWithoutChargeItem() {
    val response =
        ErpResponse.forPayload(createOperationOutcome(), ErxChargeItemBundle.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId, Instant.now());
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertFalse(then(patient).asksFor(HasChargeItemBundle.forLastDispensedDrug().asPatient()));
  }
}
