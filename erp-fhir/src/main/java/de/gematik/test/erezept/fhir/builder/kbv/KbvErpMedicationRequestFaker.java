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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerAmount;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDosage;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.ucum.builder.QuantityBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;

@Slf4j
public class KbvErpMedicationRequestFaker {

  private AccidentExtension accident;

  private KbvPatient kbvPatient = KbvPatientFaker.builder().fake();
  private final Map<String, Consumer<KbvErpMedicationRequestBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationRequestFaker() {
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      this.withBvg(fakerBool());
    } else {
      this.withSer(fakerBool());
    }
    this.withEmergencyServiceFee(fakerBool())
        .withMedication(
            KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake())
        .withRequester(KbvPractitionerFaker.builder().fake())
        .withInsurance(
            KbvCoverageFaker.builder().withInsuranceType(kbvPatient.getInsuranceType()).fake())
        .withDosageInstruction(fakerDosage())
        .withAuthorDate(new Date())
        .withSubstitution(fakerBool());

    // TODO: why do these not have a faker method?
    builderConsumers.put("requestQuantity", b -> b.quantityPackages(fakerAmount()));
  }

  public static KbvErpMedicationRequestFaker builder() {
    return new KbvErpMedicationRequestFaker();
  }

  public KbvErpMedicationRequestFaker withPatient(KbvPatient patient) {
    kbvPatient = patient;
    return this;
  }

  public KbvErpMedicationRequestFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationRequestFaker withMedication(KbvErpMedication medication) {
    builderConsumers.put("medication", b -> b.medication(medication));
    return this;
  }

  public KbvErpMedicationRequestFaker withRequester(KbvPractitioner practitioner) {
    builderConsumers.put("requester", b -> b.requester(practitioner));
    return this;
  }

  public KbvErpMedicationRequestFaker withInsurance(KbvCoverage coverage) {
    builderConsumers.put("coverage", b -> b.insurance(coverage));
    return this;
  }

  public KbvErpMedicationRequestFaker withAccident(AccidentExtension accident) {
    this.accident = accident;
    builderConsumers.put("accident", b -> b.accident(accident));
    return this;
  }

  public KbvErpMedicationRequestFaker withStatus(MedicationRequest.MedicationRequestStatus status) {
    builderConsumers.put("medRequestStatus", b -> b.status(status));
    return this;
  }

  public KbvErpMedicationRequestFaker withStatus(String code) {
    var status = MedicationRequest.MedicationRequestStatus.fromCode(code);
    if (status == null) {
      log.warn(
          "Given code {} cannot be converted to a MedicationRequestStatus: using UNKNOWN as"
              + " default",
          code);
      status = MedicationRequest.MedicationRequestStatus.UNKNOWN;
    }
    return this.withStatus(status);
  }

  public KbvErpMedicationRequestFaker withIntent(MedicationRequest.MedicationRequestIntent intent) {
    builderConsumers.put("medRequestIntent", b -> b.intent(intent));
    return this;
  }

  public KbvErpMedicationRequestFaker withIntent(String code) {
    var intent = MedicationRequest.MedicationRequestIntent.fromCode(code);
    if (intent == null) {
      log.warn(
          "Given code {} cannot be converted to a MedicationRequestIntent: using NULL as"
              + " default",
          code);
      intent = MedicationRequest.MedicationRequestIntent.NULL;
    }
    return this.withIntent(intent);
  }

  public KbvErpMedicationRequestFaker withSubstitution(
      MedicationRequest.MedicationRequestSubstitutionComponent substitution) {
    builderConsumers.put("substitution", b -> b.substitution(substitution));
    return this;
  }

  public KbvErpMedicationRequestFaker withSubstitution(boolean allowed) {
    return this.withSubstitution(
        new MedicationRequest.MedicationRequestSubstitutionComponent(new BooleanType(allowed)));
  }

  public KbvErpMedicationRequestFaker withQuantity(
      MedicationRequest.MedicationRequestDispenseRequestComponent quantity) {
    builderConsumers.put("dispenseQuantity", b -> b.quantity(quantity));
    return this;
  }

  public KbvErpMedicationRequestFaker withQuantity(Quantity quantity) {
    this.withQuantity(
        new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(quantity));
    return this;
  }

  public KbvErpMedicationRequestFaker withQuantityPackages(int amount) {
    this.withQuantity(QuantityBuilder.asUcumPackage().withValue(amount));
    return this;
  }

  public KbvErpMedicationRequestFaker withDosageInstruction(String text) {
    builderConsumers.put("dosage", b -> b.dosage(text));
    return this;
  }

  public KbvErpMedicationRequestFaker withCoPaymentStatus(StatusCoPayment statusCoPayment) {
    builderConsumers.put("coPaymentStatus", b -> b.coPaymentStatus(statusCoPayment));
    return this;
  }

  public KbvErpMedicationRequestFaker withBvg(final boolean bvg) {
    builderConsumers.put("bvg", b -> b.isBVG(bvg));
    return this;
  }

  public KbvErpMedicationRequestFaker withSer(final boolean ser) {
    builderConsumers.put("ser", b -> b.isSER(ser));
    return this;
  }

  public KbvErpMedicationRequestFaker withEmergencyServiceFee(final boolean emergencyServiceFee) {
    builderConsumers.put("emergencyServiceFee", b -> b.hasEmergencyServiceFee(emergencyServiceFee));
    return this;
  }

  public KbvErpMedicationRequestFaker withMvo(MultiplePrescriptionExtension mvo) {
    builderConsumers.put("mvo", b -> b.mvo(mvo));
    return this;
  }

  public KbvErpMedicationRequestFaker withAuthorDate(Date date) {
    return this.withAuthorDate(date, TemporalPrecisionEnum.DAY);
  }

  public KbvErpMedicationRequestFaker withAuthorDate(
      Date date, TemporalPrecisionEnum temporalPrecisionEnum) {
    builderConsumers.put("authorDate", b -> b.authoredOn(date, temporalPrecisionEnum));
    return this;
  }

  public KbvErpMedicationRequestFaker withNote(String note) {
    builderConsumers.put("note", b -> b.note(note));
    return this;
  }

  public KbvErpMedicationRequest fake() {
    return this.toBuilder().build();
  }

  public KbvErpMedicationRequestBuilder toBuilder() {
    val builder = KbvErpMedicationRequestBuilder.forPatient(kbvPatient);

    if (accident != null
            && accident.toString().equals(AccidentCauseType.ACCIDENT_AT_WORK.getDisplay())
        || accident != null
            && accident.toString().equals(AccidentCauseType.OCCUPATIONAL_DISEASE.getDisplay())) {
      this.withCoPaymentStatus(StatusCoPayment.STATUS_1);

    } else if (builderConsumers.get("coPaymentStatus") == null) {
      this.withCoPaymentStatus(fakerValueSet(StatusCoPayment.class));
    }

    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
