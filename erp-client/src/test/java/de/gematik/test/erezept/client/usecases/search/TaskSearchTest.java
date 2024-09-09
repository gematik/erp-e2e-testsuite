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

package de.gematik.test.erezept.client.usecases.search;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class TaskSearchTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(TaskSearch.class));
  }

  @Test
  void shouldBuildSimpleSortByAuthoredOn() {
    val cmd = TaskSearch.getSortedByAuthoredOn(SortOrder.ASCENDING);
    assertTrue(cmd.getRequestLocator().contains("_sort=authored-on"));
  }

  @Test
  void shouldBuildSimpleSortByModified() {
    val cmd = TaskSearch.builder().sortedByModified(SortOrder.DESCENDING).createCommand();
    assertTrue(cmd.getRequestLocator().contains("_sort=-modified"));
  }

  @Test
  void shouldBuildComplexSearchQuery() {
    val cmd =
        TaskSearch.builder()
            .sortedByAuthoredOn(SortOrder.DESCENDING)
            .withStatus(Task.TaskStatus.READY)
            .createCommand();
    assertTrue(cmd.getRequestLocator().contains("status=ready"));
    assertTrue(cmd.getRequestLocator().contains("_sort=-authored-on"));
  }

  @Test
  void shouldBuildBundlePagingQuery() {
    val cmd =
        TaskSearch.builder()
            .sortedByModified(SortOrder.DESCENDING)
            .withOffset(50)
            .withMaxCount(50)
            .createCommand();
    assertTrue(cmd.getRequestLocator().contains("__offset=50"));
    assertTrue(cmd.getRequestLocator().contains("_count=50"));
    assertTrue(cmd.getRequestLocator().contains("_sort=-modified"));
  }

  @Test
  void shouldBuildCustomQuery() {
    val cmd = TaskSearch.builder().withParameter("hello", "world").createCommand();
    assertTrue(cmd.getRequestLocator().contains("hello=world"));
  }

  @Test
  void shouldNotAddNullStatusOnComplexSearchQuery() {
    val cmd = TaskSearch.builder().withStatus(null).createCommand();
    assertFalse(cmd.getRequestLocator().contains("status="));
  }
}
