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
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedicationPZNBuilder
    extends GemErpMedicationBuilder<GemErpMedicationPZNBuilder> {

  @Override
  public GemErpMedication build() {
    checkRequiredInPZN();
    val medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);
    applyCommonFields(medication);

    return medication;
  }

  public GemErpMedicationPZNBuilder pzn(PZN pzn, String name) {
    this.codes.add(pzn.asCoding().setDisplay(name));
    return self();
  }

  public GemErpMedicationPZNBuilder pzn(String pzn) {
    return pzn(PZN.from(pzn), "unknown");
  }

  public GemErpMedicationPZNBuilder pzn(PZN pzn) {
    return pzn(pzn, "unknown");
  }

  public GemErpMedicationPZNBuilder packagingSize(String packagingSize) {
    this.packagingSize = packagingSize;
    return self();
  }

  public GemErpMedicationPZNBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedicationPZNBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return self();
  }

  public GemErpMedicationPZNBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return self();
  }

  public GemErpMedicationPZNBuilder darreichungsform(Darreichungsform form) {
    this.kbvDdarreichungsform = form;
    return self();
  }

  public GemErpMedicationPZNBuilder codeText(String codeText) {
    this.codeText = codeText;
    return self();
  }

  public GemErpMedicationPZNBuilder normgroesse(StandardSize size) {
    this.normSizeCode = size;
    return self();
  }

  private void checkRequiredInPZN() {

    if (kbvDdarreichungsform != null && kbvDdarreichungsform.equals(Darreichungsform.KPG)) {
      val msg =
          "This is the wrong Builder for a 'Kombipackung' KBV_CS_SFHIR_KBV_DARREICHUNGSFORM == KPG";
      throw new BuilderException(msg);
    }
  }
}
