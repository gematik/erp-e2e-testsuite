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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerEMail;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPhone;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.KSN;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class MedicalOrganizationFaker {

  private final Map<String, Consumer<MedicalOrganizationBuilder>> builderConsumers =
      new HashMap<>();

  public enum OrganizationFakerType {
    GENERAL,
    DENTAL,
    HOSPITAL,
    HOSPITAL_KSN;
  }

  private MedicalOrganizationFaker(OrganizationFakerType type) {
    builderConsumers.put("phone", b -> b.phone(fakerPhone()));
    builderConsumers.put("address", b -> b.address(fakerCity(), fakerZipCode(), fakerStreetName()));
    builderConsumers.put("name", b -> b.name(fakerName()));
    builderConsumers.put("email", b -> b.email(fakerEMail()));

    switch (type) {
      case DENTAL:
        this.withKzva(KZVA.random());
        break;
      case HOSPITAL:
        this.withIknr(IKNR.random());
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

  public static MedicalOrganizationFaker forPractitioner(KbvPractitioner practitioner) {
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

  public static MedicalOrganizationFaker builder() {
    return builder(fakerValueSet(OrganizationFakerType.class));
  }

  public static MedicalOrganizationFaker builder(OrganizationFakerType type) {
    return new MedicalOrganizationFaker(type);
  }

  public static MedicalOrganizationFaker dentalPractice() {
    return builder(OrganizationFakerType.DENTAL);
  }

  public static MedicalOrganizationFaker medicalPractice() {
    return builder(OrganizationFakerType.GENERAL);
  }

  public static MedicalOrganizationFaker hospital() {
    return builder(OrganizationFakerType.HOSPITAL);
  }

  public MedicalOrganizationFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public MedicalOrganizationFaker withName(String name) {
    builderConsumers.computeIfPresent("name", (key, defaultValue) -> b -> b.name(name));
    return this;
  }

  public MedicalOrganizationFaker withPhoneNumber(String number) {
    builderConsumers.computeIfPresent("phone", (key, defaultValue) -> b -> b.phone(number));
    return this;
  }

  public MedicalOrganizationFaker withEmail(String email) {
    builderConsumers.computeIfPresent("email", (key, defaultValue) -> b -> b.email(email));
    return this;
  }

  public MedicalOrganizationFaker withBsnr(BSNR bsnr) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.GENERAL.name(), b -> b.bsnr(bsnr));
    return this;
  }

  public MedicalOrganizationFaker withBsnr(String bsnr) {
    return withBsnr(BSNR.from(bsnr));
  }

  public MedicalOrganizationFaker withIknr(IKNR iknr) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.HOSPITAL.name(), b -> b.iknr(iknr));
    return this;
  }

  public MedicalOrganizationFaker withKsn(KSN ksn) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.HOSPITAL_KSN.name(), b -> b.ksn(ksn));
    return this;
  }

  public MedicalOrganizationFaker withKzva(KZVA kzva) {
    cleanupIdentifiers();
    builderConsumers.put(OrganizationFakerType.DENTAL.name(), b -> b.kzva(kzva));
    return this;
  }

  public MedicalOrganizationFaker withAddress(String city, String postal, String street) {
    builderConsumers.computeIfPresent(
        "address", (key, defaultValue) -> b -> b.address(city, postal, street));
    return this;
  }

  public MedicalOrganization fake() {
    return this.toBuilder().build();
  }

  public MedicalOrganizationBuilder toBuilder() {
    val builder = MedicalOrganizationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }

  private void cleanupIdentifiers() {
    Arrays.stream(OrganizationFakerType.values()).forEach(ot -> builderConsumers.remove(ot.name()));
  }
}
