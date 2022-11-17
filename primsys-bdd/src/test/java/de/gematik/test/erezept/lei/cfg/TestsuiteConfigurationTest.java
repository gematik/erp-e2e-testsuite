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

package de.gematik.test.erezept.lei.cfg;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
class TestsuiteConfigurationTest {

  @BeforeEach
  void setUp() {
    log.info("Reset the Singleton before testcase");
    resetSingleton(TestsuiteConfiguration.class, "instance");

    // also reset the SystemProperty
    System.clearProperty("erp.config.activeEnvironment");
  }

  @Test
  void shouldHaveActors() {
    val cfg = TestsuiteConfiguration.getInstance();
    val actors = cfg.getActors();
    assertNotNull(actors);
    assertFalse(actors.getApothecaries().isEmpty());
    assertFalse(actors.getDoctors().isEmpty());
    assertFalse(actors.getPharmacies().isEmpty());
    assertFalse(actors.getPatients().isEmpty());
  }

  @Test
  void shouldHaveTestEnvironments() {
    System.setProperty("erp.config.activeEnvironment", "TU");
    val cfg = TestsuiteConfiguration.getInstance();
    val envs = cfg.getEnvironments();
    assertNotNull(envs);
    assertFalse(envs.isEmpty());
    assertEquals("TU", cfg.getActiveEnvironment().getName());
  }

  @Test
  void shouldHaveKonnektors() {
    val cfg = TestsuiteConfiguration.getInstance();
    val konnektorConfigurations = cfg.getKonnektors();
    assertNotNull(konnektorConfigurations);
    assertFalse(konnektorConfigurations.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"RU", "RU-DEV", "TU"})
  void shouldDetectCustomEnvironmentViaSystemProperty(String env) {
    System.setProperty("erp.config.activeEnvironment", env);
    val cfg = TestsuiteConfiguration.getInstance();
    assertEquals(env, cfg.getActiveEnvironment().getName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ru", "RU-dev", "tu"})
  void shouldDetectCustomEnvironmentViaSystemPropertyCaseInsensitive(String env) {
    System.setProperty("erp.config.activeEnvironment", env);
    val cfg = TestsuiteConfiguration.getInstance();
    assertTrue(
        env.equalsIgnoreCase(cfg.getActiveEnvironment().getName()),
        format("Expected {0} to be equal to {1}", env, cfg.getActiveEnvironment().getName()));
  }

  @Test
  void shouldReplaceSingleIndexedKonnektor() {
    val expected = "TEST-KONN-abc";
    System.setProperty("erp.config.actors.doctors.#0.konnektor", expected);
    val cfg = TestsuiteConfiguration.getInstance();
    val substitutedDoc = cfg.getActors().getDoctors().get(0);
    assertEquals(expected, substitutedDoc.getKonnektor());
  }

  @Test
  void shouldReplaceAllDoctorKonnektors() {
    val expected = "TEST-KONN-abc";
    System.setProperty("erp.config.actors.doctors.*.konnektor", expected);
    val cfg = TestsuiteConfiguration.getInstance();
    cfg.getActors().getDoctors().forEach(dc -> assertEquals(expected, dc.getKonnektor()));
  }

  @Test
  void shouldThrowFetchingUnknownConfiguration() {
    val cfg = TestsuiteConfiguration.getInstance();
    val invalidActorName = "John Doe";
    List<Function<TestsuiteConfiguration, ActorConfiguration>> getConfigFunctions =
        List.of(
            c -> c.getDoctorConfig(invalidActorName),
            c -> c.getPharmacyConfig(invalidActorName),
            c -> c.getPatientConfig(invalidActorName));

    getConfigFunctions.forEach(
        f -> assertThrows(ConfigurationMappingException.class, () -> f.apply(cfg)));
  }

  @Test
  @SneakyThrows
  void shouldHandleSystemPropertiesMixedWithPcsFile() {
    val basePath = Path.of("config", "primsys", "config.yaml");
    val ymlFilePath =
        (basePath.toFile().exists() ? basePath : Path.of("..").resolve(basePath))
            .toAbsolutePath()
            .normalize();

    assertTrue(ymlFilePath.toFile().exists());

    val tmpConfigDir = Files.createTempDirectory(null);
    val tmpConfigCopy = Path.of(String.valueOf(tmpConfigDir.toAbsolutePath()), "config.yaml");
    Files.copy(ymlFilePath, tmpConfigCopy);

    // write the pcs file
    val hostname = InetAddress.getLocalHost().getHostName();
    val pcsFile =
        Path.of(String.valueOf(tmpConfigDir.toAbsolutePath()), format("{0}.pcs", hostname))
            .toFile();
    val expected = "TEST-KONN-abc";
    try (val fw = new FileWriter(pcsFile)) {
      fw.write(format("actors.doctors.*.konnektor={0}", expected));
    }

    // write concurrent system property
    val expectedSysProp = "SYS-PROP-KONN";
    System.setProperty("erp.config.actors.doctors.#0.konnektor", expectedSysProp);

    val cfg = TestsuiteConfiguration.getInstance(tmpConfigCopy.toFile());
    val docZero = cfg.getActors().getDoctors().remove(0);
    assertEquals(
        expectedSysProp, docZero.getKonnektor()); // docZero was overwritten by System Property
    cfg.getActors()
        .getDoctors()
        .forEach(
            dc ->
                assertEquals(expected, dc.getKonnektor())); // all others were substituted from pcs
  }

  /**
   * As Configuration is a Singleton we need to reset the instance after each testcase!
   *
   * @param clazz
   * @param fieldName
   */
  @SneakyThrows
  private static void resetSingleton(Class<?> clazz, String fieldName) {
    Field instance;
    instance = clazz.getDeclaredField(fieldName);
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
