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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.getFaker;

import de.gematik.test.erezept.fhir.extensions.erp.DeepLink;
import de.gematik.test.erezept.fhir.extensions.erp.RedeemCode;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseDiGA;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;

public class ErxMedicationDispenseDiGAFaker
    extends ErxMedicationDispenseBaseFaker<
        ErxMedicationDispenseDiGA,
        ErxMedicationDispenseDiGAFaker,
        ErxMedicationDispenseDiGABuilder> {

  private ErxMedicationDispenseDiGAFaker() {
    super();
    withDeepLink(DeepLink.random());
    withRedeemCode(RedeemCode.random());
    withPrescriptionId(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162));
    withPzn(PZN.random().getValue(), getFaker().app().name());
  }

  public static ErxMedicationDispenseDiGAFaker builder() {
    return new ErxMedicationDispenseDiGAFaker();
  }

  public ErxMedicationDispenseDiGAFaker withPzn(String pzn, String digaName) {
    val newMedication = GemErpMedicationFaker.builder().withPzn(PZN.from(pzn), digaName).fake();
    builderConsumers.put("medication", b -> b.medication(newMedication));
    return this;
  }

  public ErxMedicationDispenseDiGAFaker withRedeemCode(String redeemCode) {
    return withRedeemCode(RedeemCode.from(redeemCode));
  }

  public ErxMedicationDispenseDiGAFaker withRedeemCode(RedeemCode redeemCode) {
    builderConsumers.put("redeemcode", b -> b.redeemCode(redeemCode));
    return this;
  }

  public ErxMedicationDispenseDiGAFaker withDeepLink(String deepLink) {
    return withDeepLink(DeepLink.from(deepLink));
  }

  public ErxMedicationDispenseDiGAFaker withDeepLink(DeepLink deepLink) {
    builderConsumers.put("deeplink", b -> b.deepLink(deepLink));
    return this;
  }

  @Override
  public ErxMedicationDispenseDiGABuilder toBuilder() {
    val builder = ErxMedicationDispenseDiGABuilder.forKvnr(kvnr);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
