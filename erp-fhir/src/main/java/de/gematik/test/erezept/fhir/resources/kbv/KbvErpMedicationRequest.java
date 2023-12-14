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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.date.PeriodDateUtil;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "MedicationRequest")
@SuppressWarnings({"java:S110"})
public class KbvErpMedicationRequest extends MedicationRequest implements ErpFhirResource {

  public String getLogicalId() {
    return this.id.getIdPart();
  }

  public Optional<String> getNoteText() {
    val note = this.getNoteFirstRep().getText();
    return Optional.ofNullable(note);
  }

  public boolean hasAccidentExtension() {
    return this.getAccident().isPresent();
  }

  public Optional<AccidentCauseType> getAccidentCause() {
    return this.getAccident().map(AccidentExtension::accidentCauseType);
  }

  public Optional<String> getAccidentWorkplace() {
    return this.getAccident().map(AccidentExtension::workplace);
  }

  public Optional<Date> getAccidentDate() {
    return this.getAccident().map(AccidentExtension::accidentDay);
  }

  public Optional<AccidentExtension> getAccident() {
    return this.getExtension().stream()
        .filter(
            ext ->
                KbvItaErpStructDef.ACCIDENT.match(ext.getUrl())
                    || KbvItaForStructDef.ACCIDENT.match(ext.getUrl()))
        .findFirst()
        .map(AccidentExtension::fromExtension);
  }

  public boolean isBvg() {
    return this.getExtension().stream()
        .filter(ext -> KbvItaErpStructDef.BVG.match(ext.getUrl()))
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(false);
  }

  public boolean hasEmergencyServiceFee() {
    return this.getExtension().stream()
        .filter(ext -> KbvItaErpStructDef.EMERGENCY_SERVICES_FEE.match(ext.getUrl()))
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(false);
  }

  /**
   * check if the medicationrequest is a MVO
   *
   * @return true if MVO, false otherwise
   */
  public boolean isMultiple() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl().contains(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .map(ext -> ext.getExtensionByUrl("Kennzeichen"))
        .map(
            kennzeichen ->
                kennzeichen.getValue().castToBoolean(kennzeichen.getValue()).booleanValue())
        .findAny()
        .orElse(false);
  }

  public Optional<Date> getMvoStart() {
    return this.getMvoPeriod().map(Period::getStart);
  }

  public Optional<Date> getMvoEnd() {
    return this.getMvoPeriod().map(Period::getEnd);
  }

  public Optional<Integer> getNumerator() {
    return this.getMvoRatio()
        .map(Ratio::getNumerator)
        .map(quantity -> quantity.getValue().intValue());
  }

  public Optional<Integer> getDemoninator() {
    return this.getMvoRatio()
        .map(Ratio::getDenominator)
        .map((quantity -> quantity.getValue().intValue()));
  }

  public Optional<Ratio> getMvoRatio() {
    if (!isMultiple()) {
      return Optional.empty();
    }
    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl().contains(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .map(ext -> ext.getExtensionByUrl("Nummerierung"))
        .map(ratio -> ratio.getValue().castToRatio(ratio.getValue()))
        .findFirst();
  }

  public void updateMvoDates() {
    this.getMvoPeriod()
        .ifPresent(
            p -> {
              if (!PeriodDateUtil.isStillValid(p)) {
                PeriodDateUtil.updatePeriod(p);
              }
            });
  }

  public Optional<Period> getMvoPeriod() {
    if (!isMultiple()) {
      return Optional.empty();
    }

    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl().contains(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .map(ext -> ext.getExtensionByUrl("Zeitraum"))
        .map(period -> period.getValue().castToPeriod(period.getValue()))
        .findFirst();
  }

  public Optional<StatusCoPayment> getCoPaymentStatus() {
    return this.getExtension().stream()
        .filter(
            ext ->
                KbvItaForStructDef.STATUS_CO_PAYMENT.match(ext.getUrl())
                    || KbvItaErpStructDef.STATUS_CO_PAYMENT.match(ext.getUrl()))
        .map(ext -> ext.getValue().castToCoding(ext.getValue()))
        .map(coding -> StatusCoPayment.fromCode(coding.getCode()))
        .findFirst();
  }

  public boolean allowSubstitution() {
    // accessing directly the primitive boolean value might throw a NPE
    return Optional.ofNullable(this.getSubstitution().getAllowedBooleanType().asStringValue())
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  public String getNoteTextOr(String alternative) {
    return getNoteText().orElse(alternative);
  }

  public String getNoteTextOrEmpty() {
    return getNoteTextOr("");
  }

  @Override
  public String getDescription() {
    val prescription = this.isMultiple() ? "Mehrfachverordnung" : "Verordnung";
    var dosageInstruction = this.getDosageInstructionFirstRep().getText();
    if (dosageInstruction == null || dosageInstruction.isEmpty()) {
      dosageInstruction = "ohne Dosieranweisung";
    } else {
      dosageInstruction = format("mit Dosieranweisung {0}", dosageInstruction);
    }

    val quantity = this.getDispenseRequest().getQuantity();
    val autIdem = this.allowSubstitution() ? "mit aut-idem" : "ohne aut-idem";
    return format(
        "{0} {1} für {2} {3} {4}",
        prescription, autIdem, quantity.getValue(), quantity.getCode(), dosageInstruction);
  }

  public static KbvErpMedicationRequest fromMedicationRequest(MedicationRequest adaptee) {
    if (adaptee instanceof KbvErpMedicationRequest erpMedicationRequest) {
      return erpMedicationRequest;
    } else {
      val kbvMedicationRequest = new KbvErpMedicationRequest();
      adaptee.copyValues(kbvMedicationRequest);
      return kbvMedicationRequest;
    }
  }

  public static KbvErpMedicationRequest fromMedicationRequest(Resource adaptee) {
    return fromMedicationRequest((MedicationRequest) adaptee);
  }
}
