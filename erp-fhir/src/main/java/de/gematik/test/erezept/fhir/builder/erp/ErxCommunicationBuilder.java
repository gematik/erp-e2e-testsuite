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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

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
    val type = CommunicationType.INFO_REQ;

    ErxCommunication com;
    ErpWorkflowStructDef insuranceProvider;
    ErpWorkflowStructDef substitutionAllowedExt;
    ErpWorkflowStructDef prescriptionType;
    Identifier insuranceIdentifier;
    Coding flowTypeCoding;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = build(type, () -> type.getType().asCanonicalType(), message);
      insuranceProvider = ErpWorkflowStructDef.INSURANCE_PROVIDER;
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE;
      insuranceIdentifier = insuranceIknr.asIdentifier();
      flowTypeCoding = flowType.asCoding(true);
    } else {
      com =
          build(
              type,
              () -> ErpWorkflowStructDef.COM_INFO_REQ_12.asCanonicalType(erpWorkflowVersion, true),
              message);
      insuranceProvider = ErpWorkflowStructDef.INSURANCE_PROVIDER_12;
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED_12;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE_12;
      insuranceIdentifier = insuranceIknr.asIdentifier(DeBasisNamingSystem.IKNR_SID);
      // hacky but should work fow now!
      flowTypeCoding =
          flowType.asCoding(true).setSystem(ErpWorkflowCodeSystem.FLOW_TYPE_12.getCanonicalUrl());
    }

    com.addContained(medication);
    com.setAbout(List.of(new Reference(aboutReference)));

    val payload = com.getPayloadFirstRep();

    val insuranceExt = new Extension(insuranceProvider.getCanonicalUrl(), insuranceIdentifier);
    val substitutionExt =
        new Extension(
            substitutionAllowedExt.getCanonicalUrl(), new BooleanType(substitutionAllowed));
    val prescriptionTypeExt = new Extension(prescriptionType.getCanonicalUrl(), flowTypeCoding);

    payload.addExtension(insuranceExt);
    payload.addExtension(substitutionExt);
    payload.addExtension(prescriptionTypeExt);
    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildRepresentative(@NonNull final String message) {
    checkRequiredForRepresentative();
    val type = CommunicationType.REPRESENTATIVE;
    ErxCommunication com;
    ErpWorkflowStructDef substitutionAllowedExt;
    ErpWorkflowStructDef prescriptionType;
    Coding flowTypeCoding;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = build(type, () -> type.getType().asCanonicalType(), message);
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE;
      flowTypeCoding = flowType.asCoding(true);
    } else {
      com =
          build(
              type,
              () ->
                  ErpWorkflowStructDef.COM_REPRESENTATIVE_12.asCanonicalType(
                      erpWorkflowVersion, true),
              message);
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED_12;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE_12;
      // hacky but should work fow now!
      flowTypeCoding =
          flowType.asCoding(true).setSystem(ErpWorkflowCodeSystem.FLOW_TYPE_12.getCanonicalUrl());
    }

    val payload = com.getPayloadFirstRep();
    val substitutionExt =
        new Extension(
            substitutionAllowedExt.getCanonicalUrl(), new BooleanType(substitutionAllowed));
    val prescriptionTypeExt = new Extension(prescriptionType.getCanonicalUrl(), flowTypeCoding);

    payload.addExtension(substitutionExt);
    payload.addExtension(prescriptionTypeExt);
    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildDispReq(@NonNull final String message) {
    checkRequiredForDispReq();
    val type = CommunicationType.DISP_REQ;

    ErxCommunication com;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = build(type, () -> type.getType().asCanonicalType(), message);
    } else {
      com =
          build(
              type,
              () -> ErpWorkflowStructDef.COM_DISP_REQ_12.asCanonicalType(erpWorkflowVersion, true),
              message);
    }

    com.setBasedOn(List.of(new Reference(taskReference)));
    return com;
  }

  public ErxCommunication buildReply(@NonNull final String message) {
    checkRequiredForReply();
    val type = CommunicationType.REPLY;
    ErxCommunication com;
    ErpWorkflowStructDef availabilityStatusStructDef;
    ErpWorkflowCodeSystem availabilityCodeSystem;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = build(type, () -> type.getType().asCanonicalType(), message);
      availabilityStatusStructDef = ErpWorkflowStructDef.AVAILABILITY_STATUS;
      availabilityCodeSystem = ErpWorkflowCodeSystem.AVAILABILITY_STATUS;
    } else {
      com =
          build(
              type,
              () -> ErpWorkflowStructDef.COM_REPLY_12.asCanonicalType(erpWorkflowVersion, true),
              message);
      availabilityStatusStructDef = ErpWorkflowStructDef.AVAILABILITY_STATUS_12;
      availabilityCodeSystem = ErpWorkflowCodeSystem.AVAILABILITY_STATUS_12;
    }

    val payload = com.getPayloadFirstRep();

    if (availabilityStatus != null) {
      val ext =
          new Extension(
              availabilityStatusStructDef.getCanonicalUrl(),
              availabilityStatus.asCoding(availabilityCodeSystem));
      payload.addExtension(ext);
    }

    if (supplyOptionsType == null) {
      supplyOptionsType = SupplyOptionsType.onPremise();
    }
    payload.addExtension(supplyOptionsType.asExtension(erpWorkflowVersion));
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
        flowType, "A Representative Communication requires a Flow-Type of the Prescription");
  }

  private void checkRequiredForDispReq() {
    checkRequiredForTaskCommunication();
  }

  private void checkRequiredForReply() {
    checkRequiredForTaskCommunication();
  }
}
