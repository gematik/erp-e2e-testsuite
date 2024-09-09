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

package de.gematik.test.erezept.actions.chargeitem;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class GetChargeItemById extends ErpAction<ErxChargeItemBundle> {
  private final PrescriptionId prescriptionId;
  @Nullable private final AccessCode accessCode;

  @Override
  public ErpInteraction<ErxChargeItemBundle> answeredBy(Actor actor) {
    ChargeItemGetByIdCommand cmd;
    if (accessCode != null) {
      cmd = new ChargeItemGetByIdCommand(prescriptionId, accessCode);
    } else {
      cmd = new ChargeItemGetByIdCommand(prescriptionId);
    }
    return this.performCommandAs(cmd, actor);
  }

  public static Builder withPrescriptionId(PrescriptionId prescriptionId) {
    return new Builder(prescriptionId);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PrescriptionId prescriptionId;

    public GetChargeItemById withoutAccessCode() {
      return new GetChargeItemById(prescriptionId, null);
    }

    public GetChargeItemById withAccessCode(AccessCode accessCode) {
      return new GetChargeItemById(prescriptionId, accessCode);
    }
  }
}
