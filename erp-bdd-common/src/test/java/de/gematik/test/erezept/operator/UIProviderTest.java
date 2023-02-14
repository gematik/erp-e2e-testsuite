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

package de.gematik.test.erezept.operator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.awt.GraphicsEnvironment;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class UIProviderTest {

  @Test
  void shouldThrowOnGetInstructionResult() {
    try (MockedStatic<GraphicsEnvironment> graphicsEnv = mockStatic(GraphicsEnvironment.class)) {
      graphicsEnv.when(GraphicsEnvironment::isHeadless).thenReturn(true);
      assertThrows(NotImplementedException.class, () -> UIProvider.getInstructionResult("test"));
    }
  }

  @Test
  void shouldThrowOnGetImageInstructionResult() {
    try (MockedStatic<GraphicsEnvironment> graphicsEnv = mockStatic(GraphicsEnvironment.class)) {
      graphicsEnv.when(GraphicsEnvironment::isHeadless).thenReturn(true);
      assertThrows(
          NotImplementedException.class, () -> UIProvider.getInstructionResult(null, "test"));
    }
  }

  @Test
  void shouldThrowOnGetQuestionResult() {
    try (MockedStatic<GraphicsEnvironment> graphicsEnv = mockStatic(GraphicsEnvironment.class)) {
      graphicsEnv.when(GraphicsEnvironment::isHeadless).thenReturn(true);
      assertThrows(NotImplementedException.class, () -> UIProvider.getQuestionResult("test?"));
    }
  }
}
