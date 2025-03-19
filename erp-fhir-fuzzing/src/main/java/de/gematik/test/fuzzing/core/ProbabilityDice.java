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

package de.gematik.test.fuzzing.core;

import static java.text.MessageFormat.format;

import java.security.SecureRandom;
import java.util.Random;

@SuppressWarnings({"java:S3010"})
public class ProbabilityDice {

  private static ProbabilityDice instance;

  private final Random rnd;

  public ProbabilityDice() {
    this(new SecureRandom());
  }

  public ProbabilityDice(Random rnd) {
    this.rnd = rnd;
    instance = this;
  }

  public boolean toss(float probability) {
    if (probability < 0.0 || probability > 1.0) {
      throw new IllegalArgumentException(
          format("Probability must be in range of 0.0 .. 1.0 but was given {0}", probability));
    }
    return rnd.nextFloat() <= probability;
  }

  public static ProbabilityDice get() {
    if (instance == null) {
      return new ProbabilityDice();
    } else {
      return instance;
    }
  }
}
