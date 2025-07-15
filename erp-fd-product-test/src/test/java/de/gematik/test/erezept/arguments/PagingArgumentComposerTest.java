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

package de.gematik.test.erezept.arguments;

import static de.gematik.test.erezept.arguments.PagingArgumentComposer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class PagingArgumentComposerTest {

  @Test
  void shouldBuildCorrectQueryComposerBigValuesCheckFirstArgument() {
    val qCBV = queryComposerBigValues().create().toList();
    assertFalse(qCBV.isEmpty());
    assertEquals(9, qCBV.size());
    assertEquals(5, Arrays.stream(qCBV.get(1).get()).toList().size());
  }

  @Test
  void shouldBuildCorrectQueryComposerSmallValuesCheckFirstArgument() {
    val qCBV = queryComposerSmallValues().create().toList();
    assertFalse(Arrays.stream(qCBV.get(0).get()).toList().isEmpty());
    assertEquals(7, qCBV.size());
    assertEquals(5, Arrays.stream(qCBV.get(1).get()).toList().size());
  }

  @Test
  void shouldBuildCorrectQueryComposerSmallValuesForCommunicationCheckFirstArgument() {
    val qCBV = queryComposerSmallValuesForCommunication().create().toList();
    assertFalse(Arrays.stream(qCBV.get(0).get()).toList().isEmpty());
    assertEquals(7, qCBV.size());
    assertEquals(5, Arrays.stream(qCBV.get(1).get()).toList().size());
  }
}
