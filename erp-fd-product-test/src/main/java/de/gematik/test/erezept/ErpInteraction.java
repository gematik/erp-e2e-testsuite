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

package de.gematik.test.erezept;

import de.gematik.test.core.expectations.ErpResponseExpectation;
import de.gematik.test.erezept.client.rest.ErpResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
public class ErpInteraction<R extends Resource> {

  private final ErpResponse<R> response;

  public ErpInteraction(ErpResponse<R> response) {
    this.response = response;
  }

  public final Class<R> getExpectedType() {
    return response.getExpectedType();
  }

  public final boolean isOfExpectedType() {
    return response.isOfExpectedType();
  }

  public final R getExpectedResponse() {
    return response.getExpectedResource();
  }

  public final ErpResponseExpectation<R> expectation() {
    return ErpResponseExpectation.expectFor(response, response.getExpectedType());
  }

  public final ErpResponseExpectation<Resource> asIndefiniteResource() {
    return ErpResponseExpectation.expectFor(response, Resource.class);
  }

  public final ErpResponseExpectation<OperationOutcome> asOperationOutcome() {
    return ErpResponseExpectation.expectFor(response, OperationOutcome.class);
  }
}
