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
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommandWithoutSecret;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationPZNBuilderORIGINAL_BUILDER;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Date;
import java.util.LinkedList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfDispenseMedicationWithoutAcceptance implements Question<Boolean> {

  private final DequeStrategy order;

  private final Actor patient;

  private final int returnCode;

  public static Builder forPatient(Actor patient) {
    return new Builder(patient);
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val dmcPrescription = order.chooseFrom(prescriptionManager.getAssignedList());
    ErpResponse<ErxMedicationDispenseBundle> resp;
    val client = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val bundle =
        KbvErpBundleFaker.builder()
            .withPrescriptionId(PrescriptionId.from(dmcPrescription.getTaskId()))
            .withKvnr(SafeAbility.getAbility(patient, ProvideEGK.class).getKvnr())
            .fake();

    val lotNr = GemFaker.fakerLotNumber();
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(bundle.getMedication())
            .lotNumber(lotNr)
            .build();

    val medicationDisp =
        ErxMedicationDispenseBuilder.forKvnr(bundle.getPatient().getKvnr())
            .prescriptionId(bundle.getPrescriptionId())
            .performerId(SafeAbility.getAbility(actor, UseSMCB.class).getTelematikID())
            .whenHandedOver(new Date())
            .medication(gemMedication)
            .batch(lotNr, GemFaker.fakerFutureExpirationDate())
            .wasSubstituted(false)
            .build();
    val closeParams =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .with(medicationDisp, gemMedication)
            .build();
    resp =
        client.request(
            new DispensePrescriptionAsBundleCommandWithoutSecret(
                dmcPrescription.getTaskId(), closeParams));

    val checkResults = new LinkedList<Boolean>();
    checkResults.add(resp.isOperationOutcome());
    if (returnCode > 0) checkResults.add(resp.getStatusCode() == returnCode);

    return (!checkResults.contains(false));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final Actor patient;
    private int returnCode = 0;

    public Builder withReturnCode(int returnCode) {
      this.returnCode = returnCode;
      return this;
    }

    public ResponseOfDispenseMedicationWithoutAcceptance forPrescription(DequeStrategy order) {
      return new ResponseOfDispenseMedicationWithoutAcceptance(order, patient, returnCode);
    }
  }
}
