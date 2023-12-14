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

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
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
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings({"java:S1192"}) // duplicating string literals here is perfectly fine!
public enum CustomProfiles {
  DE_BASIS_PROFIL_R4("de.basisprofil.r4", "http://fhir.de/", DeBasisVersion.class),
  KBV_BASIS("kbv.basis", "https://fhir.kbv.de/", KbvBasisVersion.class),
  KBV_ITA_FOR("kbv.ita.for", "https://fhir.kbv.de/", KbvItaForVersion.class),
  KBV_ITA_ERP("kbv.ita.erp", "https://fhir.kbv.de/", KbvItaErpVersion.class),
  GEM_ERP_WORKFLOW(
      "de.gematik.erezept-workflow",
      List.of(
          "http://gematik.de/fhir",
          "http://gematik.de/fhir/erx",
          "https://gematik.de/fhir",
          "https://gematik.de/fhir/erx",
          "https://gematik.de/fhir/erp"),
      ErpWorkflowVersion.class),
  GEM_PATIENTENRECHNUNG(
      "de.gematik.erezept-patientenrechnung",
      "https://gematik.de/fhir/erpchrg",
      PatientenrechnungVersion.class),
  ABDA_ERP_BASIS(
      "de.abda.erezeptabgabedatenbasis", "http://fhir.abda.de/", AbdaErpBasisVersion.class),
  ABDA_ERP_ABGABE_PKV(
      "de.abda.erezeptabgabedatenpkv", "http://fhir.abda.de/", AbdaErpPkvVersion.class),
  DAV_KBV_CS_VS("dav.kbv.sfhir.cs.vs", "https://fhir.kbv.de/", DavKbvCsVsVersion.class),
  HL7("org.hl7", "http://terminology.hl7.org/", Hl7Version.class);

  private final String name;
  private final List<String> canonicalClaims;
  private final Class<? extends ProfileVersion<?>> versionClass;

  CustomProfiles(
      String name, String canonicalClaim, Class<? extends ProfileVersion<?>> versionClass) {
    this(name, List.of(canonicalClaim), versionClass);
  }

  CustomProfiles(
      String name, List<String> canonicalClaim, Class<? extends ProfileVersion<?>> versionClass) {
    this.name = name;
    this.canonicalClaims = canonicalClaim;
    this.versionClass = versionClass;
  }

  public boolean matchesClaim(@NonNull String url) {
    return canonicalClaims.stream().anyMatch(url::contains);
  }

  public static CustomProfiles fromName(@NonNull String name) {
    return Arrays.stream(CustomProfiles.values())
        .filter(profile -> name.contains(profile.getName()))
        .findFirst()
        .orElseThrow(
            () -> new FhirValidatorException(format("Profile with name {0} is not known", name)));
  }

  @Override
  public String toString() {
    return this.getName();
  }
}
