/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HasRetrieved implements Question<Boolean> {

  private final Predicate<ManagePharmacyPrescriptions> predict;

  @Override
  public Boolean answeredBy(Actor pharmacy) {
    val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
    return predict.test(prescriptionManager);
  }

  public static HasRetrieved theLastAcceptedPrescriptionBy(Actor pharmacy) {
    val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
    val lastAcceptedPrescription = prescriptionManager.getLastAcceptedPrescription();
    val dmcPrescription =
        DmcPrescription.ownerDmc(
            lastAcceptedPrescription.getTaskId(),
            lastAcceptedPrescription.getTask().getAccessCode());

    return new HasRetrieved(
        l -> l.getAssignedPrescriptions().getRawList().contains(dmcPrescription));
  }
}
