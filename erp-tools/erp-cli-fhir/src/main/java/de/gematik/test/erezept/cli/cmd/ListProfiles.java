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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations.ProfileSettingConfig;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ProfilesIndex;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpBasisVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.DavKbvCsVsVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.DeBasisVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.Hl7Version;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvBasisVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import java.nio.file.Path;
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
    val parsers = ParserConfigurations.getInstance();
    val profiles = ProfilesIndex.getInstance();

    if (profilesGroup != null && profilesGroup.showProfiles) {
      printProfilesConfiguration(parsers, profiles);
    } else {
      printDefaults(parsers);
    }

    return 0;
  }

  private void printProfilesConfiguration(ParserConfigurations parsers, ProfilesIndex profiles) {
    parsers
        .getProfileSettings()
        .forEach(
            profileSettingConfig -> {
              printProfileSetting(profileSettingConfig, false);
              profileSettingConfig
                  .getProfiles()
                  .forEach(
                      profile -> {
                        System.out.println(
                            format("\t{0} {1}", profile.getName(), profile.getVersion()));
                        if (profilesGroup.showSpecFiles) {
                          profiles.getProfile(profile.getVersionedProfile()).getFiles().stream()
                              .map(filePath -> Path.of(filePath).getFileName().toString())
                              .forEach(
                                  file -> {
                                    System.out.println(format("\t\t{0}", file));
                                  });
                        }
                      });
            });
  }

  private void printDefaults(ParserConfigurations parsers) {
    parsers
        .getDefaultConfiguration()
        .ifPresentOrElse(
            profileSettingConfig -> {
              printProfileSetting(profileSettingConfig, true);
              profileSettingConfig
                  .getProfiles()
                  .forEach(
                      profileDto ->
                          printDefaultVersion(CustomProfiles.fromName(profileDto.getName())));
            },
            () -> printFallbackSettings(parsers));
  }

  private void printFallbackSettings(ParserConfigurations configurations) {
    System.out.println(
        format(
            "No ProfileSetting is configured via Environment or System Property!\n"
                + "try one of: \n"
                + "export {0}={1}\n"
                + "-D{0}={2}\n",
            ParserConfigurations.ENV_TOGGLE,
            configurations.getProfileSettings().get(0).getId(),
            ParserConfigurations.SYS_PROP_TOGGLE));

    System.out.println("Profile fallbacks:");
    val latest =
        configurations.getProfileSettings().get(configurations.getProfileSettings().size() - 1);
    latest
        .getProfiles()
        .forEach(profileDto -> printDefaultVersion(CustomProfiles.fromName(profileDto.getName())));
  }

  private void printProfileSetting(ProfileSettingConfig profileSettingConfig, boolean markDefault) {
    var profileSetting = format("Profile setting: {0}", profileSettingConfig.getId());

    if (markDefault) {
      if (System.getProperty(ParserConfigurations.SYS_PROP_TOGGLE) != null) {
        profileSetting =
            format(
                "{0} (from system property {1})",
                profileSetting, ParserConfigurations.SYS_PROP_TOGGLE);
      } else if (System.getenv(ParserConfigurations.ENV_TOGGLE) != null) {
        profileSetting =
            format("{0} (from environment {1})", profileSetting, ParserConfigurations.ENV_TOGGLE);
      }
    }

    System.out.println(
        format("{0}\nDescription: {1}", profileSetting, profileSettingConfig.getNote()));
  }

  private void printDefaultVersion(CustomProfiles customProfile) {
    val defaultVersion =
        switch (customProfile) {
          case DE_BASIS_PROFIL_R4:
            yield getDefaultVersion(DeBasisVersion.class, customProfile);
          case KBV_BASIS:
            yield getDefaultVersion(KbvBasisVersion.class, customProfile);
          case KBV_ITA_FOR:
            yield getDefaultVersion(KbvItaForVersion.class, customProfile);
          case KBV_ITA_ERP:
            yield getDefaultVersion(KbvItaErpVersion.class, customProfile);
          case GEM_ERP_WORKFLOW:
            yield getDefaultVersion(ErpWorkflowVersion.class, customProfile);
          case GEM_PATIENTENRECHNUNG:
            yield getDefaultVersion(PatientenrechnungVersion.class, customProfile);
          case ABDA_ERP_BASIS:
            yield getDefaultVersion(AbdaErpBasisVersion.class, customProfile);
          case ABDA_ERP_ABGABE_PKV:
            yield getDefaultVersion(AbdaErpPkvVersion.class, customProfile);
          case DAV_KBV_CS_VS:
            yield getDefaultVersion(DavKbvCsVsVersion.class, customProfile);
          case HL7:
            yield getDefaultVersion(Hl7Version.class, customProfile);
        };

    System.out.println(format("\t{0}", defaultVersion));
  }

  private <T extends ProfileVersion<T>> String getDefaultVersion(
      Class<T> type, CustomProfiles profile) {
    val version = ProfileVersion.getDefaultVersion(type, profile).getVersion();
    if (System.getProperty(profile.getName()) != null) {
      val versionOverwritten = format("{0} (from -D{1})", version, profile.getName());
      return format("x {0} {1}", profile.getName(), versionOverwritten);
    } else {
      return format("{0} {1}", profile.getName(), version);
    }
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
