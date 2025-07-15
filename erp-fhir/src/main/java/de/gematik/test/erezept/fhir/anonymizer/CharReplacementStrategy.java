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

package de.gematik.test.erezept.fhir.anonymizer;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Collector;

public class CharReplacementStrategy implements MaskingStrategy {

  private final Random rnd = new SecureRandom();

  @Override
  public String maskString(String input) {
    return input
        .chars()
        .mapToObj(this::maskCharacter)
        .collect(
            Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
  }

  private char maskCharacter(int input) {
    var output = (char) input;
    if (Character.isLetter(output)) {
      if (Character.isLowerCase(output)) {
        output = (char) rnd.nextInt('a', 'z' + 1);
      } else {
        output = (char) rnd.nextInt('A', 'Z' + 1);
      }
    } else if (Character.isDigit(output)) {
      output = String.valueOf(rnd.nextInt(0, 10)).charAt(0);
    }

    return output;
  }
}
