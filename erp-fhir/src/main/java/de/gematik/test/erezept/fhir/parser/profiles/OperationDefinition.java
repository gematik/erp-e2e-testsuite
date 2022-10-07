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

package de.gematik.test.erezept.fhir.parser.profiles;

import lombok.Getter;

@Getter
public enum OperationDefinition {
  ABORT("abort", "http://gematik.de/fhir/OperationDefinition/AbortOperationDefinition"),
  ACCEPT("accept", "http://gematik.de/fhir/OperationDefinition/AcceptOperationDefinition"),
  ACTIVATE("activate", "http://gematik.de/fhir/OperationDefinition/ActivateOperationDefinition"),
  CLOSE("close", "http://gematik.de/fhir/OperationDefinition/CloseOperationDefinition"),
  CREATE("create", "http://gematik.de/fhir/OperationDefinition/CreateOperationDefinition"),
  REJECT("reject", "http://gematik.de/fhir/OperationDefinition/RejectOperationDefinition");

  private final String name;
  private final String canonicalUrl;

  OperationDefinition(String name, String url) {
    this.name = name;
    this.canonicalUrl = url;
  }
}
