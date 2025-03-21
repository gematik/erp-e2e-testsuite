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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;

public abstract class ErxComPrescriptionBuilder<B extends ErxCommunicationBuilder<B>>
    extends ErxCommunicationBuilder<B> {

  protected IKNR insuranceIknr;
  protected PrescriptionFlowType flowType;
  protected KbvErpMedication medication;
  protected String aboutReference;
  protected AvailabilityStatus availabilityStatus;
  protected SupplyOptionsType supplyOptionsType = SupplyOptionsType.createDefault();
  protected boolean substitutionAllowed = true;

  protected ErxComPrescriptionBuilder(String message) {
    super(message);
    this.erpWorkflowVersion = ErpWorkflowVersion.getDefaultVersion();
  }

  public B flowType(PrescriptionFlowType flowType) {
    this.flowType = flowType;
    return self();
  }

  public B availabilityStatus(AvailabilityStatus status) {
    this.availabilityStatus = status;
    return self();
  }

  public B supplyOptions(SupplyOptionsType supply) {
    this.supplyOptionsType = supply;
    return self();
  }

  public B substitution(boolean allowed) {
    this.substitutionAllowed = allowed;
    return self();
  }

  public B insurance(IKNR iknr) {
    this.insuranceIknr = iknr;
    return self();
  }

  public B medication(KbvErpMedication medication) {
    this.medication = medication;
    this.aboutReference = "#" + medication.getIdElement().getIdPart(); // about contained resource
    return self();
  }

  public B basedOn(TaskId taskId) {
    return basedOn(taskId.getValue());
  }

  public B basedOn(String taskId) {
    this.baseOnReference = taskId.startsWith("Task/") ? taskId : "Task/" + taskId;
    return self();
  }

  public B basedOn(TaskId taskId, AccessCode accessCode) {
    return basedOn(taskId.getValue(), accessCode.getValue());
  }

  public B basedOn(String taskId, String accessCode) {
    basedOn(taskId + "/$accept?ac=" + accessCode);
    //    due to the AccessCode HAPI will cut Task/ (the leading resource)
    //    trick HAPI by providing '/' in front of the leading resource
    this.baseOnReference = "/" + this.baseOnReference;
    return self();
  }
}
