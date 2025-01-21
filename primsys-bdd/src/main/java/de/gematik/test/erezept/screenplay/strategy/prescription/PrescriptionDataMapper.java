/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.FlowTypeUtil;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PrescriptionDataMapper {

  protected final List<Map<String, String>> medications;
  @Getter private final Actor patient;
  @Getter private final PrescriptionAssignmentKind type;
  private String mvoId = UUID.randomUUID().toString(); // default MVO-ID

  protected abstract KbvErpMedication getKbvErpMedication(Map<String, String> medMap);

  /**
   * The PrescriptionFlowType can be reasoned from the insurance kind (VersicherungsArtDeBasis) and
   * the PrescriptionAssignementKind. However, if a different WorkflowType is given via the
   * DataTable as the code, this one will overwrite the reasoned one.
   *
   * @return a PrescriptionFlowType which was reasoned from Data; if provided via code (code !=
   *     null) then the PrescriptionFlowType from this code
   */
  protected KbvPatient getPatientBaseData() {
    val baseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
    log.info(
        format(
            "Issue ePrescription to patient {0} with insurance type {1}",
            baseData.getFullName(), baseData.getPatientInsuranceType()));
    return baseData.getPatient();
  }

  protected String getOrDefault(String key, String defaultValue, Map<String, String> medMap) {
    return Optional.ofNullable(medMap.getOrDefault(key, defaultValue))
        .map(String::trim)
        .orElse(defaultValue);
  }

  protected PrescriptionFlowType getFlowType(Map<String, String> medMap) {
    val baseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
    val insuranceKind = baseData.getCoverageInsuranceType();
    val code = medMap.get("Workflow");
    return FlowTypeUtil.getFlowType(code, insuranceKind, type);
  }

  /**
   * Perform the mapping from a single DataTable-Row to fully built KBV-Bundle
   *
   * @return a fully built KBV-Bundle
   */
  public List<Pair<KbvErpBundleBuilder, PrescriptionFlowType>> createKbvBundles(
      KbvPractitioner practitioner, MedicalOrganization organization) {
    val kbvPatient = getPatientBaseData();
    val insurance =
        SafeAbility.getAbility(patient, ProvidePatientBaseData.class).getInsuranceCoverage();
    return medications.stream()
        .map(medMap -> createKbvBundle(practitioner, organization, kbvPatient, insurance, medMap))
        .toList();
  }

  private Pair<KbvErpBundleBuilder, PrescriptionFlowType> createKbvBundle(
      KbvPractitioner practitioner,
      MedicalOrganization organization,
      KbvPatient patient,
      KbvCoverage insurance,
      Map<String, String> medMap) {

    // read the row of the prescription and fill up with faker values if required
    val statusKennzeichen = medMap.getOrDefault("KBV_Statuskennzeichen", "00");
    val substitution =
        Boolean.parseBoolean(
            medMap.getOrDefault("Substitution", GemFaker.getFaker().bool().toString()));
    val dosage = medMap.getOrDefault("Dosierung", GemFaker.fakerDosage());
    val amount = medMap.getOrDefault("Menge", "1");
    val emergencyServiceFee =
        Boolean.parseBoolean(
            medMap.getOrDefault("Notdiensgebühr", GemFaker.getFaker().bool().toString()));
    val paymentStatus =
        medMap.getOrDefault(
            "Zahlungsstatus", GemFaker.fakerValueSet(StatusCoPayment.class).getCode());

    MultiplePrescriptionExtension mvo;
    val isMvo = Boolean.parseBoolean(medMap.getOrDefault("MVO", "false"));
    if (isMvo) {
      val denominator = Integer.parseInt(medMap.getOrDefault("Denominator", "4"));
      val numerator = Integer.parseInt(medMap.getOrDefault("Numerator", "1"));

      this.mvoId =
          Optional.ofNullable(medMap.get("MVO-ID"))
              .map(String::trim) // get rid of empty strings containing only whitespaces
              .filter(id -> !Strings.isNullOrEmpty(id))
              .orElse(this.mvoId);

      val mvoBuilder =
          MultiplePrescriptionExtension.asMultiple(numerator, denominator).withId(mvoId);

      val start = medMap.getOrDefault("Gueltigkeitsstart", "leer");
      if (!start.equalsIgnoreCase("leer")) {
        mvoBuilder.starting(Integer.parseInt(start));
      }

      val end = medMap.getOrDefault("Gueltigkeitsende", "leer");
      if (!end.equalsIgnoreCase("leer")) {
        mvo = mvoBuilder.validForDays(Integer.parseInt(end), false);
      } else {
        mvo = mvoBuilder.withoutEndDate(false);
      }

    } else {
      mvo = MultiplePrescriptionExtension.asNonMultiple();
    }
    val medication = getKbvErpMedication(medMap);

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage(dosage)
            .quantityPackages(Integer.decode(amount))
            .status("active")
            .intent("order")
            .isBVG(false)
            .mvo(mvo)
            .hasEmergencyServiceFee(emergencyServiceFee)
            .substitution(substitution)
            .coPaymentStatus(StatusCoPayment.fromCode(paymentStatus))
            .build();

    // create and return the KBV Bundle
    val kbvBuilder =
        KbvErpBundleBuilder.builder()
            .statusKennzeichen(statusKennzeichen)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    return Pair.of(kbvBuilder, this.getFlowType(medMap));
  }
}
