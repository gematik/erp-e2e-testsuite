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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.FlowTypeUtil;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.thucydides.core.annotations.Step;

@AllArgsConstructor
public class TaskCreate extends ErpAction<ErxTask> {

  private final PrescriptionFlowType flowType;

  @Override
  @Step("{0} erstellt einen neuen Task mit FlowType #flowType")
  public ErpInteraction<ErxTask> answeredBy(Actor actor) {
    val cmd = new TaskCreateCommand(flowType);
    return this.performCommandAs(cmd, actor);
  }

  public static TaskCreate withFlowType(PrescriptionFlowType flowType) {
    Object[] params = {flowType};
    return new Instrumented.InstrumentedBuilder<>(TaskCreate.class, params).newInstance();
  }

  public static Builder forPatient(PatientActor patient) {
    return new Builder(patient);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PatientActor patient;

    public TaskCreate ofAssignmentKind(PrescriptionAssignmentKind kind) {
      val insurance = patient.getInsuranceType();
      val flowType = FlowTypeUtil.getFlowType(null, insurance, kind);
      return TaskCreate.withFlowType(flowType);
    }
  }
}
