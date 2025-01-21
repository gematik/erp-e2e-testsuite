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
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@RequiredArgsConstructor
public class GetChargeItems extends ErpAction<ErxChargeItemSet> {

  private final ChargeItemGetCommand chargeItemGetCommand;

  public static GetChargeItems fromServerWith(ChargeItemGetCommand chargeItemGetCommand) {
    return new GetChargeItems(chargeItemGetCommand);
  }

  @Override
  @Step("{0} fragt den Fachdienst nach einem ChargeItem")
  public ErpInteraction<ErxChargeItemSet> answeredBy(Actor actor) {
    return this.performCommandAs(chargeItemGetCommand, actor);
  }
}
