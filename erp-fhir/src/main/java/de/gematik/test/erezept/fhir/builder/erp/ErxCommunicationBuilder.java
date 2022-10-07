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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class ErxCommunicationBuilder extends AbstractCommunicationBuilder<ErxCommunicationBuilder> {

  private String taskReference;
  private KbvErpMedication medication;
  private IKNR insuranceIknr;
  private String aboutReference;
  private boolean substitutionAllowed = true;
  private PrescriptionFlowType flowType;
  private AvailabilityStatus availabilityStatus;
  private SupplyOptionsType supplyOptionsType;

  private ErxCommunicationBuilder() {}

  public static ErxCommunicationBuilder builder() {
    return new ErxCommunicationBuilder();
  }

  public ErxCommunicationBuilder basedOnTaskId(@NonNull final String taskId) {
    this.taskReference = taskId.startsWith("Task/") ? taskId : "Task/" + taskId;
    return self();
  }

  public ErxCommunicationBuilder basedOnTask(
      @NonNull final String taskId, @NonNull final String accessCode) {
    basedOnTaskId(taskId + "/$accept?ac=" + accessCode);
    //    due to the AccessCode HAPI will cut Task/ (the leading resource)
    //    trick HAPI by providing '/' in front of the leading resource
    this.taskReference = "/" + this.taskReference;
    return self();
  }

  public ErxCommunicationBuilder basedOnTask(
      @NonNull final String taskId, @NonNull final AccessCode accessCode) {
    return basedOnTask(taskId, accessCode.getValue());
  }

  public ErxCommunicationBuilder medication(@NonNull final KbvErpMedication medication) {
    this.medication = medication;
    this.aboutReference = "#" + medication.getIdElement().getIdPart(); // about contained resource
    return self();
  }

  public ErxCommunicationBuilder insurance(@NonNull final IKNR iknr) {
    this.insuranceIknr = iknr;
    return self();
  }

  public ErxCommunicationBuilder substitution(boolean allowed) {
    this.substitutionAllowed = allowed;
    return self();
  }

  public ErxCommunicationBuilder flowType(PrescriptionFlowType flowType) {
    this.flowType = flowType;
    return self();
  }

  public ErxCommunicationBuilder availabilityStatus(AvailabilityStatus status) {
    this.availabilityStatus = status;
    return self();
  }

  public ErxCommunicationBuilder supplyOptions(SupplyOptionsType supply) {
    this.supplyOptionsType = supply;
    return self();
  }

  public ErxCommunication buildInfoReq(@NonNull final String message) {
    checkRequiredForInfoReq();
    val com = build(ErxCommunication.CommunicationType.INFO_REQ, message);

    com.addContained(medication);
    com.setAbout(List.of(new Reference(aboutReference)));

    val payload = com.getPayloadFirstRep();
    val insuranceExt =
        new Extension(
            ErpStructureDefinition.GEM_INSURANCE_PROVIDER.getCanonicalUrl(),
            insuranceIknr.asIdentifier());
    val substitutionExt =
        new Extension(
            ErpStructureDefinition.GEM_SUBSTITION_ALLOWED.getCanonicalUrl(),
            new BooleanType(substitutionAllowed));
    val prescriptionTypeExt =
        new Extension(
            ErpStructureDefinition.GEM_PRESCRIPTION_TYPE.getCanonicalUrl(),
            flowType.asCoding(true));

    payload.addExtension(insuranceExt);
    payload.addExtension(substitutionExt);
    payload.addExtension(prescriptionTypeExt);
    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildRepresentative(@NonNull final String message) {
    checkRequiredForRepresentative();
    val com = build(ErxCommunication.CommunicationType.REPRESENTATIVE, message);

    val payload = com.getPayloadFirstRep();
    val substitutionExt =
        new Extension(
            ErpStructureDefinition.GEM_SUBSTITION_ALLOWED.getCanonicalUrl(),
            new BooleanType(substitutionAllowed));
    val prescriptionTypeExt =
        new Extension(
            ErpStructureDefinition.GEM_PRESCRIPTION_TYPE.getCanonicalUrl(),
            flowType.asCoding(true));

    payload.addExtension(substitutionExt);
    payload.addExtension(prescriptionTypeExt);
    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildDispReq(@NonNull final String message) {
    checkRequiredForDispReq();
    val com = build(ErxCommunication.CommunicationType.DISP_REQ, message);
    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildReply(@NonNull final String message) {
    checkRequiredForReply();
    val com = build(ErxCommunication.CommunicationType.REPLY, message);

    val payload = com.getPayloadFirstRep();

    if (availabilityStatus != null) {
      val ext =
          new Extension(
              ErpStructureDefinition.GEM_AVAILABILITY_STATUS.getCanonicalUrl(),
              availabilityStatus.asCoding());
      payload.addExtension(ext);
    }

    if (supplyOptionsType == null) {
      supplyOptionsType = SupplyOptionsType.onPremise();
    }
    payload.addExtension(supplyOptionsType.asExtension());
    com.setBasedOn(List.of(new Reference(taskReference)));

    return com;
  }

  private void checkRequiredForTaskCommunication() {
    this.checkRequired(taskReference, "A Prescription Communication requires a Task-Reference");
  }

  private void checkRequiredForInfoReq() {
    checkRequiredForTaskCommunication();
    this.checkRequired(medication, "A InfoReq Communication requires a contained Medication");
    this.checkRequired(insuranceIknr, "A InfoReq Communication requires an IKNR of the Insurance");
    this.checkRequired(
        flowType, "A InfoReq Communication requires a Flow-Type of the Prescription");
  }

  private void checkRequiredForRepresentative() {
    checkRequiredForTaskCommunication();
    this.checkRequired(
        flowType, "A InfoReq Communication requires a Flow-Type of the Prescription");
  }

  private void checkRequiredForDispReq() {
    checkRequiredForTaskCommunication();
  }

  private void checkRequiredForReply() {
    checkRequiredForTaskCommunication();
  }
}
