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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;

/** Builder for ErxCommunication of type INFO_REQ */
public class ErxComInfoReqBuilder extends ErxComPrescriptionBuilder<ErxComInfoReqBuilder> {

  protected ErxComInfoReqBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    checkRequired();
    val com =
        buildCommon(() -> ErpWorkflowStructDef.COM_INFO_REQ.asCanonicalType(erpWorkflowVersion));
    val insuranceProvider = ErpWorkflowStructDef.INSURANCE_PROVIDER;
    val substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED;
    val prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE_12;
    val insuranceIdentifier = insuranceIknr.asIdentifier(DeBasisProfilNamingSystem.IKNR_SID);
    // hacky but should work fow now!
    val flowTypeCoding =
        flowType.asCoding(true).setSystem(ErpWorkflowCodeSystem.FLOW_TYPE_12.getCanonicalUrl());

    // set sender and receiver
    com.addRecipient(
        CommunicationType.INFO_REQ.getRecipientReference(this.receiver, this.erpWorkflowVersion));
    Optional.ofNullable(this.sender)
        .ifPresent(
            s ->
                com.setSender(
                    CommunicationType.INFO_REQ.getSenderReference(s, this.erpWorkflowVersion)));

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
    com.setBasedOn(List.of(new Reference(baseOnReference)));
    return com;
  }

  private void checkRequired() {
    this.checkRequired(medication, "A InfoReq Communication requires a contained Medication");
    this.checkRequired(insuranceIknr, "A InfoReq Communication requires an IKNR of the Insurance");
    this.checkRequired(
        flowType, "A InfoReq Communication requires a Flow-Type of the Prescription");
  }
}
