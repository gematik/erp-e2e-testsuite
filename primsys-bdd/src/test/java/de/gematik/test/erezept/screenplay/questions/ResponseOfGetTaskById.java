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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdAsAcceptBundleCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfGetTaskById extends FhirResponseQuestion<ErxAcceptBundle> {

  private final ActorRole role;
  private final DequeStrategy deque;

  private ResponseOfGetTaskById(ActorRole role, DequeStrategy deque) {
    super("GetTaskById");
    this.role = role;
    this.deque = deque;
  }

  public static ResponseOfGetTaskById asPharmacy(String deque) {
    return new ResponseOfGetTaskById(ActorRole.PHARMACY, DequeStrategy.fromString(deque));
  }

  public static ResponseOfGetTaskById withActorRole(ActorRole actor, DequeStrategy deque) {
    return new ResponseOfGetTaskById(actor, (deque));
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
