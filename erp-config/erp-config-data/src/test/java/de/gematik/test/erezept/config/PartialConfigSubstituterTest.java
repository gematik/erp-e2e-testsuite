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

package de.gematik.test.erezept.config;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpAppConfigurationBase;
import de.gematik.test.erezept.config.exceptions.PcsExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class PartialConfigSubstituterTest {

  @Test
  @ClearSystemProperty(key = "erp.primsys.name")
  void shouldReplaceValueViaSystemProperty() throws IOException {
    val confPath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/pharmacy_am_flughafen.yaml")
                .getPath());

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(confPath.toFile());

    System.setProperty("erp.primsys.name", "TEST");
    val substituted =
        PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS)
            .applyUpdates(confPath.toFile(), configTemplate)
            .toString();

    val conf = om.readValue(substituted, PharmacyConfiguration.class);

    assertEquals("TEST", conf.getName());
  }

  @Test
  @ClearSystemProperty(key = "erp.app.apps.#0.appFile")
  void shouldReplaceValuesContainingEqualSign() throws IOException {
    val confPath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/app_config.yaml")
                .getPath());

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(confPath.toFile());

    System.setProperty("erp.app.apps.#0.appFile", "cloud:uniqueName=202317071200_1.11.0-1");
    val substituted =
        PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_APP)
            .applyUpdates(confPath.toFile(), configTemplate)
            .toString();

    val conf = om.readValue(substituted, ErpAppConfigurationBase.class);
    assertEquals("cloud:uniqueName=202317071200_1.11.0-1", conf.getApps().get(0).getAppFile());
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.name")
  void shouldReplaceValueViaPcsFileAndSystemProperty() throws IOException {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/pharmacy_am_flughafen.yaml")
                .getPath());

    val outPath =
        Path.of(
            "target",
            "configurations",
            format("{0}", System.currentTimeMillis()),
            "pcs_replace_01");
    outPath.toFile().mkdirs();

    // prepare the config and the PCS file
    val configFile = Path.of(outPath.toString(), "config.yaml").toFile();
    copyFile(templatePath.toFile(), configFile.toPath());
    val pcsFile = Path.of(outPath.toString(), format("{0}.pcs", getHostName()));
    writeTestFile(
        pcsFile.toFile(), "#comment line following empty line\n\nname=TEST\nsmcbIccsn=123");

    // set the system property
    System.setProperty("erp.primsys.name", "TEST_123");

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(templatePath.toFile());

    val substituted =
        PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS)
            .applyUpdates(configFile, configTemplate)
            .toString();

    val conf = om.readValue(substituted, PharmacyConfiguration.class);

    assertNotEquals("TEST", conf.getName()); // not the one from PCS file
    assertEquals("TEST_123", conf.getName()); // but from system property wich overrides pcs
    assertEquals("123", conf.getSmcbIccsn()); // from pcs
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.actors.doctors.#3.name")
  void shouldThrowOnInvalidPcsExpression() throws IOException {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/actors_list.yaml")
                .getPath());

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(templatePath.toFile());

    System.setProperty("erp.primsys.actors.doctors.#3.name", "TEST");
    val templateFile = templatePath.toFile();
    val pcs = PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS);
    assertThrows(
        PcsExpressionException.class, () -> pcs.applyUpdates(templateFile, configTemplate));
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.actors.doctors.*.konnektor")
  void shouldReplaceOnAllElements() throws IOException {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/actors_list.yaml")
                .getPath());

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(templatePath.toFile());

    System.setProperty("erp.primsys.actors.doctors.*.konnektor", "TEST");
    val templateFile = templatePath.toFile();
    val substituted =
        PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS)
            .applyUpdates(templateFile, configTemplate);
    val doctors = substituted.findValues("doctors");
    doctors.forEach(doc -> assertEquals("TEST", doc.findValue("konnektor").asText()));
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.actors.doctors.*.konnektor")
  void shouldNullifyElements() throws IOException {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/actors_list.yaml")
                .getPath());

    val om = new ObjectMapper(new YAMLFactory());
    val configTemplate = om.readTree(templatePath.toFile());

    System.setProperty("erp.primsys.actors.doctors.*.konnektor", "NULL");
    val templateFile = templatePath.toFile();
    val substituted =
        PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS)
            .applyUpdates(templateFile, configTemplate);
    val doctors = substituted.findValues("doctors");
    doctors.forEach(doc -> assertTrue(doc.findValue("konnektor").isNull()));
  }

  @SneakyThrows
  private void writeTestFile(File file, String content) {
    try (val fw = new FileWriter(file)) {
      fw.write(content);
      fw.flush();
    }
  }

  @SneakyThrows
  private void copyFile(File source, Path target) {
    Files.copy(source.toPath(), target);
  }

  private static String getHostName() {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostName = UUID.randomUUID().toString(); // just return something random!
    }

    return hostName.split("\\.")[0];
  }
}
