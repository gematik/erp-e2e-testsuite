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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseDiGAFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DispenseDigaPrescription extends FhirResponseQuestion<ErxMedicationDispenseBundle> {
  private final DequeStrategy order;

  public static DispenseDigaPrescription forThePrescription(String order) {
    return new DispenseDigaPrescription(DequeStrategy.fromString(order));
  }

  @Override
  public ErpResponse<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionPharmacyManager =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val acceptBundle = order.chooseFrom(prescriptionPharmacyManager.getAcceptedPrescriptions());
    val evdgaBundleAsString = LocalVerifier.parse(acceptBundle.getSignedKbvBundle()).getDocument();
    val evdgaBundle = erpClient.decode(KbvEvdgaBundle.class, evdgaBundleAsString);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val pzn = evdgaBundle.getHealthAppRequest().getPzn();

    // build dispensation
    val md =
        ErxMedicationDispenseDiGAFaker.builder()
            .withKvnr(evdgaBundle.getPatient().getKvnr())
            .withPrescriptionId(acceptBundle.getTaskId().toPrescriptionId())
            .withPerformer(smcb.getTelematikID())
            .withPzn(pzn.getValue(), evdgaBundle.getHealthAppRequest().getName())
            .fake();
    val operationBuilder = GemOperationInputParameterBuilder.forClosingDiGA();
    val operationParams = operationBuilder.with(md).build();

    // $dispense without $close should fail by A_24279
    val dDP = DigaDispenseParameters.fromCloseParameters(operationParams);
    val cmd =
        new DispensePrescriptionAsBundleCommand(
            acceptBundle.getTask().getTaskId(), acceptBundle.getSecret(), dDP);

    log.info(
        "Actor {} trying to $dispense a DiGA prescription {} with PZN {} should fail. Caused by"
            + " A_24279",
        actor.getName(),
        acceptBundle.getTask().getTaskId(),
        pzn.getValue());

    return erpClient.request(cmd);
  }

  /**
   * !!! This class is an extension and can only be used in this position. Normally, it is NOT
   * permitted to dispense a DIGA as a KTR via $dispense
   */
  public static class DigaDispenseParameters extends GemDispenseOperationParameters {
    public static DigaDispenseParameters fromCloseParameters(
        GemCloseOperationParameters gemCloseOperationParameters) {
      val dDP = new DigaDispenseParameters();
      gemCloseOperationParameters.copyValues(dDP);
      return dDP;
    }
  }
}
