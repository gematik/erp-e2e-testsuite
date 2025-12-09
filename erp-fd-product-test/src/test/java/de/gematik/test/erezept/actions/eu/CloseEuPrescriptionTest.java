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

package de.gematik.test.erezept.actions.eu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.eu.EuCloseOperationManipulatorFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CloseEuPrescriptionTest extends ErpFhirBuildingTest {

  private static EuPharmacyActor pharmacy;
  private static UseTheErpClient useErpClient;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    pharmacy = new EuPharmacyActor("Hannes Vogt");
    useErpClient = mock(UseTheErpClient.class);
    pharmacy.can(useErpClient);

    EuPharmacyConfiguration cfg = new EuPharmacyConfiguration();
    cfg.setCountryCode("LI");
    pharmacy.can(ProvidePharmacyBaseData.fromConfiguration(cfg));
  }

  @Test
  void shouldBuildActionCorrect() {
    SafeAbility.getAbility(pharmacy, ProvidePharmacyBaseData.class);
    pharmacy.abilityTo(ProvidePharmacyBaseData.class);
    val action =
        CloseEuPrescription.with(EuAccessCode.random(), KVNR.random())
            .withResourceManipulator(
                EuCloseOperationManipulatorFactory.getParametersManipulator().stream()
                    .findFirst()
                    .orElseThrow())
            .withAccepted(KbvErpBundleFaker.builder().fake());
    assertDoesNotThrow(() -> pharmacy.performs(action));
  }
}
