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
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPhone;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.r4.kbv.AssignerOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

public class KbvAssignerOrganizationFaker {
  private final Map<String, Consumer<KbvAssignerOrganizationBuilder>> builderConsumers =
      new HashMap<>();
  private Reference assignerReference;

  private KbvAssignerOrganizationFaker() {
    this.withVersion(KbvItaForVersion.getDefaultVersion())
        .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
        .withName(fakerName())
        .withPhone(fakerPhone())
        .withIknr(IKNR.random())
        .withAssigner(
            ErpFhirResource.createReference(
                ResourceType.Organization, UUID.randomUUID().toString()));
  }

  public static KbvAssignerOrganizationFaker builder() {
    return new KbvAssignerOrganizationFaker();
  }

  public KbvAssignerOrganizationFaker withVersion(KbvItaForVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvAssignerOrganizationFaker withAddress(String city, String postal, String street) {
    return this.withAddress(Country.D, city, postal, street);
  }

  public KbvAssignerOrganizationFaker withAddress(
      Country country, String city, String postal, String street) {
    builderConsumers.put("address", b -> b.address(country, city, postal, street));
    return this;
  }

  public KbvAssignerOrganizationFaker withIknr(IKNR value) {
    builderConsumers.put("iknr", b -> b.iknr(value));
    return this;
  }

  public KbvAssignerOrganizationFaker withIknr(String value) {
    this.withIknr(IKNR.asArgeIknr(value));
    return this;
  }

  public KbvAssignerOrganizationFaker withPhone(String phone) {
    builderConsumers.put("phone", b -> b.phone(phone));
    return this;
  }

  public KbvAssignerOrganizationFaker forPatient(KbvPatient patient) {
    if (!patient.hasPkvKvnr()) {
      throw new BuilderException(
          format(
              "Cannot build AssignerOrganization from Patient with {0} Insurance",
              patient.getInsuranceKind()));
    }
    val assignerRef =
        patient
            .getPkvAssigner()
            .orElseThrow(
                () ->
                    new BuilderException(
                        format(
                            "{0} Patient does not have an Assigner", patient.getInsuranceKind())));

    val name =
        patient
            .getPkvAssignerName()
            .orElseThrow(
                () ->
                    new BuilderException(
                        format(
                            "{0} Patient does not have an Assigner Name",
                            patient.getInsuranceKind())));
    return this.withAssigner(assignerRef).withName(name);
  }

  public KbvAssignerOrganizationFaker withName(String name) {
    builderConsumers.put("name", b -> b.name(name));
    return this;
  }

  private KbvAssignerOrganizationFaker withAssigner(Reference reference) {
    this.assignerReference = reference;
    return this;
  }

  public AssignerOrganization fake() {
    return this.toBuilder().build();
  }

  public KbvAssignerOrganizationBuilder toBuilder() {
    val builder = KbvAssignerOrganizationBuilder.builder();

    val refTokens = assignerReference.getReference().split("/");
    // get the second token if available, otherwise the first one if reference was only <UUID>
    val resourceId = refTokens.length > 1 ? refTokens[1] : refTokens[0];
    builder.setId(resourceId);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
