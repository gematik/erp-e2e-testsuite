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

package de.gematik.test.erezept.cli.util;

import de.gematik.test.erezept.fhir.builder.*;
import lombok.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NameWrapper {

  private final String firstName;
  private final String lastName;

  public static NameWrapper randomName() {
    return fromFullName(GemFaker.fakerName());
  }

  public static NameWrapper fromFullName(String fullName) {
    if (fullName == null) {
      return randomName();
    } else {
      val tokens = splitName(fullName);
      return new NameWrapper(tokens[0], tokens[1]);
    }
  }

  private static String[] splitName(String fullName) {
    val ret = new String[2];
    val tokens = fullName.split(" ");
    if (tokens[0].isEmpty()) {
      ret[0] = GemFaker.fakerFirstName();
    } else {
      ret[0] = tokens[0];
    }

    if (tokens.length < 2) {
      ret[1] = GemFaker.fakerLastName();
    } else {
      ret[1] = tokens[1];
    }

    return ret;
  }
}
