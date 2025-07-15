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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConsentDeleteBuilder {

  private final List<QueryParameter> queryParameters;

  public static ConsentDeleteCommand withValidParams() {
    return new ConsentDeleteCommand();
  }

  public static ConsentDeleteCommand withCustomCategory(String categoryValue) {
    return new ConsentDeleteCommand(List.of(new QueryParameter("category", categoryValue)));
  }

  public static ConsentDeleteBuilder withCustomQuerySet() {
    return new ConsentDeleteBuilder(new LinkedList<>());
  }

  public ConsentDeleteBuilder addQuery(QueryParameter queryParameter) {
    this.queryParameters.add(queryParameter);
    return this;
  }

  public ConsentDeleteCommand build() {
    return new ConsentDeleteCommand(queryParameters);
  }
}
