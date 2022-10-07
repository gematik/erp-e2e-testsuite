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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import java.util.ArrayList;
import lombok.val;

public class TaskSearch {

  private TaskSearch() {
    throw new AssertionError();
  }

  public static TaskGetCommand getSortedByAuthoredOn(SortOrder order) {
    return getSorted("authored-on", order);
  }

  public static TaskGetCommand getSorted(String sortBy, SortOrder order) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new SortParameter(sortBy, order));
    return new TaskGetCommand(searchParams);
  }
}
