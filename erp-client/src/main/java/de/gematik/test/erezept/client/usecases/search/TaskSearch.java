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

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.hl7.fhir.r4.model.Task;

public class TaskSearch {

  private TaskSearch() {
    throw new AssertionError();
  }

  public static TaskGetCommand getSortedByAuthoredOn(SortOrder order) {
    return new Builder().sortedByAuthoredOn(order).createCommand();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<IQueryParameter> searchParams = new LinkedList<>();

    public Builder sortedByAuthoredOn(SortOrder order) {
      return sortedBy("authored-on", order);
    }

    public Builder sortedByModified(SortOrder order) {
      return sortedBy("modified", order);
    }

    public Builder withOffset(int offset) {
      searchParams.add(new QueryParameter("__offset", String.valueOf(offset)));
      return this;
    }

    public Builder withMaxCount(int count) {
      searchParams.add(new QueryParameter("_count", String.valueOf(count)));
      return this;
    }

    public Builder withStatus(@Nullable Task.TaskStatus status) {
      if (status != null) searchParams.add(new QueryParameter("status", status.toCode()));
      return this;
    }

    public Builder withParameter(String key, String value) {
      return withParameter(new QueryParameter(key, value));
    }

    public Builder withParameter(IQueryParameter parameter) {
      searchParams.add(parameter);
      return this;
    }

    private Builder sortedBy(String value, SortOrder order) {
      searchParams.add(new SortParameter(value, order));
      return this;
    }

    public TaskGetCommand createCommand() {
      return new TaskGetCommand(this.searchParams);
    }
  }
}
