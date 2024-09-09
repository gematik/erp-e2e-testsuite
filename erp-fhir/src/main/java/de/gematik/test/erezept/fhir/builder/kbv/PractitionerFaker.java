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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class PractitionerFaker {
  private final Map<String, Consumer<PractitionerBuilder>> builderConsumers = new HashMap<>();
  private static final String KEY_QUALIFICATION_TYPE =
      "qualificationType"; // key used for builderConsumers map
  private static final String KEY_BASE_ANR = "baseAnr"; // key used for builderConsumers map

  private PractitionerFaker() {
    val qualificationType = randomElement(QualificationType.DOCTOR, QualificationType.DENTIST);
    builderConsumers.put("name", b -> b.name(fakerFirstName(), fakerLastName()));
    builderConsumers.put("jobTitle", b -> b.addQualification(fakerProfession()));
    builderConsumers.put(KEY_QUALIFICATION_TYPE, b -> b.addQualification(qualificationType));
    builderConsumers.put("version", b -> b.version(KbvItaForVersion.getDefaultVersion()));
    builderConsumers.put(
        KEY_BASE_ANR, b -> b.anr(BaseANR.randomFromQualification(qualificationType)));
  }

  public static PractitionerFaker builder() {
    return new PractitionerFaker();
  }

  public PractitionerFaker withName(String firstName, String lastName) {
    builderConsumers.computeIfPresent(
        "name", (key, defaultValue) -> b -> b.name(firstName, lastName));
    return this;
  }

  public PractitionerFaker withName(String fullName) {
    val tokens = fullName.split(" ");
    val firstName = tokens[0] != null ? tokens[0] : fakerFirstName();
    val lastName = tokens[1] != null ? tokens[1] : fakerLastName();
    return this.withName(firstName, lastName);
  }

  public PractitionerFaker withQualificationType(QualificationType qualificationType) {
    builderConsumers.computeIfPresent(
        KEY_QUALIFICATION_TYPE, (key, defaultValue) -> b -> b.addQualification(qualificationType));
    builderConsumers.computeIfPresent(
        KEY_BASE_ANR,
        (key, defaultValue) -> b -> b.anr(BaseANR.randomFromQualification(qualificationType)));
    return this;
  }

  public PractitionerFaker withQualificationType(String jobTitle) {
    builderConsumers.computeIfPresent(
        "jobTitle", (key, defaultValue) -> b -> b.addQualification(jobTitle));
    return this;
  }

  public PractitionerFaker withQualificationType(AsvFachgruppennummer asvFachgruppennummer) {
    builderConsumers.put("asvFachgruppennummer", b -> b.addQualification(asvFachgruppennummer));
    return this;
  }

  public PractitionerFaker withLanr(String lanr) {
    this.withAnr(new LANR(lanr));
    return this;
  }

  public PractitionerFaker withZanr(String zanr) {
    this.withAnr(new ZANR(zanr));
    return this;
  }

  public PractitionerFaker withAnr(BaseANR anr) {
    val qualificationType =
        (anr.getType() == BaseANR.ANRType.LANR)
            ? QualificationType.DOCTOR
            : QualificationType.DENTIST;
    builderConsumers.computeIfPresent(KEY_BASE_ANR, (key, defaultValue) -> b -> b.anr(anr));
    builderConsumers.computeIfPresent(
        KEY_QUALIFICATION_TYPE, (key, defaultValue) -> b -> b.addQualification(qualificationType));
    return this;
  }

  public PractitionerFaker withVersion(KbvItaForVersion version) {
    builderConsumers.computeIfPresent("version", (key, defaultValue) -> b -> b.version(version));
    return this;
  }

  public KbvPractitioner fake() {
    return this.toBuilder().build();
  }

  public PractitionerBuilder toBuilder() {
    val builder = PractitionerBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
