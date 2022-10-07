/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
@ResourceDef(name = "Medication", profile = StructureDefinitionFixedUrls.KBV_PR_ERP_MEDICATION_PZN)
@SuppressWarnings({"java:S110"})
public class KbvErpMedication extends Medication {

  public List<MedicationCategory> getCatagory() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl()
                    .equals(ErpStructureDefinition.KBV_MEDICATION_CATEGORY.getCanonicalUrl()))
        .map(Extension::getValue)
        .map(coding -> coding.castToCoding(coding))
        .map(coding -> MedicationCategory.fromCode(coding.getCode()))
        .collect(Collectors.toList());
  }

  public MedicationCategory getCategoryFirstRep() {
    return this.getCatagory().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpMedication.class, ErpStructureDefinition.KBV_MEDICATION_CATEGORY));
  }

  public Optional<MedicationType> getMedicationType() {
    return this.getCode().getCoding().stream()
        .filter(
            coding -> coding.getSystem().equals(ErpCodeSystem.MEDICATION_TYPE.getCanonicalUrl()))
        .map(coding -> MedicationType.fromCode(coding.getCode()))
        .findFirst();
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl()
                    .equals(ErpStructureDefinition.KBV_MEDICATION_VACCINE.getCanonicalUrl()))
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
        .filter(coding -> coding.getSystem().equals(ErpCodeSystem.PZN.getCanonicalUrl()))
        .map(Coding::getCode)
        .collect(Collectors.toList());
  }

  public String getPznFirstRep() {
    return this.getPzn().stream()
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(KbvErpMedication.class, ErpCodeSystem.PZN));
  }

  public String getMedicationName() {
    return this.getCode().getText();
  }

  public List<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .collect(Collectors.toList());
  }

  public Optional<Darreichungsform> getDarreichungsformFirstRep() {
    return this.getDarreichungsform().stream().findFirst();
  }

  public StandardSize getStandardSize() {
    return this.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(ErpStructureDefinition.NORMGROESSE.getCanonicalUrl()))
        .map(ext -> StandardSize.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst()
        .orElse(StandardSize.KA);
  }

  public static KbvErpMedication fromMedication(Medication adaptee) {
    val kbvMedication = new KbvErpMedication();
    adaptee.copyValues(kbvMedication);
    return kbvMedication;
  }

  public static KbvErpMedication fromMedication(Resource adaptee) {
    return fromMedication((Medication) adaptee);
  }
}
