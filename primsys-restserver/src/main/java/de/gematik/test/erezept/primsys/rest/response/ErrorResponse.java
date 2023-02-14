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

package de.gematik.test.erezept.primsys.rest.response;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;

@Getter
@XmlRootElement
public class ErrorResponse {

  private final String message;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public ErrorResponse(ErpResponse erpResponse) {
    if (erpResponse.isOperationOutcome()) {
      val oo = erpResponse.getResource(OperationOutcome.class);
      val details = oo.getIssueFirstRep().getDetails().getText();
      val diagnostics = oo.getIssueFirstRep().getDiagnostics();
      this.message = format("{0}: {1}", details, diagnostics);
    } else if (erpResponse.isEmptyBody()) {
      this.message = "Unknown Error: ErpResponse contains empty Body";
    } else {
      this.message =
          format(
              "Unknown Error: ErpResponse contains FHIR Resource of Type {0}",
              erpResponse.getResourceType());
    }
  }

  @Override
  public String toString() {
    return "ErrorResponse{" + "message='" + message + '\'' + '}';
  }
}
