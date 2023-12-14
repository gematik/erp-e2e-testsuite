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

package de.gematik.test.erezept.fhir.builder;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.Hl7StructDef;
import de.gematik.test.erezept.fhir.valuesets.Country;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.StringType;

public class AddressBuilder {

  private AddressBuilder() {
    throw new AssertionError();
  }

  private static final Pattern STREET_PATTERN =
      Pattern.compile("^([\\D\\s]+)\\s*([\\d|\\w]+)?.*"); // NOSONAR won't be exposed to a user
  // directly

  public static Address address(
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street,
      @NonNull Address.AddressType addressType) {
    return address(Country.D, city, postal, street, addressType);
  }

  public static Address address(
      @NonNull Country country,
      @NonNull String city,
      @NonNull String postal,
      @NonNull String street,
      @NonNull Address.AddressType addressType) {
    val address = new Address();
    val type = new Enumeration<>(new Address.AddressTypeEnumFactory(), addressType);
    address.setTypeElement(type);
    address.setCountry(country.getCode());
    address.setPostalCode(postal).setCity(city);

    val streetMatcher = STREET_PATTERN.matcher(street);
    if (!streetMatcher.matches()) {
      throw new BuilderException(format("Given Street {0} is invalid", street));
    }

    val streetName = streetMatcher.group(1).trim();
    val houseNumber = streetMatcher.group(2).trim();

    val streetLine = address.addLineElement();
    streetLine.setValue(street);
    streetLine
        .addExtension()
        .setUrl(Hl7StructDef.HOUSE_NUMBER.getCanonicalUrl())
        .setValue(new StringType(houseNumber));
    streetLine
        .addExtension()
        .setUrl(Hl7StructDef.STREET_NAME.getCanonicalUrl())
        .setValue(new StringType(streetName));

    return address;
  }
}
