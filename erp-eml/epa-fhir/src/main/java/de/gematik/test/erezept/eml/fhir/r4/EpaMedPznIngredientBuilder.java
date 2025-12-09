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

package de.gematik.test.erezept.eml.fhir.r4;

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.SNOMED_SCT;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaMedPznIngredientBuilder
    extends ResourceBuilder<EpaMedPznIngredient, EpaMedPznIngredientBuilder> {

  private PZN pzn;
  private EpaMedicationVersion version = EpaMedicationVersion.getDefaultVersion();
  private String textInCoding;
  @Nullable private String displayInPzn;
  @Nullable private CodeableConcept codeableCocept;
  private GemEpaIngredientComponentBuilder ingredientCodeBuilder =
      GemEpaIngredientComponentBuilder.builder();
  private boolean shouldBuildIngredientComponent = false;
  private boolean withVersion = true;

  public static EpaMedPznIngredientBuilder builder() {
    return new EpaMedPznIngredientBuilder();
  }

  public EpaMedPznIngredientBuilder pzn(PZN pzn) {
    return pzn(pzn, null);
  }

  public EpaMedPznIngredientBuilder codingText(String textInCoding) {
    this.textInCoding = textInCoding;
    return this;
  }

  public EpaMedPznIngredientBuilder pzn(PZN pzn, String displayInPzn) {
    this.pzn = pzn;
    this.displayInPzn = displayInPzn;
    return this;
  }

  public EpaMedPznIngredientBuilder pzn(CodeableConcept codeableConcept) {
    this.codeableCocept = codeableConcept;
    return this;
  }

  public EpaMedPznIngredientBuilder version(EpaMedicationVersion version) {
    this.version = version;
    return this;
  }

  @Override
  public EpaMedPznIngredient build() {
    checkRequiredOneOfTwoNotBoth(
        pzn,
        codeableCocept,
        "Minimum of suitable Entries is a CodeableConcept with Coding or a Pzn to build a"
            + " CodeableConcept");
    EpaMedPznIngredient epaMed;
    if (withVersion) {
      epaMed =
          this.createResource(
              EpaMedPznIngredient::new, EpaMedicationStructDef.MEDICATION_PZN_INGREDIENT, version);
    } else {
      epaMed =
          this.createResource(
              EpaMedPznIngredient::new, EpaMedicationStructDef.MEDICATION_PZN_INGREDIENT);
    }
    epaMed.addExtension(createSnomedExt());
    Optional.ofNullable(this.pzn).ifPresent(pz -> epaMed.getCode().addCoding(pz.asCoding()));
    Optional.ofNullable(this.displayInPzn)
        .ifPresent(disp -> epaMed.getCode().getCoding().get(0).setDisplay(disp));
    Optional.ofNullable(this.textInCoding).ifPresent(text -> epaMed.getCode().setText(text));
    Optional.ofNullable(codeableCocept).ifPresent(epaMed::setCode);
    if (shouldBuildIngredientComponent) epaMed.addIngredient(ingredientCodeBuilder.build());
    return epaMed;
  }

  private static Extension createSnomedExt() {
    // this extention follows the instruction of: Transformationsregel F_017
    // https://wiki.gematik.de/display/B714ERPFD/Transformationsregel+F_017
    val snomedExt = new Extension();
    snomedExt.setUrl(EpaMedicationStructDef.EPA_MED_TYPE_EXT.getCanonicalUrl());
    val snomedCode = new Coding();
    snomedCode.setSystem(SNOMED_SCT.getCanonicalUrl());
    snomedCode.setVersion("http://snomed.info/sct/900000000000207008/version/20240201");
    snomedCode.setCode("781405001");
    snomedCode.setDisplay("Medicinal product package (product)");
    snomedExt.setValue(snomedCode);
    return snomedExt;
  }

  protected final <T> void checkRequiredOneOfTwoNotBoth(T obj1, T obj2, String errorMsg) {
    if (obj1 == null && obj2 == null) {
      val prefixedErrorMsg = format("Missing required property: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
    if (obj1 != null && obj2 != null) {
      val prefixedErrorMsg = format("to much properties, one is Max: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  /**
   * EpaIngredient Object is not mandatory, but if you like to set the Darreichungsform you need a
   * IngredientItem, too. To realise this constrain the easiest way is to set the Item CC. Text,
   *
   * @param darreichungsformText
   * @param ingredientItemText
   * @return builder
   */
  public EpaMedPznIngredientBuilder darreichungsform(
      String darreichungsformText, String ingredientItemText) {
    ingredientCodeBuilder
        .darreichungsform(darreichungsformText)
        .ingredientCodingText(ingredientItemText);
    shouldBuildIngredientComponent = true;
    return this;
  }

  public EpaMedPznIngredientBuilder withoutVersion() {
    this.withVersion = false;
    return this;
  }
}
