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

package de.gematik.test.erezept.fhir.r4.kbv;

import static de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef.COMPOUNDING_INSTRUCTION;
import static de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef.PACKAGING;
import static de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef.PACKAGING_SIZE;
import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.FhirVersionException;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Medication")
@SuppressWarnings({"java:S110"})
public class KbvErpMedication extends Medication implements ErpFhirResource {

  public static KbvErpMedication fromMedication(Medication adaptee) {
    if (adaptee instanceof KbvErpMedication erpMedication) {
      return erpMedication;
    } else {
      val kbvMedication = new KbvErpMedication();
      adaptee.copyValues(kbvMedication);
      return kbvMedication;
    }
  }

  public static KbvErpMedication fromMedication(Resource adaptee) {
    return fromMedication((Medication) adaptee);
  }

  public List<MedicationCategory> getCatagory() {
    return this.getExtension().stream()
        .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::matches)
        .map(Extension::getValue)
        .map(coding -> coding.castToCoding(coding))
        .map(coding -> MedicationCategory.fromCode(coding.getCode()))
        .toList();
  }

  public MedicationCategory getCategoryFirstRep() {
    return this.getCatagory().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpMedication.class, KbvItaErpStructDef.MEDICATION_CATEGORY));
  }

  public Optional<MedicationType> getMedicationType() {
    return this.getCode().getCoding().stream()
        .filter(MedicationType.CODE_SYSTEM::matches)
        .map(coding -> MedicationType.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<String> getPackagingSize() {
    return this.getAmount().getNumerator().getExtension().stream()
        .filter(KbvItaErpStructDef.PACKAGING_SIZE::matches)
        .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
        .findFirst();
  }

  public int getPackagingSizeOrEmpty() {
    return this.getAmount().getNumerator().getExtension().stream()
        .filter(PACKAGING_SIZE::matches)
        .findFirst()
        .map(ext -> Integer.valueOf(ext.getValue().primitiveValue()))
        .orElse(0);
  }

  public Optional<String> getPackagingUnit() {
    return Optional.ofNullable(this.getAmount().getNumerator().getUnit());
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(KbvItaErpStructDef.MEDICATION_VACCINE::matches)
        .map(Extension::getValue)
        .map(coding -> coding.castToBoolean(coding))
        .map(BooleanType::booleanValue)
        .findFirst()
        .orElse(false);
  }

  /**
   * @return the free text of the medication or null if not present
   * @deprecated fetching the free text of the medication this way may result in a
   *     NullPointerException because this value is optional. Use {@link #getFreeTextOptional()}
   *     instead.
   */
  @Deprecated(since = "0.10.1", forRemoval = true)
  public String getFreeText() {
    return this.getCode().getText();
  }

  public Optional<String> getFreeTextOptional() {
    return Optional.ofNullable(this.getCode().getText());
  }

  public Optional<String> getIngredientText() {
    return this.getIngredient().stream()
        .map(mic -> mic.getItemCodeableConcept().getText())
        .findFirst();
  }

  public Optional<String> getManufactoringInstrOptional() {
    return this.getExtension().stream()
        .filter(ex -> ex.getUrl().contains(COMPOUNDING_INSTRUCTION.getCanonicalUrl()))
        .map(instr -> instr.getValue().primitiveValue())
        .findFirst();
  }

  /**
   * If an amount is given, format as String such as [VALUE] [UNIT]
   *
   * @return Amount Numerator with as String with Value and Unit
   */
  public Optional<String> getAmountNumeratorString() {
    Optional<String> ret = Optional.empty();
    val value = this.getAmount().getNumerator().getValue();
    val unit = this.getAmount().getNumerator().getUnit();
    if (value != null && unit != null) {
      val numerator = this.getAmount().getNumerator();
      ret = Optional.of(format("{0} {1}", numerator.getValue(), numerator.getUnit()));
    }
    return ret;
  }

  public Optional<String> getIngredientStrengthString() {
    return this.getIngredient().stream()
        .map(MedicationIngredientComponent::getStrength)
        .map(
            ratio ->
                format("{0} {1}", ratio.getNumerator().getValue(), ratio.getNumerator().getUnit()))
        .findFirst();
  }

  public Optional<Ratio> getIngredientStrengthRatio() {
    return this.getIngredient().stream()
        .map(MedicationIngredientComponent::getStrength)
        .findFirst();
  }

  public Optional<String> getIngredientTextOptional() {
    return this.getIngredient().stream()
        .map(MedicationIngredientComponent::getItemCodeableConcept)
        .map(CodeableConcept::getText)
        .findFirst();
  }

  public List<String> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(Coding::getCode)
        .toList();
  }

  public Optional<PZN> getPznOptional() {
    return this.getPznFromPznMedication().or(this::getPznFromCompounding);
  }

  public String getPznFirstRep() {
    return this.getPzn().stream()
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(KbvErpMedication.class, DeBasisProfilCodeSystem.PZN));
  }

  private Optional<PZN> getPznFromPznMedication() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(coding -> PZN.from(coding.getCode()))
        .findFirst();
  }

  private Optional<PZN> getPznFromCompounding() {
    return this.getIngredient().stream()
        .map(MedicationIngredientComponent::getItemCodeableConcept)
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(mic -> PZN.from(mic.getCodingFirstRep().getCode()))
        .findFirst();
  }

  public String getMedicationName() {
    return this.getCode().getText();
  }

  public Optional<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<String> getTextInFormOptional() {
    return Optional.ofNullable(this.getForm().getText());
  }

  public StandardSize getStandardSize() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.NORMGROESSE::matches)
        .map(ext -> StandardSize.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst()
        .orElse(StandardSize.KA);
  }

  @Override
  public String getDescription() {
    val type =
        this.getMedicationType()
            .map(mt -> format("(Verordnungstyp {0})", mt.getDisplay()))
            .orElse("(unbekannter Verordnungstyp)");
    val pznText =
        this.getPznOptional().map(pzn -> format("(PZN: {0})", pzn.getValue())).orElse(type);

    val medicationName = this.getMedicationName();
    val size = this.getStandardSize();
    return format("{0} {1} {2}", medicationName, size.getCode(), pznText);
  }

  public KbvItaErpVersion getVersion() {
    return this.getMeta().getProfile().stream()
        .findFirst()
        .map(
            profile -> {
              val profileTokens = profile.asStringValue().split("\\|");
              if (profileTokens.length > 1) {
                return KbvItaErpVersion.fromString(profileTokens[1]);
              } else {
                return KbvItaErpVersion.getDefaultVersion();
              }
            })
        .stream()
        .findFirst()
        .orElseThrow(
            () ->
                new FhirVersionException(
                    format(
                        "Unable to retrieve {0} for {1}",
                        KbvItaErpVersion.class.getSimpleName(), this.getClass().getSimpleName())));
  }

  public Optional<String> getPackagingOptional() {
    return this.getExtension().stream()
        .filter(ex -> ex.getUrl().contains(PACKAGING.getCanonicalUrl()))
        .map(instr -> instr.getValue().primitiveValue())
        .findFirst();
  }
}
