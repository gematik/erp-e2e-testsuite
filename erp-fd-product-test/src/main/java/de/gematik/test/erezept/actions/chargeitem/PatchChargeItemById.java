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

package de.gematik.test.erezept.actions.chargeitem;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.ChargeItemPatchCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Parameters;

@RequiredArgsConstructor
public class PatchChargeItemById extends ErpAction<ErxChargeItem> {

  private final PrescriptionId prescriptionId;
  private final Parameters patchBody;

  public static Builder withPrescriptionId(PrescriptionId prescriptionId) {
    return new Builder(prescriptionId);
  }

  @Override
  public ErpInteraction<ErxChargeItem> answeredBy(Actor actor) {

    val cmd = new ChargeItemPatchCommand(prescriptionId, patchBody);

    return this.performCommandAs(cmd, actor);
  }

  @RequiredArgsConstructor
  public static class Builder {

    private final PrescriptionId prescriptionId;
    private Parameters patchBody;

    public Builder withPatchBody(Parameters patchBody) {
      this.patchBody = patchBody;
      return this;
    }

    public PatchChargeItemById build() {
      return new PatchChargeItemById(prescriptionId, patchBody);
    }
  }
}
