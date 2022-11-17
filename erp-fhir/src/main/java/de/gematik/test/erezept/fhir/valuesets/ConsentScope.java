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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.Hl7CodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/** https://terminology.hl7.org/2.1.0/CodeSystem-consentscope.html */
@Getter
public enum ConsentScope implements IValueSet {
  RESEARCH(
      "research",
      "Research",
      "Consent to participate in research protocol and information sharing required"),
  PATIENT_PRIVACY(
      "patient-privacy",
      "Privacy Consent",
      "Agreement to collect, access, use or disclose (share) information"),
  TREATMENT("treatment", "Treatment", "Consent to undergo a specific treatment");

  public static final Hl7CodeSystem CODE_SYSTEM = Hl7CodeSystem.CONSENT_SCOPE;
  public static final String VERSION = "0.1.0";
  public static final String DESCRIPTION = "The Scope of a Consent";
  public static final String PUBLISHER = "CBCC";

  private final String code;
  private final String display;
  private final String definition;

  ConsentScope(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public Hl7CodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ConsentScope fromCode(@NonNull String code) {
    return Arrays.stream(ConsentScope.values())
        .filter(pt -> pt.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(ConsentScope.class, code));
  }
}
