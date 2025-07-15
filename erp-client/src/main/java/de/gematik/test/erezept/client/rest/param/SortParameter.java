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

package de.gematik.test.erezept.client.rest.param;

import static java.text.MessageFormat.format;

public class SortParameter implements IQueryParameter {

  private final String parameter;
  private final SortOrder order;

  public SortParameter(String parameter) {
    this(parameter, SortOrder.DESCENDING);
  }

  public SortParameter(String parameter, SortOrder order) {
    this.parameter = parameter;
    this.order = order;
  }

  @Override
  public String encode() {
    return format("_sort={0}", order.sign(parameter));
  }

  @Override
  public String parameter() {
    return "_sort";
  }

  @Override
  public String value() {
    return order.sign(parameter);
  }
}
