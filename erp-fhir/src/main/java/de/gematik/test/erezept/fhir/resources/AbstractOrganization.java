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

package de.gematik.test.erezept.fhir.resources;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.valuesets.Country;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PrimitiveType;

@Slf4j
@ResourceDef(name = "Organization")
@SuppressWarnings({"java:S110"})
public abstract class AbstractOrganization extends Organization {

  public String getPhone() {
    return this.getTelecom().stream()
        .filter(telecom -> telecom.getSystem().equals(ContactPoint.ContactPointSystem.PHONE))
        .map(ContactPoint::getValue)
        .findFirst()
        .orElse("n/a");
  }

  public String getMail() {
    return this.getTelecom().stream()
        .filter(telecom -> telecom.getSystem().equals(ContactPoint.ContactPointSystem.EMAIL))
        .map(ContactPoint::getValue)
        .findFirst()
        .orElse("n/a");
  }

  public String getCity() {
    return this.getAddressFirstRep().getCity();
  }

  public String getPostalCode() {
    return this.getAddressFirstRep().getPostalCode();
  }

  public String getStreet() {
    return this.getAddressFirstRep().getLine().stream()
        .map(PrimitiveType::getValue)
        .collect(Collectors.joining(" "));
  }

  public Country getCountry() {
    return Country.fromCode(this.getAddressFirstRep().getCountry());
  }
}
