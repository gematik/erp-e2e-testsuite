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

package de.gematik.test.erezept.config.dto.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErpActorConfigurationTest {

  private static String alice =
      """
      {
      "name": "Alice",
      "device": "iPhone"
      }
      """;

  private static String bob =
      """
        {
        "name": "Bob",
        "device": "Android",
        "useVirtualEgk": "true"
        }
        """;

  private static String charlie =
      """
        {
        "name": "Charlie",
        "device": "iPhone",
        "egkIccsn": "123123123"
        }
        """;

  private static String david =
      """
        {
        "name": "David",
        "device": "iPhone",
        "egkIccsn": "",
        "useVirtualEgk": "true"
        }
        """;

  private static String eve =
      """
        {
        "name": "Eve",
        "device": "iPhone"
        }
        """;
  

  @ParameterizedTest
  @MethodSource
  void shouldCheckVirtualEgkConfiguration(
      String input,
      String name,
      String device,
      Consumer<String> iccsnAssertion,
      boolean hasVirtualEgk)
      throws JsonProcessingException {
    val mapper = new ObjectMapper();
    val conf = mapper.readValue(input, ErpActorConfiguration.class);
    assertEquals(name, conf.getName());
    assertEquals(device, conf.getDevice());
    iccsnAssertion.accept(conf.getEgkIccsn());
    assertEquals(hasVirtualEgk, conf.useVirtualEgk());
  }

  static Stream<Arguments> shouldCheckVirtualEgkConfiguration() {
    return Stream.of(
        arguments(alice, "Alice", "iPhone", (Consumer<String>) Assertions::assertNull, false),
        arguments(bob, "Bob", "Android", (Consumer<String>) Assertions::assertNull, false),
        arguments(
            charlie,
            "Charlie",
            "iPhone",
            (Consumer<String>) iccsn -> assertEquals("123123123", iccsn),
            false),
        arguments(david, "David", "iPhone", (Consumer<String>) Assertions::assertNull, false),
        arguments(eve, "Eve", "iPhone", (Consumer<String>) Assertions::assertNull, false));
  }
}
