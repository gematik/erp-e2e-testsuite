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

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

/** Builder for ErxCommunication of type INFO_REQ */
public class ErxComInfoReqBuilder extends ErxComPrescriptionBuilder<ErxComInfoReqBuilder> {

  protected ErxComInfoReqBuilder(String message) {
    super(message);
  }

  @Override
  public ErxCommunication build() {
    checkRequired();
    val type = CommunicationType.INFO_REQ;

    ErxCommunication com;
    ErpWorkflowStructDef insuranceProvider;
    ErpWorkflowStructDef substitutionAllowedExt;
    ErpWorkflowStructDef prescriptionType;
    Identifier insuranceIdentifier;
    Coding flowTypeCoding;
    if (this.erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      com = buildCommon(type, () -> type.getType().asCanonicalType());
      insuranceProvider = ErpWorkflowStructDef.INSURANCE_PROVIDER;
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE;
      insuranceIdentifier = insuranceIknr.asIdentifier();
      flowTypeCoding = flowType.asCoding(true);
    } else {
      com =
          buildCommon(
              type, () -> ErpWorkflowStructDef.COM_INFO_REQ_12.asCanonicalType(erpWorkflowVersion));
      insuranceProvider = ErpWorkflowStructDef.INSURANCE_PROVIDER_12;
      substitutionAllowedExt = ErpWorkflowStructDef.SUBSTITUTION_ALLOWED_12;
      prescriptionType = ErpWorkflowStructDef.PRESCRIPTION_TYPE_12;
      insuranceIdentifier = insuranceIknr.asIdentifier(DeBasisProfilNamingSystem.IKNR_SID);
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
