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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;

import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class GemErpMedFreeTextFaker implements GemErpMedicationFaker {

  private final Map<String, Consumer<GemErpMedFreeTextBuilder>> builderConsumers = new HashMap<>();

  public GemErpMedFreeTextFaker() {
    withVersion(ErpWorkflowVersion.getDefaultVersion());
    if (fakerBool()) {
      withCodeText(GemFaker.getFaker().chuckNorris().fact());
    }
    if (fakerBool()) {
      withDrugCategory(EpaDrugCategory.C_00);
    }
    if (fakerBool()) {
      withLotNumber(GemFaker.fakerLotNumber());
    }
    if (fakerBool()) {
      withVaccineTrue(GemFaker.fakerBool());
    }
    if (fakerBool()) {
      withFormText(GemFaker.getFaker().chuckNorris().fact());
    }
  }

  public GemErpMedFreeTextFaker withCodeText(String codeText) {
    builderConsumers.put("codeText", b -> b.codeText(codeText));
    return this;
  }

  public GemErpMedFreeTextFaker withFormText(String formText) {
    builderConsumers.put("formText", b -> b.formText(formText));
    return this;
  }

  public GemErpMedFreeTextFaker withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public GemErpMedFreeTextFaker withDrugCategory(EpaDrugCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public GemErpMedFreeTextFaker withVaccineTrue(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public GemErpMedFreeTextFaker withLotNumber(String lotNumber) {
    builderConsumers.put("lotnumber", b -> b.lotNumber(lotNumber));
    return this;
  }

  public GemErpMedication fake() {
    return this.toBuilder().build();
  }

  public GemErpMedFreeTextBuilder toBuilder() {
    val builder = GemErpMedicationBuilder.forFreeText();
    if (!builderConsumers.containsKey("codeText")) {
      withCodeText(GemFaker.getFaker().chuckNorris().fact());
    }
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
