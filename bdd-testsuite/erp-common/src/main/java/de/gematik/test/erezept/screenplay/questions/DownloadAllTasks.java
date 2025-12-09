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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.client.usecases.search.TaskSearch;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class DownloadAllTasks implements Question<ErxTaskBundle> {

  private SortOrder sortOrder;

  @Override
  public ErxTaskBundle answeredBy(Actor actor) {
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    TaskGetCommand cmd;
    if (sortOrder != null) {
      cmd = TaskSearch.getSortedByAuthoredOn(sortOrder);
    } else {
      cmd = new TaskGetCommand();
    }
    return erpClient.request(cmd).getExpectedResource();
  }

  public static DownloadAllTasks descending() {
    return sortedWith(SortOrder.DESCENDING);
  }

  public static DownloadAllTasks ascending() {
    return sortedWith(SortOrder.ASCENDING);
  }

  public static DownloadAllTasks sortedWith(String sortOrder) {
    return sortedWith(DequeStrategy.fromString(sortOrder));
  }

  public static DownloadAllTasks sortedWith(DequeStrategy dequeStrategy) {
    if (dequeStrategy.equals(DequeStrategy.FIFO)) {
      return ascending();
    } else {
      return descending();
    }
  }

  public static DownloadAllTasks sortedWith(SortOrder sortOrder) {
    return new DownloadAllTasks(sortOrder);
  }

  public static DownloadAllTasks unsorted() {
    return new DownloadAllTasks();
  }
}
