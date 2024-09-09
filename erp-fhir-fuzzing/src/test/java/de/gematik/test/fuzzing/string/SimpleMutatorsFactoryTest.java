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

package de.gematik.test.fuzzing.string;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.fuzzing.core.ProbabilityDice;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.Test;

class SimpleMutatorsFactoryTest {

  private static final String BUNDLE_INPUT =
      "<Bundle xmlns=\"http://hl7.org/fhir\"><id"
          + " value=\"80f212e1-42dc-4691-a257-0ef2f01de7fd\"/><meta><lastUpdated"
          + " value=\"2021-11-12T11:08:17.2415449+01:00\"/><profile"
          + " value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1\"/></meta><identifier>";

  @Test
  void shouldUpperCaseAllBooleanValues() {
    val input = "<allowedBoolean value=\"true\"/><allowedBoolean value=\"false\"/>";
    val m = SimpleMutatorsFactory.booleanValuesToUpperCase();
    val output = m.apply(input);
    assertEquals("<allowedBoolean value=\"TRUE\"/><allowedBoolean value=\"FALSE\"/>", output);
  }

  @Test
  void shouldUpperCaseAllBooleanValues2() {
    val input = "<allowedBoolean value=\"true\"/><allowedBoolean value=\"false\"/>";
    val dice = new ProbabilityDice(new Random(1234L)); // init dice for deterministic tests
    val flipCount = 10;
    val probability = 0.5f;
    val m = SimpleMutatorsFactory.booleanValuesToUpperCase(probability);

    var flips = 0;
    for (var i = 0; i < flipCount; i++) {
      val output = m.apply(input);
      if (output.contains("TRUE")) flips++;
      if (output.contains("FALSE")) flips++;
    }

    // well, not very precise but should do the job!
    val expectedMin = (flipCount * 2) * probability;
    assertTrue(flips >= expectedMin, "Too few changes");

    // flipCount*2 would mean 100%
    val expectedMax = flipCount * 2;
    assertTrue(flips < expectedMax, "Too many changes");
  }

  @Test
  void shouldFlipAllBooleanValues() {
    val input = "<allowedBoolean value=\"true\"/><allowedBoolean value=\"false\"/>";
    val m = SimpleMutatorsFactory.flipBooleans();
    val output = m.apply(input);
    assertEquals("<allowedBoolean value=\"false\"/><allowedBoolean value=\"true\"/>", output);
  }

  @Test
  void shouldRandomlyManipulateRawString() {
    val m = SimpleMutatorsFactory.everything();
    val output = m.apply(BUNDLE_INPUT);
    assertNotEquals(BUNDLE_INPUT, output);
  }

  @Test
  void shouldRandomlyManipulateRawSubString() {
    val regex = XmlRegExpFactory.range("<meta>", "</meta>").exclusive();
    val m = SimpleMutatorsFactory.everything(regex, 120.0);
    val output = m.apply(BUNDLE_INPUT);
    assertNotEquals(BUNDLE_INPUT, output);
  }

  @Test
  void shouldRandomlyManipulateRawSubString2() {
    val regex = XmlRegExpFactory.betweenXmlTag("meta");
    val m = SimpleMutatorsFactory.everything(regex, -5.1);
    val output = m.apply(BUNDLE_INPUT);
    assertNotEquals(BUNDLE_INPUT, output);
  }

  @Test
  void shouldRandomlyManipulateByteArray() {
    val in = "Hello World";
    val ba = in.getBytes(StandardCharsets.UTF_8);
    SimpleMutatorsFactory.wholeByteArray(5.0).accept(ba);
    val outString = new String(ba);
    assertNotEquals(in, outString);
  }
}
