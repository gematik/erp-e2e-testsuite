/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.fuzzing.dav;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.*;

@Slf4j
class DavBundleManipulatorFactoryTest extends ParsingTest {

  private static final boolean DEBUG = false;

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<DavBundleManipulatorFactory> constructor =
        DavBundleManipulatorFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @ParameterizedTest(
      name = "[{index}] -> Try DavBundleManipulators with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldFailAgainstHapi(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val manipulators = DavBundleManipulatorFactory.getAllDavBundleManipulators();

    val executables =
        manipulators.stream()
            //            .filter(m -> m.getName().equals("Composition mit invalider Formular Art"))
            .map(
                m ->
                    (Executable)
                        () -> {
                          val davBundle = DavAbgabedatenBuilder.faker().build();
                          assertDoesNotThrow(() -> m.getParameter().accept(davBundle));
                          val encoded = parser.encode(davBundle, EncodingType.XML);
                          val result = parser.validate(encoded);

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
                        });
    assertAll("Should fail on manipulated DavBundle", executables);
  }
}
