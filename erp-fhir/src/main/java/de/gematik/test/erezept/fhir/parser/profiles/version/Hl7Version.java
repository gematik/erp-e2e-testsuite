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

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.time.LocalDate;
import java.time.Month;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The HL7 Version is only required to satisfy the Generic of Hl7StructDef the concrete Version
 * shall never be required
 */
@Getter
@RequiredArgsConstructor
public enum Hl7Version implements ProfileVersion<Hl7Version> {
  /*
  Note: validFromDate and validUntilDate are only required to distinguish the proper Version via current date.
  This won't be needed for this profile thus valid throughout the whole time
   */
  V1_1_1("1.0.0", LocalDate.of(1970, Month.JANUARY, 1), LocalDate.of(2070, Month.DECEMBER, 31));

  private final String version;
  private final LocalDate validFromDate;
  private final LocalDate validUntilDate;
  private final CustomProfiles customProfile = CustomProfiles.HL7;

  public static Hl7Version getDefaultVersion() {
    return ProfileVersion.getDefaultVersion(Hl7Version.class, CustomProfiles.HL7);
  }
}
