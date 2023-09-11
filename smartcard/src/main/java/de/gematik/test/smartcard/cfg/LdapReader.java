/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.smartcard.cfg;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.SmartcardOwnerData;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public final class LdapReader {

  private LdapReader() {
    throw new UnsupportedOperationException();
  }

  public static SmartcardOwnerData getOwnerData(Principal subject) {
    return getOwnerData(subject.getName());
  }

  public static SmartcardOwnerData getOwnerData(String subject) {
    val builder = SmartcardOwnerData.builder();
    val fixedName = subject.replace("+", ",");
    val elements = fixedName.split(",");

    for (val rdn : elements) {
      val token = rdn.split("=");
      val key = token[0].trim();
      val value = token[1].trim();

      switch (key.toUpperCase()) {
        case "CN" -> builder.commonName(value);
        case "T" -> builder.title(value);
        case "GIVENNAME", "GN" -> builder.givenName(value);
        case "SURNAME" -> builder.surname(value);
        case "STREET" -> builder.street(value);
        case "postalCode" -> builder.postalCode(value);
        case "O" -> builder.organization(value);
        case "OU" -> builder.organizationUnit(value);
        case "L" -> builder.locality(value);
        case "C" -> builder.country(value);
        default -> log.trace(
            format("ignore key {0} with value {1} in subject {2}", key, value, subject));
      }
    }

    return builder.build();
  }
}
