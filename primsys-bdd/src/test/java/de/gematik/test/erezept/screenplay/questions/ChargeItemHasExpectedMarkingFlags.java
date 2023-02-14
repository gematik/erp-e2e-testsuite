/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import io.cucumber.datatable.DataTable;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChargeItemHasExpectedMarkingFlags implements Question<Boolean> {

  private final DequeStrategy deque;
  private final Map<String, String> markingFlagsMap;

  @Override
  public Boolean answeredBy(Actor actor) {
    val getResponse = ResponseOfGetChargeItem.forPrescription(deque).asPatient().answeredBy(actor);
    val fdChargeItem = getResponse.getResource(ErxChargeItem.class);

    boolean ret = true;
    val insurance = Boolean.parseBoolean(markingFlagsMap.get("Versicherung"));
    val taxOffice = Boolean.parseBoolean(markingFlagsMap.get("Finanzamt"));
    val subsidy = Boolean.parseBoolean(markingFlagsMap.get("Beihilfe"));

    if (insurance != fdChargeItem.hasInsuranceProvider()) ret = false;

    if (taxOffice != fdChargeItem.hasTaxOffice()) ret = false;

    if (subsidy != fdChargeItem.hasSubsidy()) ret = false;

    return ret;
  }

  public static Builder fromDataTable(DataTable dataTable) {
    return new Builder(dataTable.asMap());
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final Map<String, String> markingFlagsMap;

    public ChargeItemHasExpectedMarkingFlags forPrescription(String order) {
      return forPrescription(DequeStrategy.fromString(order));
    }

    public ChargeItemHasExpectedMarkingFlags forPrescription(DequeStrategy deque) {
      return new ChargeItemHasExpectedMarkingFlags(deque, markingFlagsMap);
    }
  }
}
