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

package de.gematik.test.erezept.pharmacyserviceprovider.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResponsesCheckTest {

  @Test
  public void getResponseTransAcAndBody() {
    var resp = ResponsesCheck.generateResponse("transactiontestId ", "123".getBytes(), 200);
    assertEquals(
        "200 erfolgreiche Datenübermittlung, no telematikID",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void getResponseNoTransactionID() {
    var resp = ResponsesCheck.generateResponse(null, "123".getBytes(), 100);
    assertEquals(
        "100 erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void getResponseNoBody() {
    var resp = ResponsesCheck.generateResponse("testtranaction", null, 100);
    assertEquals(
        "404 erfolgreiche Datenübermittlung, no telematikID, no body arrived",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void getResponseNoBodyNoTransaction() {
    var resp = ResponsesCheck.generateResponse(null, null, 100);
    assertEquals(
        "404 erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived, no body"
            + " arrived",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void generateResponseAll3() {
    var resp = ResponsesCheck.generateResponse("tiId", "transactiontestId ", "123".getBytes(), 200);
    assertEquals("200 erfolgreiche Datenübermittlung", resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void generateResponseNoTiID() {
    var resp = ResponsesCheck.generateResponse(null, "transactiontestId ", "123".getBytes(), 200);
    assertEquals(
        "200 erfolgreiche Datenübermittlung, no telematikID",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void generateResponseNoTransactionID() {
    var resp = ResponsesCheck.generateResponse("tiId", null, "123".getBytes(), 200);
    assertEquals(
        "200 erfolgreiche Datenübermittlung, no transactionID",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void generateResponseNoBody() {
    var resp = ResponsesCheck.generateResponse("tiId", "transactiontestId ", null, 200);
    assertEquals(
        "200 erfolgreiche Datenübermittlung, body is to short",
        resp.getStatus() + " " + resp.getEntity());
  }

  @Test
  public void generateResponseNoArguments() {
    var resp = ResponsesCheck.generateResponse(null, null, null, 200);
    assertEquals(
        "200 erfolgreiche Datenübermittlung, no telematikID, no transactionID, body is to short",
        resp.getStatus() + " " + resp.getEntity());
  }
}
