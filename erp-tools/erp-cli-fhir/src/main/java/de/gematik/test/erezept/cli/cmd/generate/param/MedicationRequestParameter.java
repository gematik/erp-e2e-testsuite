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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.r4.kbv.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import javax.annotation.Nullable;
import lombok.*;
import picocli.*;
import picocli.CommandLine.*;

public class MedicationRequestParameter implements BaseResourceParameter {

  @Option(names = "--mvo", type = Boolean.class, description = "Multiple Prescriptions (MVO) only")
  @Getter
  private boolean mvoOnly = false;

  @Option(
      names = {"--substitution", "--aut-idem"},
      type = Boolean.class,
      description =
          "If set a substitution of the medication will be allowed (default=${DEFAULT-VALUE})")
  @Getter
  private boolean substitution = false;

  @Option(
      names = {"--emergency-fee"},
      type = Boolean.class,
      description = "If set an emergency service fee flag will be set (default=${DEFAULT-VALUE})")
  @Getter
  private boolean emergencyServiceFee = false;

  @Option(
      names = {"--accident"},
      type = AccidentCauseType.class,
      description = "If set the MedicationRequest will be marked as an accident (default=null)")
  @Getter
  private AccidentCauseType accidentCauseType;

  @CommandLine.Option(
      names = {"--dosage"},
      paramLabel = "<INSTRUCTION>",
      type = String.class,
      description = "Dosage instruction")
  private String dosage;

  @CommandLine.Option(
      names = {"--packages"},
      paramLabel = "<QUANTITY>",
      type = Integer.class,
      description = "The quantity of packages (default=random)")
  private Integer packages;

  @CommandLine.Option(
      names = {"--co-payment"},
      paramLabel = "<STATUS>",
      type = StatusCoPayment.class,
      description =
          "The Type of the Insurance from ${COMPLETION-CANDIDATES} (default=${DEFAULT-VALUE})")
  @Getter
  private StatusCoPayment statusCoPayment = StatusCoPayment.STATUS_0;

  @Setter private KbvPatient patient;
  @Setter private KbvCoverage insurance;
  @Setter private KbvPractitioner practitioner;
  @Setter private KbvErpMedication medication;

  public KbvPatient getPatient() {
    return this.getOrDefault(patient, () -> KbvPatientFaker.builder().fake());
  }

  public KbvCoverage getInsurance() {
    return this.getOrDefault(insurance, () -> KbvCoverageFaker.builder().fake());
  }

  public KbvPractitioner getPractitioner() {
    return this.getOrDefault(practitioner, () -> KbvPractitionerFaker.builder().fake());
  }

  public KbvErpMedication getMedication() {
    return this.getOrDefault(medication, () -> KbvErpMedicationPZNFaker.builder().fake());
  }

  public String getDosage() {
    return this.getOrDefault(dosage, GemFaker::fakerDosage);
  }

  public Integer getPackagesQuantity() {
    return this.getOrDefault(packages, GemFaker::fakerAmount);
  }

  private @Nullable AccidentExtension getAccidentExtension() {
    if (accidentCauseType != null) {
      return AccidentExtension.faker(accidentCauseType);
    } else {
      return null;
    }
  }

  public KbvErpMedicationRequest createMedicationRequest() {
    val mvo = mvoOnly ? GemFaker.mvo(true) : GemFaker.mvo();
    return KbvErpMedicationRequestBuilder.forPatient(this.getPatient())
        .insurance(getInsurance())
        .requester(getPractitioner())
        .medication(getMedication())
        .dosage(getDosage())
        .quantityPackages(getPackagesQuantity())
        .status("active")
        .intent("order")
        .isBVG(GemFaker.fakerBool())
        .mvo(mvo)
        .accident(getAccidentExtension())
        .hasEmergencyServiceFee(isEmergencyServiceFee())
        .substitution(isSubstitution())
        .coPaymentStatus(getStatusCoPayment())
        .build();
  }
}
