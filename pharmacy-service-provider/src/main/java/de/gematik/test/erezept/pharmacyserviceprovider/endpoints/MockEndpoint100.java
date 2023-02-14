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

package de.gematik.test.erezept.pharmacyserviceprovider.endpoints;

import de.gematik.test.erezept.pharmacyserviceprovider.helper.ResponsesCheck;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("100")
@Consumes("application/pkcs7-mime")
public class MockEndpoint100 {

  private static final int RESPONSE_CODE = 100;

  @POST
  @Path("{ti_id}")
  public Response versOnly(
      @PathParam("ti_id") String telemId, @QueryParam("req") String transactionId, byte[] body) {
    return ResponsesCheck.generateResponse(telemId, transactionId, body, RESPONSE_CODE);
  }

  @POST
  public Response decodeVersOnlySecondId(@QueryParam("req") String transactionId, byte[] body) {
    return ResponsesCheck.generateResponse(transactionId, body, RESPONSE_CODE);
  }
}
