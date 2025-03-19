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
 */

package de.gematik.test.erezept.pharmacyserviceprovider.helper;

import jakarta.ws.rs.core.Response;

public class ResponsesCheck {

  private ResponsesCheck() {}

  public static Response generateResponse(String transactionId, byte[] body, int respCode) {
    String respMessage = "erfolgreiche Datenübermittlung, no telematikID";
    if (ValidatePayload.stringIsNull(transactionId)) {
      respMessage = respMessage + ", no transactionID arrived";
    }
    if (!ValidatePayload.bodyHasContent(body)) {
      respMessage = respMessage + ", no body arrived";
      respCode = 404;
    }
    return Response.status(respCode).entity(respMessage).build();
  }

  public static Response generateResponse(
      String telemId, String transactionId, byte[] body, int responsCode) {
    String respMessage = "erfolgreiche Datenübermittlung";
    if (ValidatePayload.stringIsNull(telemId)) respMessage = respMessage + ", no telematikID";
    if (ValidatePayload.stringIsNull(transactionId)) {
      respMessage = respMessage + ", no transactionID";
    }
    if (!ValidatePayload.bodyHasContent(body)) respMessage = respMessage + ", body is to short";
    return Response.status(responsCode).entity(respMessage).build();
  }
}
