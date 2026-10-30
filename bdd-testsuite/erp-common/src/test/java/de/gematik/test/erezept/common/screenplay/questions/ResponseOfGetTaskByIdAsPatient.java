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

package de.gematik.test.erezept.common.screenplay.questions;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class ResponseOfGetTaskByIdAsPatient extends FhirResponseQuestion<ErxPrescriptionBundle> {
  private final DequeStrategy deque;

  public static ResponseOfGetTaskByIdAsPatient with(String deque) {
    return with(DequeStrategy.fromString(deque));
  }

  public static ResponseOfGetTaskByIdAsPatient with(DequeStrategy deque) {
    return new ResponseOfGetTaskByIdAsPatient(deque);
  }

  @Override
  public FhirBResponse<ErxPrescriptionBundle> answeredBy(Actor actor) {
    val dmcManager = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcManager.getDmcList());
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val cmd = new TaskGetByIdCommand(dmc.getTaskId(), dmc.getAccessCode());
    return erpClient.request(cmd);
  }
}
