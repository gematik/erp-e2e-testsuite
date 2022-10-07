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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleHasValidAccessCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrescriptionBundleVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldPassOnValidAccessCode() {
    val erxTask = new ErxTask();
    erxTask.addIdentifier(AccessCode.random().asIdentifier());
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    step.apply(prescriptionBundle);
  }

  @Test
  void shouldFailOnMissingAccessCode() {
    val erxTask = new ErxTask();
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }

  @Test
  void shouldFailOnInvalidAccessCode() {
    val erxTask = new ErxTask();
    erxTask.addIdentifier(new AccessCode("affe").asIdentifier());
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }
}
