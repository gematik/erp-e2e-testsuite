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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargeItemBundleVerifierTest extends ParsingTest {

  private static final String CHARGE_ITEM_BUNDLE_PATH_FROM_JSON =
      "fhir/valid/erp/1.2.0/chargeitembundle/7e779557-7b6e-49df-b20f-9c2c9c1e0afb.json";
  private final String invalidID = "123.TEST";
  private static final String VALID_ID = "200.000.003.320.338.84";

  private static ErxChargeItemBundle getDecodedChargeItemBundle() {
    return parser.decode(
        ErxChargeItemBundle.class,
        ResourceLoader.readFileFromResource(CHARGE_ITEM_BUNDLE_PATH_FROM_JSON));
  }

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ChargeItemBundleVerifier.class));
  }

  @Test
  void shouldValidateChargeItemIdIsEqual() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.chargeItemIdIsEqualTo(PrescriptionId.from(VALID_ID));
    step.apply(validChargeItemBundle);
  }

  @Test
  void shouldThrowChargeItemIdIsEqual() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.chargeItemIdIsEqualTo(PrescriptionId.from(invalidID));
    assertThrows(AssertionError.class, () -> step.apply(validChargeItemBundle));
  }

  @Test
  void shouldValidateChargeItemIdNotEquals() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.chargeItemIdIsNotEqualTo(PrescriptionId.from(invalidID));
    step.apply(validChargeItemBundle);
  }

  @Test
  void shouldThrowChargeItemIdNotEquals() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.chargeItemIdIsNotEqualTo(PrescriptionId.from(VALID_ID));
    assertThrows(AssertionError.class, () -> step.apply(validChargeItemBundle));
  }

  @Test
  void shouldValidatePrescriptionIdNotEquals() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.prescriptionIdIsEqualTo(TaskId.from(VALID_ID));
    step.apply(validChargeItemBundle);
  }

  @Test
  void shouldThrowPrescriptionIdNotEquals() {
    val validChargeItemBundle = getDecodedChargeItemBundle();
    val step = ChargeItemBundleVerifier.prescriptionIdIsEqualTo(TaskId.from(invalidID));
    assertThrows(AssertionError.class, () -> step.apply(validChargeItemBundle));
  }
}
