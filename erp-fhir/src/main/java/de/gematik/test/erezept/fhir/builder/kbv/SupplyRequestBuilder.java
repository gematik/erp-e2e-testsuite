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

package de.gematik.test.erezept.fhir.builder.kbv;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import java.security.SecureRandom;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.SupplyRequest;

/**
 * Class is only used in DeclinePrescriptionWithPracticeSupply Class reason: SupplyRequest ist
 * defined @ KBV and allowed, and forbidden by A_23384 out of C_11292 if MedicationRequest is
 * contained
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplyRequestBuilder extends ResourceBuilder<SupplyRequest, SupplyRequestBuilder> {

  private Date authoredOn = new Date();
  private TemporalPrecisionEnum temporalPrecision = TemporalPrecisionEnum.DAY;
  private KbvCoverage coverage;
  private Reference medicationReference;
  private Reference requesterReference;

  public static SupplyRequestBuilder withCoverage(KbvCoverage coverage) {
    val srb = new SupplyRequestBuilder();
    srb.coverage = coverage;
    return srb;
  }

  @Override
  public SupplyRequest build() {
    checkRequiredAttributes();
    val sR = this.createResource(SupplyRequest::new, KbvItaErpStructDef.SUPPLY_REQUEST);

    sR.setAuthoredOnElement(new DateTimeType(authoredOn, temporalPrecision));
    addKostentraegerExtension(sR);
    sR.setQuantity(this.getQuantity());
    sR.setRequester(requesterReference);
    sR.setItem(medicationReference);
    return sR;
  }

  private void addKostentraegerExtension(SupplyRequest sR) {
    val ktExt = sR.addExtension();
    ktExt.setUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_PracticeSupply_Payor");
    val ikIdentifier =
        DeBasisProfilNamingSystem.IKNR_SID.asIdentifier(coverage.getIknr().getValue());

    ktExt.addExtension(new Extension("IK", ikIdentifier));
    ktExt.addExtension(new Extension("Name", new StringType(coverage.getName())));
    ktExt.addExtension("Kostentraegertyp", coverage.getInsuranceKind().asCoding());
  }

  private Quantity getQuantity() {
    return new Quantity()
        .setCode("{Package}")
        .setSystem("http://unitsofmeasure.org")
        .setValue(new SecureRandom().nextInt(0, 5));
  }

  public SupplyRequestBuilder requester(KbvPractitioner practitioner) {
    this.requesterReference = practitioner.asReference();
    return this;
  }

  public SupplyRequestBuilder coverage(KbvCoverage coverage) {
    this.coverage = coverage;
    return this;
  }

  public SupplyRequestBuilder authoredOn(Date date) {
    this.authoredOn = date;
    return this;
  }

  public SupplyRequestBuilder authoredOn(Date date, TemporalPrecisionEnum temporalPrecision) {
    this.temporalPrecision = temporalPrecision;
    return authoredOn(date);
  }

  public SupplyRequestBuilder medication(KbvErpMedication medication) {
    this.medicationReference = medication.asReference();
    return this;
  }

  private void checkRequiredAttributes() {
    checkRequired(
        medicationReference,
        "required argument is missing, please setup before: medicationReference");
    checkRequired(
        requesterReference,
        "required argument is missing, please setup before: requesterReference,");
    checkRequired(coverage, "required argument is missing, please setup before: coverage.");
  }
}
