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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.FhirProfileException;
import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import lombok.val;

public interface ProfileVersion<V extends ProfileVersion<V>> {

  Pattern SEMVER_REGEX = Pattern.compile("([0-9]{1,3}\\.[0-9]+\\.[0-9]+)");

  static String parseVersion(String input) {
    val matcher = SEMVER_REGEX.matcher(input);
    if (!matcher.find()) {
      throw new FhirProfileException(format("Given input does not contain a version: {0}", input));
    }

    return matcher.group(1);
  }

  static <T extends ProfileVersion<?>> T fromString(Class<T> type, String input) {
    val profileVersion = parseVersion(input);
    return Arrays.stream(type.getEnumConstants())
        .filter(version -> version.isEqual(profileVersion))
        .findFirst()
        .orElseThrow(
            () ->
                new FhirProfileException(
                    format(
                        "Profile version {0} is not known for {1}",
                        profileVersion, type.getSimpleName())));
  }

  static <T extends ProfileVersion<T>> T getDefaultVersion(Class<T> type, CustomProfiles profile) {
    if (!profile.getVersionClass().equals(type)) {
      throw new FhirProfileException(
          format(
              "Given profile {0} does match the given Version type {1}",
              profile, type.getSimpleName()));
    }

    val profileName = profile.getName();

    // first read the set default from environment
    val defaultVersionConfig = ParserConfigurations.getInstance().getDefaultConfiguration();
    val envSetProfileVersion = new AtomicReference<String>();
    defaultVersionConfig
        .flatMap(dvc -> dvc.getOptionalVersionedProfile(profileName))
        .ifPresent(d -> envSetProfileVersion.set(d.getVersion()));

    // read from property and overwrite env if property is set
    val definedVersion = System.getProperty(profileName, envSetProfileVersion.get());

    // if a version was defined via env or property, take this one, otherwise choose a default be
    // date or the only one available
    if (definedVersion != null) {
      return fromString(type, definedVersion);
    } else if (type.getEnumConstants().length == 1) {
      // if only one version is available, return this one
      return type.getEnumConstants()[0];
    } else {
      val now = LocalDate.now();
      return Arrays.stream(type.getEnumConstants())
          .filter(
              version ->
                  (now.isAfter(version.getValidFromDate())
                          && now.isBefore(version.getValidUntilDate()))
                      || now.isEqual(version.getValidFromDate())
                      || now.isEqual(version.getValidUntilDate()))
          .findFirst()
          .orElseThrow(
              () ->
                  new RuntimeException(
                      format(
                          "Unable to determine a valid profile version for {0} which is valid on {1}",
                          profile, now)));
    }
  }

  CustomProfiles getCustomProfile();

  /**
   * Define the starting date from when on the testsuite shall use the profile version as default
   * <b>for creation of fhir resources within the builders</b>
   *
   * @return start date
   */
  LocalDate getValidFromDate();

  /**
   * Define the final date until when the testsuite shall use the profile version as default <b>for
   * creation of fhir resources within the builders</b>
   *
   * @return final date
   */
  LocalDate getValidUntilDate();

  String getVersion();

  default boolean isEqual(String version) {
    return compareTo(version) == 0;
  }

  default int compareTo(V o) {
    return compareTo(o.getVersion());
  }

  default int compareTo(String version) {
    var otherVersion = version.trim();
    var myVersion = this.getVersion().trim();

    if (otherVersion.split("\\.").length == 2) {
      // sometimes we only have version 1.1 make a SemVer by appending a zero -> 1.1.0
      otherVersion = format("{0}.0", otherVersion);
    }

    if (myVersion.split("\\.").length == 2) {
      myVersion = format("{0}.0", myVersion);
    }

    val otherTokens = otherVersion.split("\\.");
    val myTokens = myVersion.split("\\.");
    for (var i = 0; i < otherTokens.length; i++) {
      val ovt = Integer.parseInt(otherTokens[i]); // other version token
      val mvt = Integer.parseInt(myTokens[i]); // my version token
      if (ovt != mvt) {
        return (mvt < ovt) ? -1 : 1;
      }
    }
    return 0;
  }
}
