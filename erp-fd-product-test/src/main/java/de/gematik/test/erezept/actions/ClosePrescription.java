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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class ClosePrescription extends ErpAction<ErxReceipt> {

  private final ErxAcceptBundle acceptBundle;
  @Nullable private final Date preparedDate;
  @Nullable private final Date handedOver;
  private final Map<String, Object> manipulator;

  @Override
  @Step("{0} dispensiert ein E-Rezept und schließt den Vorgang mit $close ab")
  public ErpInteraction<ErxReceipt> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val taskId = acceptBundle.getTaskId();
    val secret = acceptBundle.getSecret();
    val kbvAsString = acceptBundle.getKbvBundleAsString();
    val kbvBundle = erpClient.decode(KbvErpBundle.class, kbvAsString);
    val medication = kbvBundle.getMedication();
    val prescriptionId =
        (PrescriptionId)
            manipulator.getOrDefault("prescriptionId", acceptBundle.getTask().getPrescriptionId());
    val kvnr =
        (KVNR)
            manipulator.getOrDefault(
                "kvnr",
                acceptBundle
                    .getTask()
                    .getForKvnr()
                    .orElseThrow()); // why is this an optional at all?
    val telematikId =
        (String)
            manipulator.getOrDefault(
                "telematikId", SafeAbility.getAbility(actor, UseSMCB.class).getTelematikID());

    val medicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .prescriptionId(prescriptionId)
            .performerId(telematikId)
            .medication(medication);
    if (preparedDate != null) medicationDispense.whenPrepared(preparedDate);
    if (handedOver != null) medicationDispense.whenHandedOver(handedOver);
    val cmd = new CloseTaskCommand(taskId, secret, medicationDispense.build());
    return this.performCommandAs(cmd, actor);
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

    private final Map<String, Object> alternatives = new HashMap<>();

    public Builder performer(String telematikId) {
      alternatives.put("telematikId", telematikId);
      return this;
    }

    public Builder prescriptionId(PrescriptionId prescriptionId) {
      alternatives.put("prescriptionId", prescriptionId);
      return this;
    }

    public Builder kvnr(KVNR kvnr) {
      alternatives.put("kvnr", kvnr);
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
      Object[] params = {acceptBundle, prepareDate, handedOver, alternatives};
      return new Instrumented.InstrumentedBuilder<>(ClosePrescription.class, params).newInstance();
    }
  }
}
