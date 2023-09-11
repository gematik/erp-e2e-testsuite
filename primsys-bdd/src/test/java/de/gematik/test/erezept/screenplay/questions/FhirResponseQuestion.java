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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import net.serenitybdd.screenplay.Question;
import org.hl7.fhir.r4.model.Resource;

public abstract class FhirResponseQuestion<R extends Resource> implements Question<ErpResponse<R>> {

  private final String operationName;

  protected FhirResponseQuestion(String operationName) {
    this.operationName = operationName;
  }

  public final String getOperationName() {
    return this.operationName;
  }

  @Override
  public String toString() {
    return getOperationName();
  }
}
