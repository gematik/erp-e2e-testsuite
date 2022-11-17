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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.client.cfg.INamedConfiguration;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.lei.cfg.util.PartialConfigSubstituter;
import de.gematik.test.erezept.pspwsclient.config.PSPClientConfig;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorConfiguration;
import de.gematik.test.konnektor.cfg.LocalKonnektorConfiguration;
import de.gematik.test.konnektor.cfg.RemoteKonnetorConfiguration;
import de.gematik.test.konnektor.exceptions.MissingKonnektorKonfigurationException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@Slf4j
public class TestsuiteConfiguration {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  static {
    OBJECT_MAPPER.registerSubtypes(new NamedType(RemoteKonnetorConfiguration.class, "remote"));
    OBJECT_MAPPER.registerSubtypes(new NamedType(LocalKonnektorConfiguration.class, "local"));
  }

  private static final String PRODUCT_NAME = "primsys";
  private static final String CONFIG_YAML = "config.yaml";

  private static TestsuiteConfiguration instance;

  private String activeEnvironment;

  private boolean preferManualSteps = false;

  private PSPClientConfig pspClientConfig;

  private ActorsConfiguration actors;
  private List<EnvironmentConfiguration> environments;
  private List<KonnektorConfiguration> konnektors;

  public static TestsuiteConfiguration getInstance() {
    val basePath = Path.of("config", PRODUCT_NAME, CONFIG_YAML);
    val ymlFilePath =
        (basePath.toFile().exists() ? basePath : Path.of("..").resolve(basePath))
            .toAbsolutePath()
            .normalize();

    return getInstance(ymlFilePath.toFile());
  }

  @SneakyThrows
  public static TestsuiteConfiguration getInstance(File ymlFile) {
    if (instance == null) {
      val configTemplate = readFile(ymlFile);
      val finalConfig = PartialConfigSubstituter.applyUpdates(ymlFile, configTemplate);

      instance = OBJECT_MAPPER.readValue(finalConfig.toString(), TestsuiteConfiguration.class);

      log.info(
          format(
              "Read configuration from {0} with configured environment {1} ",
              ymlFile.getAbsolutePath(), instance.activeEnvironment));
    }

    return instance;
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return this.getConfig(this.activeEnvironment, this.environments);
  }

  public KonnektorConfiguration getKonnektorConfig(String name) {
    return konnektors.stream()
        .filter(konnektor -> konnektor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new MissingKonnektorKonfigurationException(name));
  }

  private Konnektor instantiateKonnektorClient(String name) {
    return this.getKonnektorConfig(name).create();
  }

  public Konnektor instantiateDoctorKonnektor(DoctorConfiguration docConfig) {
    return instantiateKonnektorClient(docConfig.getKonnektor());
  }

  public Konnektor instantiatePharmacyKonnektor(PharmacyConfiguration pharmacyConfig) {
    return instantiateKonnektorClient(pharmacyConfig.getKonnektor());
  }

  public Konnektor instantiateApothecaryKonnektor(ApothecaryConfiguration apothecaryConfig) {
    return instantiateKonnektorClient(apothecaryConfig.getKonnektor());
  }

  public DoctorConfiguration getDoctorConfig(String name) {
    return getConfig(name, this.actors.getDoctors());
  }

  public PharmacyConfiguration getPharmacyConfig(String name) {
    return getConfig(name, this.actors.getPharmacies());
  }

  public ApothecaryConfiguration getApothecaryConfig(String name) {
    return getConfig(name, this.actors.getApothecaries());
  }

  public PatientConfiguration getPatientConfig(String name) {
    return getConfig(name, this.actors.getPatients());
  }

  private <T extends INamedConfiguration> T getConfig(String name, List<T> configs) {
    return configs.stream()
        .filter(actor -> actor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    name, configs.stream().map(INamedConfiguration::getName).toList()));
  }

  @SneakyThrows
  private static JsonNode readFile(File file) {
    return OBJECT_MAPPER.readTree(file);
  }
}
