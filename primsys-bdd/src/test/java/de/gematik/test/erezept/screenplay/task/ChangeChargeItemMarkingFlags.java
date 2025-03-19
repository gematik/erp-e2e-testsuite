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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.client.usecases.ChargeItemPatchCommand;
import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import de.gematik.test.erezept.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.datatable.DataTable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeChargeItemMarkingFlags implements Task {

  private final DequeStrategy deque;
  private final MarkingFlag markingFlag;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val dispensedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val chargeItemsAbility = SafeAbility.getAbility(actor, ManageChargeItems.class);
    val dispensationInformation = deque.chooseFrom(dispensedDrugs.getDispensedDrugsList());
    val cmd = new ChargeItemPatchCommand(dispensationInformation.prescriptionId(), markingFlag);
    val response = erpClient.request(cmd);
    val chargeItem = response.getExpectedResource();
    chargeItemsAbility.update(chargeItem);
  }

  public static Builder forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static Builder forPrescription(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final DequeStrategy deque;

    public ChangeChargeItemMarkingFlags withDataTable(DataTable dataTable) {
      val map = dataTable.asMaps().get(0);
      val insurance = Boolean.parseBoolean(map.get("Versicherung"));
      val taxOffice = Boolean.parseBoolean(map.get("Finanzamt"));
      val subsidy = Boolean.parseBoolean(map.get("Beihilfe"));
      val flags = MarkingFlag.with(insurance, subsidy, taxOffice);
      return new ChangeChargeItemMarkingFlags(deque, flags);
    }
  }
}
