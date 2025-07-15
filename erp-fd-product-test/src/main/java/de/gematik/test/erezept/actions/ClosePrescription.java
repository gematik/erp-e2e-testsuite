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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationPZNBuilderORIGINAL_BUILDER;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class ClosePrescription extends ErpAction<ErxReceipt> {

  private final ErxAcceptBundle acceptBundle;
  @Nullable private final Date preparedDate;
  @Nullable private final Date handedOver;

  private final List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> fhirCloseMutators;

  @Override
  @Step("{0} dispensiert ein E-Rezept und schließt den Vorgang mit $close ab")
  public ErpInteraction<ErxReceipt> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();
    val prescriptionId = acceptBundle.getTask().getPrescriptionId();
    val kvnr = acceptBundle.getTask().getForKvnr().orElseThrow();
    val telematikId = SafeAbility.getAbility(actor, UseSMCB.class).getTelematikID();

    val kbvAsString = LocalVerifier.parse(acceptBundle.getSignedKbvBundle()).getDocument();
    val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);

    val gemOperationBuilder = GemOperationInputParameterBuilder.forClosingPharmaceuticals();
    val kbvMedication = kbvBundle.getMedication();
    val medication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedication)
            .lotNumber(GemFaker.fakerLotNumber())
            .build();

    val medicationDispenseBuilder =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .prescriptionId(prescriptionId)
            .performerId(telematikId)
            .medication(medication);

    if (preparedDate != null) {
      medicationDispenseBuilder.whenPrepared(preparedDate);
    }
    if (handedOver != null) {
      medicationDispenseBuilder.whenHandedOver(handedOver);
    }

    val medicationDispense = medicationDispenseBuilder.build();
    applyMutators(this.fhirCloseMutators, medicationDispense);

    val gemMedicationDispense = gemOperationBuilder.with(medicationDispense, medication).build();
    val cmd = new CloseTaskCommand(taskId, secret, gemMedicationDispense);

    return this.performCommandAs(cmd, actor);
  }

  static void applyMutators(
      List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> mutators,
      ErxMedicationDispense medicationDispense) {
    mutators.forEach(
        manipulator -> {
          Serenity.recordReportData().withTitle("Apply Mutator").andContents(manipulator.getName());
          manipulator.getParameter().accept(medicationDispense);
        });
  }

  public static ClosePrescription acceptedWith(ErpInteraction<ErxAcceptBundle> interaction) {
    return acceptedWith(interaction.getExpectedResponse());
  }

  public static ClosePrescription acceptedWith(ErxAcceptBundle acceptBundle) {
    return alternative().acceptedWith(acceptBundle);
  }

  public static Builder alternative() {
    return new Builder();
  }

  public static class Builder {
    private final List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> fhirCloseMutators =
        new LinkedList<>();

    public Builder performer(String telematikId) {
      return this.withResourceManipulator(
          NamedEnvelope.of(
              "Custom Telematik-ID: " + telematikId,
              medicationDispense ->
                  medicationDispense
                      .getPerformerFirstRep()
                      .getActor()
                      .getIdentifier()
                      .setValue(telematikId)));
    }

    public Builder prescriptionId(PrescriptionId prescriptionId) {
      return this.withResourceManipulator(
          NamedEnvelope.of(
              "Custom PrescriptionId: " + prescriptionId.getValue(),
              medicationDispense ->
                  medicationDispense.getIdentifierFirstRep().setValue(prescriptionId.getValue())));
    }

    public Builder kvnr(KVNR kvnr) {
      return this.withResourceManipulator(
          NamedEnvelope.of(
              "Custom KVNR: " + kvnr.getValue(),
              medicationDispense ->
                  medicationDispense.getSubject().setIdentifier(kvnr.asIdentifier())));
    }

    public Builder withResourceManipulator(
        NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> mutator) {
      this.fhirCloseMutators.add(mutator);
      return this;
    }

    public ClosePrescription acceptedWith(ErpInteraction<ErxAcceptBundle> interaction) {
      return acceptedWith(interaction.getExpectedResponse());
    }

    public ClosePrescription acceptedWith(ErxAcceptBundle acceptBundle) {
      return acceptedWith(acceptBundle, null, null);
    }

    public ClosePrescription acceptedWith(
        ErpInteraction<ErxAcceptBundle> interaction, Date prepareDate) {
      return acceptedWith(interaction.getExpectedResponse(), prepareDate, null);
    }

    public ClosePrescription acceptedWith(
        ErpInteraction<ErxAcceptBundle> interaction, Date prepareDate, Date handedOver) {
      return acceptedWith(interaction.getExpectedResponse(), prepareDate, handedOver);
    }

    public ClosePrescription acceptedWith(
        ErxAcceptBundle acceptBundle, Date prepareDate, Date handedOver) {
      Object[] params = {acceptBundle, prepareDate, handedOver, fhirCloseMutators};
      return new Instrumented.InstrumentedBuilder<>(ClosePrescription.class, params).newInstance();
    }
  }
}
