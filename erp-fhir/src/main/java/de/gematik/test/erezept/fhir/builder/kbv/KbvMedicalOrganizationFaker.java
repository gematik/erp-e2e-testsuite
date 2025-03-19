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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerEMail;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPhone;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvMedicalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KSN;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvMedicalOrganizationFaker {
  private final Map<String, Consumer<KbvMedicalOrganizationBuilder>> builderConsumers =
      new HashMap<>();

  public enum OrganizationFakerType {
    GENERAL,
    DENTAL,
    HOSPITAL,
    HOSPITAL_KSN
  }

  private KbvMedicalOrganizationFaker(OrganizationFakerType type) {
    this.withPhoneNumber(fakerPhone())
        .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
        .withName(fakerName())
        .withEmail(fakerEMail());

    switch (type) {
      case DENTAL:
        this.withKzva(KZVA.random());
        break;
      case HOSPITAL:
        this.withIknr(IKNR.randomSidIknr());
        break;
      case HOSPITAL_KSN:
        this.withKsn(KSN.random());
        break;
      case GENERAL:
      default:
        this.withBsnr(BSNR.random());
        break;
    }
  }

  public static KbvMedicalOrganizationFaker forPractitioner(KbvPractitioner practitioner) {
    if (practitioner.getQualificationType() == QualificationType.DENTIST) {
      return dentalPractice();
    } else {
      // Note: older KbvItaForVersion does not accept KSN. HOSPITAL_KSN can be removed from
      // exclusion once KbvItaForVersion.V1_0_3 is removed
      return builder(
          fakerValueSet(
              OrganizationFakerType.class,
              List.of(OrganizationFakerType.DENTAL, OrganizationFakerType.HOSPITAL_KSN)));
    }
  }

  public static KbvMedicalOrganizationFaker builder() {
    return builder(fakerValueSet(OrganizationFakerType.class));
  }

  public static KbvMedicalOrganizationFaker builder(OrganizationFakerType type) {
    return new KbvMedicalOrganizationFaker(type);
  }

  public static KbvMedicalOrganizationFaker dentalPractice() {
    return builder(OrganizationFakerType.DENTAL);
  }

  public static KbvMedicalOrganizationFaker medicalPractice() {
    return builder(OrganizationFakerType.GENERAL);
  }

  public static KbvMedicalOrganizationFaker hospital() {
    return builder(OrganizationFakerType.HOSPITAL);
  }

  public KbvMedicalOrganizationFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvMedicalOrganizationFaker withName(String name) {
    builderConsumers.put("name", b -> b.name(name));
    return this;
  }

  public KbvMedicalOrganizationFaker withPhoneNumber(String number) {
    builderConsumers.put("phone", b -> b.phone(number));
    return this;
  }

  public KbvMedicalOrganizationFaker withEmail(String email) {
    builderConsumers.put("email", b -> b.email(email));
    return this;
  }

  public KbvMedicalOrganizationFaker withBsnr(BSNR bsnr) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.GENERAL.name(), b -> b.bsnr(bsnr));
    return this;
  }

  public KbvMedicalOrganizationFaker withBsnr(String bsnr) {
    return withBsnr(BSNR.from(bsnr));
  }

  public KbvMedicalOrganizationFaker withIknr(IKNR iknr) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.HOSPITAL.name(), b -> b.iknr(iknr));
    return this;
  }

  public KbvMedicalOrganizationFaker withKsn(KSN ksn) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.HOSPITAL_KSN.name(), b -> b.ksn(ksn));
    return this;
  }

  public KbvMedicalOrganizationFaker withKzva(KZVA kzva) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.DENTAL.name(), b -> b.kzva(kzva));
    return this;
  }

  public KbvMedicalOrganizationFaker withAddress(String city, String postal, String street) {
    builderConsumers.put("address", b -> b.address(city, postal, street));
    return this;
  }

  public KbvMedicalOrganization fake() {
    return this.toBuilder().build();
  }

  public KbvMedicalOrganizationBuilder toBuilder() {
    val builder = KbvMedicalOrganizationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }

  private void cleanupIdentifiers() {
    Arrays.stream(OrganizationFakerType.values()).forEach(ot -> builderConsumers.remove(ot.name()));
  }
}
