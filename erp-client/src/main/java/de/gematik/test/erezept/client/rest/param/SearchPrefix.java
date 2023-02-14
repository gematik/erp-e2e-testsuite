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

package de.gematik.test.erezept.client.rest.param;

public enum SearchPrefix {
  EQ("eq"),
  NE("ne"),
  GT("gt"),
  LT("lt"),
  GE("ge"),
  LE("le"),
  SA("sa"),
  EB("eb"),
  AP("ap");

  private final String operatorName;

  SearchPrefix(String value) {
    this.operatorName = value;
  }

  public String value() {
    return this.operatorName;
  }
}
