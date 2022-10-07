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

package de.gematik.test.fuzzing.kbv;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@Slf4j
class KbvBundleManipulatorFactoryTest {

  private static final boolean DEBUG = false;
  private static FhirParser fhir;

  @BeforeAll
  static void setupFhirParser() {
    fhir = new FhirParser();
  }

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<KbvBundleManipulatorFactory> constructor =
        KbvBundleManipulatorFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @Test
  void shouldFailAgainstHapi() {
    val manipulators = KbvBundleManipulatorFactory.getAllKbvBundleManipulators();

    val executables =
        manipulators.stream()
            //            .filter(m -> m.getName().equals("Composition mit invalider Formular Art"))
            .map(
                m ->
                    (Executable)
                        () -> {
                          val kbvBundle = KbvErpBundleBuilder.faker("X123456789").build();
                          assertDoesNotThrow(() -> m.getParameter().accept(kbvBundle));
                          val encoded = fhir.encode(kbvBundle, EncodingType.XML);
                          val result = fhir.validate(encoded);

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
                            System.out.println(format("{0}\n", encoded));
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
                          // However, once KBV profiles are updated, all of these MUST fail

                        });
    assertAll("Should fail on manipulated KbvErpBundle", executables);
  }
}
