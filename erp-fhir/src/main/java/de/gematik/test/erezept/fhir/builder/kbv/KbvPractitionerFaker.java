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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvPractitionerFaker {
  private final Map<String, Consumer<KbvPractitionerBuilder>> builderConsumers = new HashMap<>();
  private static final String KEY_QUALIFICATION_TYPE =
      "qualificationType"; // key used for builderConsumers map
  private static final String KEY_BASE_ANR = "baseAnr"; // key used for builderConsumers map

  private KbvPractitionerFaker() {
    val qualificationType = randomElement(QualificationType.DOCTOR, QualificationType.DENTIST);
    this.withName(fakerFirstName(), fakerLastName())
        .withQualificationType(fakerProfession())
        .withAnr(BaseANR.randomFromQualification(qualificationType));
  }

  public static KbvPractitionerFaker builder() {
    return new KbvPractitionerFaker();
  }

  public KbvPractitionerFaker withName(String firstName, String lastName) {
    builderConsumers.put("name", b -> b.name(firstName, lastName));
    return this;
  }

  public KbvPractitionerFaker withName(String fullName) {
    val tokens = fullName.split(" ");
    val firstName = tokens[0] != null ? tokens[0] : fakerFirstName();
    val lastName = tokens[1] != null ? tokens[1] : fakerLastName();
    return this.withName(firstName, lastName);
  }

  public KbvPractitionerFaker withQualificationType(QualificationType qualificationType) {
    builderConsumers.put(KEY_QUALIFICATION_TYPE, b -> b.addQualification(qualificationType));
    builderConsumers.put(
        KEY_BASE_ANR, b -> b.anr(BaseANR.randomFromQualification(qualificationType)));
    return this;
  }

  public KbvPractitionerFaker withQualificationType(String jobTitle) {
    builderConsumers.put("jobTitle", b -> b.addQualification(jobTitle));
    return this;
  }

  public KbvPractitionerFaker withQualificationType(AsvFachgruppennummer asvFachgruppennummer) {
    builderConsumers.put("asvFachgruppennummer", b -> b.addQualification(asvFachgruppennummer));
    return this;
  }

  public KbvPractitionerFaker withLanr(String lanr) {
    this.withAnr(new LANR(lanr));
    return this;
  }

  public KbvPractitionerFaker withZanr(String zanr) {
    this.withAnr(new ZANR(zanr));
    return this;
  }

  public KbvPractitionerFaker withAnr(BaseANR anr) {
    val qualificationType =
        (anr.getType() == BaseANR.ANRType.LANR)
            ? QualificationType.DOCTOR
            : QualificationType.DENTIST;
    builderConsumers.put(KEY_BASE_ANR, b -> b.anr(anr));
    builderConsumers.put(KEY_QUALIFICATION_TYPE, b -> b.addQualification(qualificationType));
    return this;
  }

  public KbvPractitionerFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvPractitioner fake() {
    return this.toBuilder().build();
  }

  public KbvPractitionerBuilder toBuilder() {
    val builder = KbvPractitionerBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
