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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.builder.QuantityBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;

public class MedicationRequestFaker {
  private final MedicationRequestBuilder builder;
  private static KbvPatient patient;
  private final Map<String, Consumer<MedicationRequestBuilder>> builderConsumers = new HashMap<>();

  private MedicationRequestFaker(MedicationRequestBuilder builder) {
    this.builder = builder;
    builderConsumers.put("bvg", b -> b.isBVG(true));
    builderConsumers.put("emergencyServiceFee", b -> b.hasEmergencyServiceFee(false));
    builderConsumers.put("version", b -> b.version(KbvItaErpVersion.getDefaultVersion()));
    builderConsumers.put(
        "medication", b -> b.medication(KbvErpMedicationPZNFaker.builder().fake()));
    builderConsumers.put("requester", b -> b.requester(PractitionerFaker.builder().fake()));
    builderConsumers.put(
        "insurance",
        b -> b.insurance(KbvCoverageBuilder.faker(patient.getInsuranceKind()).build()));
    builderConsumers.put("requestQuantity", b -> b.quantityPackages(fakerAmount()));
    builderConsumers.put("dosage", b -> b.dosage(fakerDosage()));
    builderConsumers.put(
        "coPaymentStatus", b -> b.coPaymentStatus(fakerValueSet(StatusCoPayment.class)));
    builderConsumers.put("authorDate", b -> b.authoredOn(new Date()));
    builderConsumers.put("substitution", b -> b.substitution(true));
  }

  public static MedicationRequestFaker builder() {
    patient = PatientFaker.builder().fake();
    return new MedicationRequestFaker(MedicationRequestBuilder.forPatient(patient));
  }

  public static MedicationRequestFaker builder(KbvPatient kbvPatient) {
    patient = kbvPatient;
    return new MedicationRequestFaker(MedicationRequestBuilder.forPatient(kbvPatient));
  }

  public MedicationRequestFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.computeIfPresent("version", (key, defaultValue) -> b -> b.version(version));
    return this;
  }

  public MedicationRequestFaker withMedication(KbvErpMedication medication) {
    builderConsumers.computeIfPresent(
        "medication", (key, defaultValue) -> b -> b.medication(medication));
    return this;
  }

  public MedicationRequestFaker withRequester(Practitioner practitioner) {
    builderConsumers.computeIfPresent(
        "requester", (key, defaultValue) -> b -> b.requester(practitioner));
    return this;
  }

  public MedicationRequestFaker withInsurance(KbvCoverage coverage) {
    builderConsumers.computeIfPresent(
        "insurance", (key, defaultValue) -> b -> b.insurance(coverage));
    return this;
  }

  public MedicationRequestFaker withAccident(AccidentExtension accident) {
    builderConsumers.put("accident", b -> b.accident(accident));
    return this;
  }

  public MedicationRequestFaker withStatus(MedicationRequest.MedicationRequestStatus status) {
    builderConsumers.put("medRequestStatus", b -> b.status(status));
    return this;
  }

  public MedicationRequestFaker withStatus(String code) {
    builder.status(code);
    return this;
  }

  public MedicationRequestFaker withIntent(MedicationRequest.MedicationRequestIntent intent) {
    builderConsumers.put("medRequestIntent", b -> b.intent(intent));
    return this;
  }

  public MedicationRequestFaker withIntent(String code) {
    builder.intent(code);
    return this;
  }

  public MedicationRequestFaker withSubstitution(
      MedicationRequest.MedicationRequestSubstitutionComponent substitution) {
    builderConsumers.computeIfPresent(
        "substitution", (key, defaultValue) -> b -> b.substitution(substitution));
    return this;
  }

  public MedicationRequestFaker withSubstitution(boolean allowed) {
    return this.withSubstitution(
        new MedicationRequest.MedicationRequestSubstitutionComponent(new BooleanType(allowed)));
  }

  public MedicationRequestFaker withQuantity(
      MedicationRequest.MedicationRequestDispenseRequestComponent quantity) {
    builderConsumers.computeIfPresent(
        "dispenseQuantity", (key, defaultValue) -> b -> b.quantity(quantity));
    return this;
  }

  public MedicationRequestFaker withQuantity(Quantity quantity) {
    this.withQuantity(
        new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(quantity));
    return this;
  }

  public MedicationRequestFaker withQuantityPackages(int amount) {
    this.withQuantity(QuantityBuilder.asUcumPackage().withValue(amount));
    return this;
  }

  public MedicationRequestFaker withDosageInstruction(String text) {
    builderConsumers.computeIfPresent("dosage", (key, defaultValue) -> b -> b.dosage(text));
    return this;
  }

  public MedicationRequestFaker withCoPaymentStatus(StatusCoPayment statusCoPayment) {
    builderConsumers.computeIfPresent(
        "coPaymentStatus", (key, defaultValue) -> b -> b.coPaymentStatus(statusCoPayment));
    return this;
  }

  public MedicationRequestFaker withBvg(final boolean bvg) {
    builderConsumers.computeIfPresent("bvg", (key, defaultValue) -> b -> b.isBVG(bvg));
    return this;
  }

  public MedicationRequestFaker withEmergencyServiceFee(final boolean emergencyServiceFee) {
    builderConsumers.computeIfPresent(
        "emergencyServiceFee",
        (key, defaultValue) -> b -> b.hasEmergencyServiceFee(emergencyServiceFee));
    return this;
  }

  public MedicationRequestFaker withMvo(MultiplePrescriptionExtension mvo) {
    builderConsumers.put("mvo", b -> b.mvo(mvo));
    return this;
  }

  public MedicationRequestFaker withAuthorDate(Date date) {
    builderConsumers.computeIfPresent("authorDate", (key, defaultValue) -> b -> b.authoredOn(date));
    return this;
  }

  public MedicationRequestFaker withAuthorDate(
      Date date, TemporalPrecisionEnum temporalPrecisionEnum) {
    builder.authoredOn(date, temporalPrecisionEnum);
    return this;
  }

  public MedicationRequestFaker withNote(String note) {
    builderConsumers.put("note", b -> b.note(note));
    return this;
  }

  public KbvErpMedicationRequest fake() {
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder.build();
  }
}
