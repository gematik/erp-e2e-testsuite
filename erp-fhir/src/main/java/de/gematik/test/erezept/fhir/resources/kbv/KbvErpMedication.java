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

package de.gematik.test.erezept.fhir.resources.kbv;

import static de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef.*;
import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

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
        .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::match)
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
        .filter(MedicationType.CODE_SYSTEM::match)
        .map(coding -> MedicationType.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<Integer> getPackagingAmount() {
    return this.getAmount().getNumerator().getExtension().stream()
        .filter(KbvItaErpStructDef.PACKAGING_SIZE::match)
        .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
        .map(Integer::parseInt) // why is the amount defined as String?
        .findFirst();
  }

  public int getMedicationAmount() {
    return this.getAmount().getNumerator().getExtension().stream()
        .filter(PACKAGING_SIZE::match)
        .findFirst()
        .map(ext -> Integer.valueOf(ext.getValue().primitiveValue()))
        .orElse(0);
  }

  public Optional<String> getPackagingUnit() {
    return Optional.ofNullable(this.getAmount().getNumerator().getUnit());
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(KbvItaErpStructDef.MEDICATION_VACCINE::match)
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
        .filter(DeBasisCodeSystem.PZN::match)
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
            () -> new MissingFieldException(KbvErpMedication.class, DeBasisCodeSystem.PZN));
  }

  private Optional<PZN> getPznFromPznMedication() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisCodeSystem.PZN::match)
        .map(coding -> PZN.from(coding.getCode()))
        .findFirst();
  }

  private Optional<PZN> getPznFromCompounding() {
    return this.getIngredient().stream()
        .filter(
            mic ->
                DeBasisCodeSystem.PZN.match(
                    mic.getItemCodeableConcept().getCodingFirstRep().getSystem()))
        .map(mic -> PZN.from(mic.getItemCodeableConcept().getCodingFirstRep().getCode()))
        .findFirst();
  }

  public String getMedicationName() {
    return this.getCode().getText();
  }

  /**
   * @return the free text of the medication or null if not present
   * @deprecated fetching the form.code of the medication this way may result in a
   *     NullPointerException because this value is optional. Use {@link
   *     #getDarreichungsformOptional} instead.
   */
  @Deprecated(since = "0.10.1", forRemoval = false)
  public List<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .toList();
  }

  public Optional<List<Darreichungsform>> getDarreichungsformOptional() {
    return Optional.of(
        this.getForm().getCoding().stream()
            .map(coding -> Darreichungsform.fromCode(coding.getCode()))
            .toList());
  }

  public Optional<String> getTextInFormOptional() {
    return Optional.ofNullable(this.getForm().getText());
  }

  public Optional<Darreichungsform> getDarreichungsformFirstRep() {
    return this.getDarreichungsform().stream().findFirst();
  }

  public StandardSize getStandardSize() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.NORMGROESSE::match)
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
                return ProfileVersion.fromString(KbvItaErpVersion.class, profileTokens[1]);
              } else {
                return KbvItaErpVersion.getDefaultVersion();
              }
            })
        .stream()
        .findFirst()
        .orElseThrow(
            () ->
                new FhirValidatorException(
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
