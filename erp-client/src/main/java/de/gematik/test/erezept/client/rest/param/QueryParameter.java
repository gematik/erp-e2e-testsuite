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

package de.gematik.test.erezept.client.rest.param;

import static java.text.MessageFormat.format;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QueryParameter implements IQueryParameter {

  private final String parameter;
  private final String value;

  public QueryParameter(String parameter, String value) {
    this.parameter = parameter;
    this.value = value;
  }

  /**
   * This constructor can be used to form search Requests e.g. authored-on=ge2020 which has the
   * pattern [parameter]=[prefix][value]
   *
   * @param parameter
   * @param prefix
   * @param value
   */
  public QueryParameter(String parameter, SearchPrefix prefix, String value) {
    this.parameter = parameter;
    this.value = prefix.value() + value;
  }

  @Override
  public String encode() {
    return format(
        "{0}={1}",
        URLEncoder.encode(parameter, StandardCharsets.UTF_8),
        URLEncoder.encode(value, StandardCharsets.UTF_8));
  }

  @Override
  public String parameter() {
    return parameter;
  }

  @Override
  public String value() {
    return value;
  }
}
