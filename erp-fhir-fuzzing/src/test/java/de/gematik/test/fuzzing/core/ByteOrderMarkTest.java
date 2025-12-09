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

package de.gematik.test.fuzzing.core;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ByteOrderMarkTest {

  @ParameterizedTest
  @EnumSource(ByteOrderMark.class)
  void shouldAppendBOM(ByteOrderMark bom) {
    val mutator = bom.asMutator();
    val original = "<xml>Some XML</xml>";
    val mutated = mutator.apply(original);

    // Note: just for debugging purposes
    //    val out = Path.of("target/bom-" + bom.name() + ".xml"); // NOSONAR
    //    Files.writeString(out, mutated, bom.getCharset()); // NOSONAR

    assertTrue(hasBomBytes(mutated, bom));
  }

  private static boolean hasBomBytes(String input, ByteOrderMark bom) {
    val array = input.getBytes(bom.getCharset());
    val prefix = bom.getBytes();
    for (int i = 0; i < prefix.length; i++) {
      if (array[i] != prefix[i]) {
        return false;
      }
    }
    return true;
  }
}
