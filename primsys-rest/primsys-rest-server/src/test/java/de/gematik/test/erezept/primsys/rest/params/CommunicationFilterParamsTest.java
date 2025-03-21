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

package de.gematik.test.erezept.primsys.rest.params;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.test.erezept.client.rest.param.SortOrder;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommunicationFilterParamsTest {

  @Test
  void shouldHaveEmptyConstructor() {
    val beanParam = new CommunicationFilterParams();
    assertTrue(beanParam.getSenderId().isEmpty());
    assertTrue(beanParam.getReceiverId().isEmpty());
    assertEquals(SortOrder.DESCENDING, beanParam.getSortOrder());
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapSortOrder(String input, SortOrder expectation)
      throws NoSuchFieldException, IllegalAccessException {
    val beanParam = new CommunicationFilterParams();

    // Note: CommunicationFilterParams will be generated by jakarta
    // opening up this class via @Data or setters only for testing purposes would be a bad practice
    val sortField = CommunicationFilterParams.class.getDeclaredField("sort");
    sortField.setAccessible(true);
    sortField.set(beanParam, input);

    assertEquals(expectation, beanParam.getSortOrder());
  }

  static Stream<Arguments> shouldMapSortOrder() {
    return Stream.of(
        arguments("ascending", SortOrder.ASCENDING),
        arguments("lifo", SortOrder.ASCENDING),
        arguments("LIFO", SortOrder.ASCENDING),
        arguments("Oldest", SortOrder.ASCENDING),
        arguments("", SortOrder.DESCENDING),
        arguments("FIFO", SortOrder.DESCENDING),
        arguments(null, SortOrder.DESCENDING));
  }
}
