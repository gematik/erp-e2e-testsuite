/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.fuzzing.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class MvoExtensionManipulatorFactoryTest {

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<MvoExtensionManipulatorFactory> constructor =
        MvoExtensionManipulatorFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @ParameterizedTest(
      name = "[{index}] -> Try MVO KbvBundleManipulators with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void getMvoExtensionKennzeichenFalsifier(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val manipulators = MvoExtensionManipulatorFactory.getMvoExtensionKennzeichenFalsifier();

    manipulators.forEach(
        m -> {
          val patient = PatientBuilder.faker(VersicherungsArtDeBasis.GKV).build();
          val coverage = KbvCoverageBuilder.faker(VersicherungsArtDeBasis.GKV).build();
          val practitioner = PractitionerBuilder.faker().build();
          val medication =
              KbvErpMedicationBuilder.faker().category(MedicationCategory.C_00).build();

          val kbvBundle =
              KbvErpBundleBuilder.faker(patient.getGkvId().orElseThrow())
                  .medication(medication)
                  .medicationRequest(
                      MedicationRequestBuilder.faker(patient)
                          .medication(medication)
                          .insurance(coverage)
                          .requester(practitioner)
                          .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).validThrough(0, 365))
                          .build())
                  .patient(patient)
                  .insurance(coverage)
                  .build();

          assertDoesNotThrow(() -> m.getParameter().accept(kbvBundle));
        });
  }
}
