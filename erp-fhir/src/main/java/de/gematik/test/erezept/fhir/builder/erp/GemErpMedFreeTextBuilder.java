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

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedFreeTextBuilder extends GemErpMedicationBuilder<GemErpMedFreeTextBuilder> {

  @Override
  public GemErpMedication build() {
    checkRequiredInFreeText();
    val medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);
    applyCommonFields(medication);

    return medication;
  }

  public GemErpMedFreeTextBuilder codeText(String codeText) {
    this.codeText = codeText;
    return self();
  }

  public GemErpMedFreeTextBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }

  private void checkRequiredInFreeText() {
    if (codeText != null && codeText.isBlank() || formText != null && formText.isBlank()) {
      val msg = "this Medication is made up of the text you forgot to set";
      throw new BuilderException(msg);
    }
  }
}
