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

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.lei.exceptions.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HasChargeItem implements Question<Boolean> {

  private final DequeStrategy deque;
  private final ActorRole role;

  @Override
  public Boolean answeredBy(final Actor actor) {
    val response = actor.asksFor(ResponseOfGetChargeItem.forPrescription(deque).withRole(role));
    if (response.getResourceOptional(ErxChargeItemBundle.class).isEmpty()) {
      return false; // unexpected response
    }

    if (role == ActorRole.PATIENT) {
      return checkAsPatient(actor, response);
    } else if (role == ActorRole.PHARMACY) {
      return checkAsPharmacy(actor, response);
    } else {
      throw new InvalidActorRoleException(format("Cannot check ChargeItem as {0}", role));
    }
  }

  private Boolean checkAsPatient(Actor actor, ErpResponse response) {
    val chargeItemBundle = response.getResource(ErxChargeItemBundle.class);
    val chargeItem = chargeItemBundle.getChargeItem();

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class);
    // store the fetched charge item on local stack
    val chargeItemAbility = SafeAbility.getAbility(actor, ManageChargeItems.class);
    chargeItemAbility.update(chargeItem);

    if (chargeItem.getReceiptReference().isEmpty() || chargeItemBundle.getReceipt().isEmpty()) {
      return false;
    }

    return chargeItem.getSubjectKvid().equals(egk.getKvnr());
  }

  private Boolean checkAsPharmacy(Actor actor, ErpResponse response) {
    val chargeItemBundle = response.getResource(ErxChargeItemBundle.class);
    val chargeItem = chargeItemBundle.getChargeItem();

    val pharmacyStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val dispensed = deque.chooseFrom(pharmacyStack.getDispensedPrescriptions());

    if (!chargeItem.getPrescriptionId().equals(dispensed.getPrescriptionId())) {
      return false;
    }

    if (chargeItem.getReceiptReference().isPresent() || chargeItemBundle.getReceipt().isPresent()) {
      return false;
    }

    if (chargeItem.getAccessCode().isPresent()) {
      return false;
    }

    return chargeItem.getEntererTelematikId().equals(smcb.getTelematikID());
  }

  public static Builder forLastDispensedDrug() {
    return new Builder(DequeStrategy.LIFO);
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

    public HasChargeItem asPatient() {
      return withRole(ActorRole.PATIENT);
    }

    public HasChargeItem asPharmacy() {
      return withRole(ActorRole.PHARMACY);
    }

    public HasChargeItem withRole(ActorRole role) {
      return new HasChargeItem(deque, role);
    }
  }
}
