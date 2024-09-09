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

package de.gematik.test.erezept.fhir.builder;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public abstract class AbstractOrganizationBuilder<T extends AbstractOrganizationBuilder<T>>
    extends AbstractResourceBuilder<T> {

  protected final List<ContactPoint> contactPoints = new LinkedList<>();
  protected String name;
  protected Address address;

  public T name(@NonNull String name) {
    this.name = name;
    return self();
  }

  public T address(@NonNull Address address) {
    this.address = address;
    return self();
  }

  public T phone(@NonNull String phoneNumber) {
    return this.contact(ContactPoint.ContactPointSystem.PHONE, phoneNumber);
  }

  public T email(@NonNull String email) {
    return this.contact(ContactPoint.ContactPointSystem.EMAIL, email);
  }

  public T contact(ContactPoint.ContactPointSystem system, @NonNull String value) {
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

    // set FHIR-specific values provided by HAPI
    organization.setId(this.getResourceId()).setMeta(meta);

    organization.setIdentifier(identifiers).setName(name).setTelecom(contactPoints);

    if (address != null) {
      organization.setAddress(List.of(address));
    }

    return organization;
  }
}
