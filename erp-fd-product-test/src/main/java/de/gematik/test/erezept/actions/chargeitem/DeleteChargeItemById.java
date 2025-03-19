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
 */

package de.gematik.test.erezept.actions.chargeitem;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.ChargeItemDeleteCommand;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@RequiredArgsConstructor
public class DeleteChargeItemById extends ErpAction<Resource> {
  private final PrescriptionId prescriptionId;

  public static DeleteChargeItemById withId(PrescriptionId prescriptionId) {
    return new DeleteChargeItemById(prescriptionId);
  }

  @Override
  public ErpInteraction<Resource> answeredBy(Actor actor) {
    val interaction = this.performCommandAs(new ChargeItemDeleteCommand(prescriptionId), actor);
    log.info(format("Deleted ChargeItem with ID: {0} !", prescriptionId));
    return interaction;
  }
}
