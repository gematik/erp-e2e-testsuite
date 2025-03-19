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
 */

package de.gematik.test.erezept.cli.cmd;

import static de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory.ERP_FHIR_PROFILES_CONFIG;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory;
import java.util.concurrent.Callable;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "lsp", description = "list available profiles", mixinStandardHelpOptions = true)
public class ListProfiles implements Callable<Integer> {

  @ArgGroup(exclusive = false, heading = "Show configured FHIR Profiles%n")
  private FhirProfilesGroup profilesGroup;

  @Override
  public Integer call() throws Exception {
    val configuration =
        ProfilesConfigurator.getConfiguration(
            ERP_FHIR_PROFILES_CONFIG, ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE);

    // TODO: re-implement with the new configuration structure
    configuration
        .getProfileConfigurations()
        .forEach(
            cfg -> {
              System.out.println(format("Profile: {0} ({1})", cfg.getId(), cfg.getNote()));
              cfg.getProfiles()
                  .forEach(
                      p -> {
                        System.out.println(format("\t{0} : {1}", p.getName(), p.getVersion()));
                      });
            });

    return 0;
  }

  static class FhirProfilesGroup {
    @Option(
        names = {"-a", "--all"},
        type = Boolean.class,
        required = true,
        description = "Show all configured FHIR profiles")
    private boolean showProfiles = false;

    @Option(
        names = {"-l", "--long"},
        type = Boolean.class,
        description = "Show also the FHIR specification files for each profile")
    private boolean showSpecFiles = false;
  }
}
