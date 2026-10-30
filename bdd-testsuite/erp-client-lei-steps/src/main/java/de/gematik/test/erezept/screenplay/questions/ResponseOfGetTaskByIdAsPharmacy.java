/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdAsAcceptBundleCommand;
import de.gematik.test.erezept.common.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.questions.FhirResponseQuestion;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class ResponseOfGetTaskByIdAsPharmacy extends FhirResponseQuestion<ErxAcceptBundle> {
  private final DequeStrategy deque;

  public static ResponseOfGetTaskByIdAsPharmacy with(String deque) {
    return with(DequeStrategy.fromString(deque));
  }

  public static ResponseOfGetTaskByIdAsPharmacy with(DequeStrategy deque) {
    return new ResponseOfGetTaskByIdAsPharmacy(deque);
  }

  @Override
  public FhirBResponse<ErxAcceptBundle> answeredBy(Actor actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val dmc = deque.chooseFrom(prescriptionManager.getAssignedList());
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val cmd = new TaskGetByIdAsAcceptBundleCommand(dmc.getTaskId(), dmc.getAccessCode());
    return erpClient.request(cmd);
  }
}
