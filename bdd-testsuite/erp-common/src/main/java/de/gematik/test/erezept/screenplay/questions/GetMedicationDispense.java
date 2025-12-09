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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GetMedicationDispense implements Question<ErxMedicationDispenseBundle> {

  private final DequeStrategy deque;
  private final ActorRole role;

  @Override
  public ErxMedicationDispenseBundle answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    PrescriptionId prescriptionId;
    if (ActorRole.PATIENT.equals(role)) {
      val dispensed = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
      log.info(
          format(
              "{0} {1} looks for the {2} of the {3} received medications for refetching",
              role, actor.getName(), deque, dispensed.getDispensedDrugsList().size()));
      val dispensationInformation = deque.chooseFrom(dispensed.getDispensedDrugsList());
      prescriptionId = dispensationInformation.prescriptionId();
    } else if (ActorRole.PHARMACY.equals(role)) {
      val receipts = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
      log.info(
          format(
              "{0} {1} looks for the {2} of the {3} dispensed receipts",
              role, actor.getName(), deque, receipts.getReceiptsList().size()));
      val dispensed = deque.chooseFrom(receipts.getClosedPrescriptions());
      prescriptionId = dispensed.getPrescriptionId();
    } else {
      throw new FeatureNotImplementedException(format("Get MedicationDispense as {0}", role));
    }

    val cmd = new MedicationDispenseSearchByIdCommand(prescriptionId);
    val response = erpClient.request(cmd);
    return response.getExpectedResource();
  }

  public static Builder asPatient() {
    return as(ActorRole.PATIENT);
  }

  public static Builder asPharmacy() {
    return as(ActorRole.PHARMACY);
  }

  public static Builder as(ActorRole role) {
    return new Builder(role);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final ActorRole role;

    public GetMedicationDispense forPrescription(String order) {
      return forPrescription(DequeStrategy.fromString(order));
    }

    public GetMedicationDispense forPrescription(DequeStrategy deque) {
      return new GetMedicationDispense(deque, role);
    }
  }
}
