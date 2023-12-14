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

package de.gematik.test.erezept.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.config.dto.BaseConfigurationDto;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.app.ErpAppConfigurationBase;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorModuleConfigurationDto;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings("java:S119")
public class ConfigurationReader<DTO extends BaseConfigurationDto> {

  private final Class<DTO> type;
  private final Path configPath;
  private final ConfigurationScope scope;
  private final List<NamedType> subtypes;

  private ConfigurationReader(Builder<DTO> builder) {
    this.type = builder.type;
    this.configPath = builder.configPath;
    this.scope = builder.scope;
    this.subtypes = builder.subtypes;
  }

  @SneakyThrows
  private DTO create(boolean withPcs) {
    val om = new ObjectMapper(new YAMLFactory());

    subtypes.forEach(om::registerSubtypes);

    val configTemplate = om.readTree(configPath.toFile());

    String finalConfig;
    if (withPcs) {
      finalConfig =
          PartialConfigSubstituter.forScope(this.scope)
              .applyUpdates(configPath.toFile(), configTemplate)
              .toString();
    } else {
      finalConfig = configTemplate.toString();
    }
    return om.readValue(finalConfig, type);
  }

  /**
   * shorthand factory method for
   * ConfigurationFactory.forConfiguration(ErpAppConfigurationBase.class, "erp-app")
   *
   * @return Builder for the App-Testsuite Configuration
   */
  public static Builder<ErpAppConfigurationBase> forAppConfiguration() {
    return forConfiguration(ErpAppConfigurationBase.class, TestsuiteconfigurationScope.ERP_APP);
  }

  /**
   * shorthand factory method for
   * ConfigurationFactory.forConfiguration(PrimsysConfigurationBase.class, "primsys")
   *
   * @return Builder for the E2E-Testsuite Configuration
   */
  public static Builder<PrimsysConfigurationDto> forPrimSysConfiguration() {
    return forConfiguration(
        PrimsysConfigurationDto.class, TestsuiteconfigurationScope.ERP_PRIMSYS);
  }

  public static Builder<KonnektorModuleConfigurationDto> forKonnektorClient() {
    return forConfiguration(KonnektorModuleConfigurationDto.class, ModuleConfigurationScope.KONNEKTOR_CLIENT);
  }

  private static <BDTO extends BaseConfigurationDto> Builder<BDTO> forConfiguration(
      Class<BDTO> type, ConfigurationScope scope) {
    return new Builder<>(type, calculateDefaultPath(scope), scope);
  }

  private static Path calculateDefaultPath(ConfigurationScope scope) {
    val basePath = Path.of("config", scope.getDefaultDirectoryName(), "config.yaml");
    return (basePath.toFile().exists() ? basePath : Path.of("..").resolve(basePath))
            .toAbsolutePath()
            .normalize();
  }

  public static class Builder<BDTO extends BaseConfigurationDto> {

    private final Class<BDTO> type;
    private final List<NamedType> subtypes;
    private final ConfigurationScope scope;
    private Path configPath;

    private Builder(Class<BDTO> type, Path configPath, ConfigurationScope scope) {
      this.type = type;
      this.configPath = configPath;
      this.subtypes = new LinkedList<>();
      this.scope = scope;
    }

    public Builder<BDTO> configFile(Path path) {
      if (path.toFile().isFile()) {
        this.configPath = path;
      } else {
        this.configPath = Path.of(path.toString(), "config.yaml");
      }

      return this;
    }

    public Builder<BDTO> registerSubtype(Class<?> type, String name) {
      return registerSubtype(new NamedType(type, name));
    }

    public Builder<BDTO> registerSubtype(NamedType type) {
      this.subtypes.add(type);
      return this;
    }

    public BDTO create() {
      return create(true);
    }

    public BDTO create(boolean withPcs) {
      val factory = new ConfigurationReader<>(this);
      return factory.create(withPcs);
    }

    public <T extends ConfiguredFactory> T wrappedBy(
        Function<BDTO, T> constructor, boolean withPcs) {
      return constructor.apply(this.create(withPcs));
    }

    public <T extends ConfiguredFactory> T wrappedBy(Function<BDTO, T> constructor) {
      return constructor.apply(this.create());
    }
  }
}
