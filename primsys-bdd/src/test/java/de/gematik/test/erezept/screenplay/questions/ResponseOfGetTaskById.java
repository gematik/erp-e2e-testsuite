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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdAsAcceptBundleCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfGetTaskById {

  public static ResponseOfGetTaskByIdAsPatient asPatient(String deque) {
    return asPatient(DequeStrategy.fromString(deque));
  }

  public static ResponseOfGetTaskByIdAsPatient asPatient(DequeStrategy deque) {
    return new ResponseOfGetTaskByIdAsPatient(deque);
  }

  public static ResponseOfGetTaskByIdAsPharmacy asPharmacy(String deque) {
    return asPharmacy(DequeStrategy.fromString(deque));
  }

  public static ResponseOfGetTaskByIdAsPharmacy asPharmacy(DequeStrategy deque) {
    return new ResponseOfGetTaskByIdAsPharmacy(deque);
  }

  public static class ResponseOfGetTaskByIdAsPharmacy
      extends FhirResponseQuestion<ErxAcceptBundle> {

    private final DequeStrategy deque;

    private ResponseOfGetTaskByIdAsPharmacy(DequeStrategy deque) {
      this.deque = deque;
    }

    @Override
    public ErpResponse<ErxAcceptBundle> answeredBy(Actor actor) {
      val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
      val dmc = deque.chooseFrom(prescriptionManager.getAssignedList());
      val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
      val cmd = new TaskGetByIdAsAcceptBundleCommand(dmc.getTaskId(), dmc.getAccessCode());
      return erpClient.request(cmd);
    }
  }

  public static class ResponseOfGetTaskByIdAsPatient
      extends FhirResponseQuestion<ErxPrescriptionBundle> {

    private final DequeStrategy deque;

    private ResponseOfGetTaskByIdAsPatient(DequeStrategy deque) {
      this.deque = deque;
    }

    @Override
    public ErpResponse<ErxPrescriptionBundle> answeredBy(Actor actor) {
      val dmcManager = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
      val dmc = deque.chooseFrom(dmcManager.getDmcList());
      val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
      val cmd = new TaskGetByIdCommand(dmc.getTaskId(), dmc.getAccessCode());
      return erpClient.request(cmd);
    }
  }
}
