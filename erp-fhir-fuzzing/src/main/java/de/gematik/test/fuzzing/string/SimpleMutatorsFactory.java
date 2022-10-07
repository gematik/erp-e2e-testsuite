/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.fuzzing.string;

import static java.text.MessageFormat.format;

import de.gematik.test.fuzzing.core.ByteArrayMutator;
import de.gematik.test.fuzzing.core.ProbabilityDice;
import de.gematik.test.fuzzing.core.StringMutator;
import java.security.SecureRandom;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SimpleMutatorsFactory {

  private SimpleMutatorsFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static StringMutator booleanValuesToUpperCase() {
    return booleanValuesToUpperCase(1.0f);
  }

  public static StringMutator booleanValuesToUpperCase(float probability) {
    return input -> {
      val pattern = Pattern.compile("(true|false)");
      val matcher = pattern.matcher(input);
      return matcher.replaceAll(
          replacer -> {
            if (ProbabilityDice.get().toss(probability)) {
              return replacer.group().toUpperCase(); // flip
            } else {
              return replacer.group(); // don't flip
            }
          });
    };
  }

  public static StringMutator flipBooleans() {
    return flipBooleans(1.0f);
  }

  public static StringMutator flipBooleans(float probability) {
    return input -> {
      val pattern = Pattern.compile("(true|false)");
      val matcher = pattern.matcher(input);
      return matcher.replaceAll(
          replacer -> {
            if (ProbabilityDice.get().toss(probability)) {
              // flip
              if (replacer.group().equals("true")) return "false";
              else if (replacer.group().equals("false")) return "true";
              else {
                log.warn(format("Unexpected RegEx Match: {0}", replacer.group()));
                return replacer.group();
              }
            } else {
              return replacer.group(); // don't flip
            }
          });
    };
  }

  public static StringMutator everything() {
    return everything(0.05);
  }

  public static StringMutator everything(double percentage) {
    return everything(RegExp.everything(), percentage);
  }

  public static StringMutator everything(RegExp regex, double percentage) {
    return input -> {
      val matcher = regex.matcher(input);
      val rnd = new SecureRandom();
      val sb = new StringBuilder(input);

      while (matcher.find()) {
        val targetStart = matcher.start("target");
        val targetEnd = matcher.end("target");
        val len = targetEnd - targetStart;

        // len == 0 means an empty group was found which cannot be fuzzed - as it is empty!
        // mostly can happen if the target contains .*
        if (len > 0) {
          val amount = estimateAmount(len, percentage);

          for (var i = 0; i < amount; i++) {
            val idx = targetStart + rnd.nextInt(len);
            val c = (char) (0xFF & rnd.nextInt(255));
            sb.setCharAt(idx, c);
          }
        }
      }

      return sb.toString();
    };
  }

  public static ByteArrayMutator wholeByteArray(double percentage) {
    return input -> {
      val rnd = new SecureRandom();
      val amount = estimateAmount(input.length, percentage);

      for (var i = 0; i < amount; i++) {
        val idx = rnd.nextInt(input.length);
        input[idx] = (byte) (0xFF & rnd.nextInt(255));
      }
    };
  }

  private static int estimateAmount(int inputLength, double percentage) {
    var amount = (int) Math.floor(inputLength * cleanedPercentage(percentage));
    return (amount <= 0) ? 1 : amount; // ensure at leas a single char is manipulated!
  }

  /**
   * The percentage is not always a fixed value but rather can be dynamically calculated. To prevent
   * IndexOutOfBoundExceptions percentage must be checked and adjusted accordingly
   *
   * @param percentage the calculated percentage value
   * @return checked and adjusted percentage in a range between 0 and 100% only
   */
  private static double cleanedPercentage(double percentage) {
    if (percentage < 0.0) {
      percentage = Math.abs(percentage);
    }

    if (percentage > 100.0) {
      percentage = 100.0;
    }

    return percentage;
  }
}
