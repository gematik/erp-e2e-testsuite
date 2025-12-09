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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.erezept.remotefdv.api.model.Prescription;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class ReadPrescription implements Question<FdVResponse<Prescription>> {
  private final TaskId taskId;

  @Override
  @Step("{0} überprüft ob die Prescription vorhanden ist")
  public FdVResponse<Prescription> answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    return client.sendRequest(PatientRequests.getPrescriptionById(taskId.getValue()));
  }

  public static ReadPrescription withTaskId(TaskId taskId) {
    return Instrumented.instanceOf(ReadPrescription.class).withProperties(taskId);
  }
}
