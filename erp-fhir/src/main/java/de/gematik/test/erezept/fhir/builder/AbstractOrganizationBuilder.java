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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

public abstract class AbstractOrganizationBuilder<
        R extends Resource, B extends AbstractOrganizationBuilder<R, B>>
    extends ResourceBuilder<R, B> {

  protected final List<ContactPoint> contactPoints = new LinkedList<>();
  protected String name;
  protected Address address;

  public B name(String name) {
    this.name = name;
    return self();
  }

  public B address(Address address) {
    this.address = address;
    return self();
  }

  public B phone(String phoneNumber) {
    return this.contact(ContactPoint.ContactPointSystem.PHONE, phoneNumber);
  }

  public B email(String email) {
    return this.contact(ContactPoint.ContactPointSystem.EMAIL, email);
  }

  public B contact(ContactPoint.ContactPointSystem system, String value) {
    this.contactPoints.add(new ContactPoint().setSystem(system).setValue(value));
    return self();
  }

  protected final Organization buildOrganizationWith(
      Supplier<CanonicalType> profileSupplier, Identifier identifier) {
    return buildOrganizationWith(profileSupplier, List.of(identifier));
  }

  protected final Organization buildOrganizationWith(
      Supplier<CanonicalType> profileSupplier, List<Identifier> identifiers) {
    val organization = new Organization();

    val profile = profileSupplier.get();
    val meta = new Meta().setProfile(List.of(profile));
    organization.setId(this.getResourceId()).setMeta(meta);

    organization.setIdentifier(identifiers).setName(name).setTelecom(contactPoints);

    Optional.ofNullable(address).map(List::of).ifPresent(organization::setAddress);

    return organization;
  }
}
