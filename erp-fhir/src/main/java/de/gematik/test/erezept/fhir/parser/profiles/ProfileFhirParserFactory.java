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

package de.gematik.test.erezept.fhir.parser.profiles;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ProfilesIndex;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Configuration;

@Slf4j
public class ProfileFhirParserFactory {

  private static List<FhirProfiledValidator> profiledParsers;

  private ProfileFhirParserFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static List<FhirProfiledValidator> getProfiledValidators() {
    if (profiledParsers == null) {
      profiledParsers = createProfiledValidators();
    }

    return profiledParsers;
  }

  @SneakyThrows
  private static List<FhirProfiledValidator> createProfiledValidators() {
    val profilesIndex = ProfilesIndex.getInstance();
    val parserConfigurations = ParserConfigurations.getInstance();

    Configuration.setAcceptInvalidEnums(true); // can be made configurable if required

    List<FhirProfiledValidator> parsers = new LinkedList<>();

    parserConfigurations
        .getProfileSettings()
        .forEach(
            parserCfg -> {
              val ctx = FhirContext.forR4();
              val supports =
                  parserCfg.getProfiles().stream()
                      .map(profile -> profilesIndex.getProfile(profile.getVersionedProfile()))
                      .map(
                          profileSourceDto ->
                              (IValidationSupport)
                                  new ValidationSupport(
                                      profileSourceDto.getVersionedProfile(),
                                      profileSourceDto.getFiles(),
                                      ctx))
                      .collect(Collectors.toList());
              parsers.add(new FhirProfiledValidator(parserCfg, ctx, supports));
            });

    return parsers;
  }
}
