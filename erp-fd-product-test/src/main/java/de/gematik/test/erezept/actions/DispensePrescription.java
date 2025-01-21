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
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.resources.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DispensePrescription extends ErpAction<ErxMedicationDispenseBundle> {

  private final DispensePrescriptionAsBundleCommand dispensePrescriptionAsBundleCommand;

  public static Builder forPrescription(TaskId taskId, Secret secret) {
    return new Builder(taskId, secret);
  }

  @Override
  @Step("{0} dispensiert ein E-Rezept mit #taskId und #accessCode ohne close")
  public ErpInteraction<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    return this.performCommandAs(dispensePrescriptionAsBundleCommand, actor);
  }

  @AllArgsConstructor
  public static class Builder {
    private final TaskId taskId;
    private final Secret secret;

    public DispensePrescription withMedDsp(List<ErxMedicationDispense> erxMedicationDispenses) {
      val cmd = new DispensePrescriptionAsBundleCommand(taskId, secret, erxMedicationDispenses);
      return new DispensePrescription(cmd);
    }

    public DispensePrescription withParameters(GemDispenseOperationParameters params) {
      val cmd = new DispensePrescriptionAsBundleCommand(taskId, secret, params);
      return new DispensePrescription(cmd);
    }
  }
}
