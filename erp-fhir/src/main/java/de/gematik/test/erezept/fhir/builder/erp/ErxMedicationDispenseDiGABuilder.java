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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.extensions.erp.DeepLink;
import de.gematik.test.erezept.fhir.extensions.erp.RedeemCode;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseDiGA;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public class ErxMedicationDispenseDiGABuilder
    extends ErxMedicationDispenseBaseBuilder<
        ErxMedicationDispenseDiGA, ErxMedicationDispenseDiGABuilder> {

  private RedeemCode redeemCode;
  private DeepLink deepLink;

  protected ErxMedicationDispenseDiGABuilder(KVNR kvnr) {
    super(kvnr);
  }

  public static ErxMedicationDispenseDiGABuilder forKvnr(KVNR kvnr) {
    return new ErxMedicationDispenseDiGABuilder(kvnr);
  }

  public ErxMedicationDispenseDiGABuilder redeemCode(String redeemCode) {
    return redeemCode(RedeemCode.from(redeemCode));
  }

  public ErxMedicationDispenseDiGABuilder redeemCode(RedeemCode redeemCode) {
    this.redeemCode = redeemCode;
    return this;
  }

  public ErxMedicationDispenseDiGABuilder deepLink(String deepLink) {
    return deepLink(DeepLink.from(deepLink));
  }

  public ErxMedicationDispenseDiGABuilder deepLink(DeepLink deepLink) {
    this.deepLink = deepLink;
    return this;
  }

  @Override
  public ErxMedicationDispenseDiGA build() {
    val medDisp =
        this.createResource(
            ErxMedicationDispenseDiGA::new,
            ErpWorkflowStructDef.MEDICATION_DISPENSE_DIGA,
            erpWorkflowVersion);
    buildBase(medDisp);

    val medicationReference = new Reference();
    this.medication
        .getPzn()
        .ifPresent(pzn -> medicationReference.setIdentifier(pzn.asIdentifier()));
    medicationReference.setDisplay(this.medication.getName().orElse(null));
    medDisp.setMedication(medicationReference);

    Optional.ofNullable(deepLink).ifPresent(dl -> medDisp.addExtension(dl.asExtension()));
    Optional.ofNullable(redeemCode).ifPresent(rc -> medDisp.addExtension(rc.asExtension()));

    return medDisp;
  }
}
