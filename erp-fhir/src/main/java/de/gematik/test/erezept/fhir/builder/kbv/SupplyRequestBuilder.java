/*
 * Copyright 2023 gematik GmbH
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
import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.references.kbv.MedicationReference;
import de.gematik.test.erezept.fhir.references.kbv.RequesterReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.SupplyRequest;

/**
 * Class is only used in DeclinePrescriptionWithPracticeSupply Class reason: SupplyRequest ist
 * defined @ KBV and allowed, and forbidden by A_23384 out of C_11292 if MedicationRequest is
 * contained
 */
public class SupplyRequestBuilder extends AbstractResourceBuilder<SupplyRequestBuilder> {

  private Date authoredOn = new Date();
  private TemporalPrecisionEnum temporalPrecision = TemporalPrecisionEnum.DAY;
  private KbvCoverage coverage;
  private Reference medicationReference;
  private Reference requesterReference;
  private KbvPatient patient;

  private SupplyRequestBuilder() {}

  public static SupplyRequestBuilder fakeForPatient(KbvPatient patient) {
    val srb = new SupplyRequestBuilder();
    srb.patient = patient;
    return srb;
  }

  public static SupplyRequestBuilder withCoverage(KbvCoverage coverage) {
    val srb = new SupplyRequestBuilder();
    srb.coverage = coverage;
    return srb;
  }

  /**
   * to build a valid SupplyRequest it is mandatory to set up: requesterReference with
   * "requester(@NonNull Practitioner practitioner)", coverage with "coverage(KbvCoverage coverage)"
   * and medication with "medication(@NonNull KbvErpMedication medication)"
   *
   * @return valid SupplyRequest designed by "KBV_PR_ERP_PracticeSupply" profile
   */
  public SupplyRequest build() {
    checkRequiredAttributes();
    SupplyRequest sR = new SupplyRequest();
    sR.setId(this.getResourceId());
    sR.setAuthoredOnElement(new DateTimeType(authoredOn, temporalPrecision));
    addKostentraegerExtension(sR);
    sR.setMeta(new Meta());
    sR.getMeta()
        .setProfile(
            List.of(new CanonicalType(KbvItaErpStructDef.SUPPLY_REQUEST.getCanonicalUrl())));
    sR.setQuantity(this.getQuantity());
    sR.setRequester(requesterReference);
    sR.setItem(medicationReference);
    return sR;
  }

  private void addKostentraegerExtension(SupplyRequest sR) {
    val ktExt = sR.addExtension();
    ktExt.setUrl("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_PracticeSupply_Payor");
    val ikIdentifier =
        new Identifier()
            .setSystem(DeBasisNamingSystem.IKNR_SID.getCanonicalUrl())
            .setValue(coverage.getIknr().getValue());
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

  public SupplyRequestBuilder requester(@NonNull Practitioner practitioner) {
    this.requesterReference = new RequesterReference(practitioner.getId()).asReference();
    return self();
  }

  public SupplyRequestBuilder coverage(@NonNull KbvCoverage coverage) {
    this.coverage = coverage;
    return self();
  }

  public SupplyRequestBuilder authoredOn(Date date) {
    this.authoredOn = date;
    return self();
  }

  public SupplyRequestBuilder authoredOn(Date date, TemporalPrecisionEnum temporalPrecision) {
    this.temporalPrecision = temporalPrecision;
    return authoredOn(date);
  }

  public SupplyRequestBuilder medication(@NonNull KbvErpMedication medication) {
    this.medicationReference = new MedicationReference(medication.getId()).asReference();
    return self();
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
