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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

public class ManageChargeItems implements Ability {

  @Getter private final ManagedList<ErxChargeItem> chargeItems;

  private ManageChargeItems() {
    this.chargeItems = new ManagedList<>(() -> "No Charge Items were downloaded so far");
  }

  public static ManageChargeItems sheReceives() {
    return new ManageChargeItems();
  }

  public Optional<ErxChargeItem> getChargeItem(PrescriptionId prescriptionId) {
    return chargeItems.getRawList().stream()
        .filter(ci -> ci.getPrescriptionId().equals(prescriptionId))
        .findFirst();
  }

  public void remove(PrescriptionId prescriptionId) {
    val existing = getChargeItem(prescriptionId);
    existing.ifPresent(ci -> chargeItems.getRawList().remove(ci));
  }

  public void update(ErxChargeItem chargeItem) {
    this.remove(chargeItem.getPrescriptionId());
    chargeItems.append(chargeItem);
  }
}
