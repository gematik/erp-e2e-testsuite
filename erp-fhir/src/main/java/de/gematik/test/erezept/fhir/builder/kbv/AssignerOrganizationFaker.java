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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.references.kbv.OrganizationReference;
import de.gematik.test.erezept.fhir.resources.kbv.AssignerOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

public class AssignerOrganizationFaker {
  private final AssignerOrganizationBuilder builder;
  private final Map<String, Consumer<AssignerOrganizationBuilder>> builderConsumers =
      new HashMap<>();
  private Reference assignerReference;

  private AssignerOrganizationFaker(AssignerOrganizationBuilder builder) {
    this.builder = builder;
    builderConsumers.put("address", b -> b.address(fakerCity(), fakerZipCode(), fakerStreetName()));
    builderConsumers.put("name", b -> b.name(fakerName()));
    builderConsumers.put("phone", b -> b.phone(fakerPhone()));
    builderConsumers.put("iknr", b -> b.iknr(IKNR.random()));
    builderConsumers.put("version", b -> b.version(KbvItaForVersion.getDefaultVersion()));
  }

  public static AssignerOrganizationFaker builder() {
    return new AssignerOrganizationFaker(AssignerOrganizationBuilder.builder());
  }

  public AssignerOrganizationFaker withVersion(KbvItaForVersion version) {
    builderConsumers.computeIfPresent("version", (key, defaultValue) -> b -> b.version(version));
    return this;
  }

  public AssignerOrganizationFaker withAddress(String city, String postal, String street) {
    return this.withAddress(Country.D, city, postal, street);
  }

  public AssignerOrganizationFaker withAddress(
      Country country, String city, String postal, String street) {
    builderConsumers.computeIfPresent(
        "address", (key, defaultValue) -> b -> b.address(country, city, postal, street));
    return this;
  }

  public AssignerOrganizationFaker withIknr(IKNR value) {
    builderConsumers.computeIfPresent("iknr", (key, defaultValue) -> b -> b.iknr(value));
    return this;
  }

  public AssignerOrganizationFaker withIknr(String value) {
    this.withIknr(IKNR.from(value));
    return this;
  }

  public AssignerOrganizationFaker withPhone(String phone) {
    builderConsumers.computeIfPresent("phone", (key, defaultValue) -> b -> b.phone(phone));
    return this;
  }

  public AssignerOrganizationFaker forPatient(KbvPatient patient) {
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
    this.withReference(assignerRef);
    this.withName(name);
    return this;
  }

  public AssignerOrganizationFaker withName(String name) {
    builderConsumers.computeIfPresent("name", (key, defaultValue) -> b -> b.name(name));
    return this;
  }

  public AssignerOrganizationFaker withReference(Reference reference) {
    this.assignerReference = reference;
    return this;
  }

  public AssignerOrganizationFaker withOrganizationReference(OrganizationReference reference) {
    this.withReference(reference.asReference());
    return this;
  }

  public AssignerOrganization fake() {
    val ref =
        Objects.requireNonNullElseGet(
            assignerReference, () -> new Reference(UUID.randomUUID().toString()));

    val refTokens = ref.getReference().split("/");
    // get the second token if available, otherwise the first one if reference was only <UUID>
    val resourceId = refTokens.length > 1 ? refTokens[1] : refTokens[0];
    builder.setResourceId(resourceId);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder.build();
  }
}
