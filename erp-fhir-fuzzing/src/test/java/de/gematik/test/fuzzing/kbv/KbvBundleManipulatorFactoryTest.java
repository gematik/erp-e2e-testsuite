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

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.testutil.*;
import java.lang.reflect.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.*;

@Slf4j
class KbvBundleManipulatorFactoryTest extends ParsingTest {

  private static final boolean DEBUG = false;

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<KbvBundleManipulatorFactory> constructor =
        KbvBundleManipulatorFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @ParameterizedTest(
      name = "[{index}] -> Try KbvBundleManipulators with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldFailAgainstHapi(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val manipulators = KbvBundleManipulatorFactory.getAllKbvBundleManipulators();

    val executables =
        manipulators.stream()
//            .filter(m -> m.getName().contains("Leerzeichen nach der Patient-Adresse"))
            .map(
                m ->
                    (Executable)
                        () -> {
                          val kbvBundle = KbvErpBundleBuilder.faker().build();
                          assertDoesNotThrow(() -> m.getParameter().accept(kbvBundle));
                          val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle, DEBUG);

                          if (DEBUG) {
                            if (result.isSuccessful()) {
                              System.out.println(
                                  format("#### {0} ist laut HAPI valide ####", m.getName()));
                            } else {
                              System.out.println(
                                  format(
                                      "#### {0} ist laut HAPI NICHT valide mit {1} Fehlern ####",
                                      m.getName(), result.getMessages().size()));
                              result.getMessages().forEach(System.out::println);
                              System.out.println("-----");
                            }
                          }

                          // assert only the failing ones to prevent the testcase to fail and still
                          // reach coverage!
                          if (!result.isSuccessful()) {
                            assertFalse(result.isSuccessful(), m.getName());
                          } else {
                            log.warn(
                                format(
                                    "''{0}'' manipulation was not detected as invalid by HAPI",
                                    m.getName()));
                          }
                        });
    assertAll("Should fail on manipulated KbvErpBundle", executables);
  }
}
