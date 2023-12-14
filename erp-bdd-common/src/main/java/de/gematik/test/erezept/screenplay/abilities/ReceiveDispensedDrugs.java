/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import net.serenitybdd.screenplay.*;

public class ReceiveDispensedDrugs implements Ability {

  private final ManagedList<PrescriptionId> dispensedDrugs;

  public ReceiveDispensedDrugs() {
    this.dispensedDrugs = new ManagedList<>(() -> "No Drugs were dispensed so far");
  }

  public void append(PrescriptionId forPrescription) {
    dispensedDrugs.append(forPrescription);
  }

  public PrescriptionId getLastDispensedDrug() {
    return this.dispensedDrugs.getLast();
  }

  public PrescriptionId consumeLastDispensedDrug() {
    return this.dispensedDrugs.consumeLast();
  }

  public PrescriptionId getFirstDispensedDrug() {
    return this.dispensedDrugs.getFirst();
  }

  public PrescriptionId consumeFirstDispensedDrug() {
    return this.dispensedDrugs.consumeFirst();
  }

  public List<PrescriptionId> getDispensedDrugsList() {
    return this.dispensedDrugs.getRawList();
  }

  public static ReceiveDispensedDrugs forHimself() {
    return new ReceiveDispensedDrugs();
  }
}
