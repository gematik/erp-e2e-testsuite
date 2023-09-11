/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.FhirProfileException;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
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

  public List<MedicationCategory> getCatagory() {
    return this.getExtension().stream()
        .filter(
            ext -> ext.getUrl().equals(KbvItaErpStructDef.MEDICATION_CATEGORY.getCanonicalUrl()))
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
        .filter(coding -> coding.getSystem().equals(MedicationType.CODE_SYSTEM.getCanonicalUrl()))
        .map(coding -> MedicationType.fromCode(coding.getCode()))
        .findFirst();
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(KbvItaErpStructDef.MEDICATION_VACCINE.getCanonicalUrl()))
        .map(Extension::getValue)
        .map(coding -> coding.castToBoolean(coding))
        .map(BooleanType::booleanValue)
        .findFirst()
        .orElse(false);
  }

  public String getFreeText() {
    return this.getCode().getText();
  }

  public Optional<String> getIngredientText() {
    return this.getIngredient().stream()
        .map(mic -> mic.getItemCodeableConcept().getText())
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

  public List<String> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(coding -> coding.getSystem().equals(DeBasisCodeSystem.PZN.getCanonicalUrl()))
        .map(Coding::getCode)
        .toList();
  }

  public String getPznFirstRep() {
    return this.getPzn().stream()
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(KbvErpMedication.class, DeBasisCodeSystem.PZN));
  }

  public String getMedicationName() {
    return this.getCode().getText();
  }

  public List<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .toList();
  }

  public Optional<Darreichungsform> getDarreichungsformFirstRep() {
    return this.getDarreichungsform().stream().findFirst();
  }

  public StandardSize getStandardSize() {
    return this.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(DeBasisStructDef.NORMGROESSE.getCanonicalUrl()))
        .map(ext -> StandardSize.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst()
        .orElse(StandardSize.KA);
  }

  @Override
  public String getDescription() {
    val medicationName = this.getMedicationName();
    val pzn = this.getPznFirstRep();
    val size = this.getStandardSize();
    val category = this.getCategoryFirstRep();
    return format(
        "{0} {1} (PZN: {2}) / {3}", medicationName, size.getCode(), pzn, category.getDisplay());
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
                new FhirProfileException(
                    format(
                        "Unable to retrieve {0} for {1}",
                        KbvItaErpVersion.class.getSimpleName(), this.getClass().getSimpleName())));
  }

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
}
