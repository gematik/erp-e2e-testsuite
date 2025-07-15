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

package de.gematik.test.erezept.eml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import lombok.val;
import org.junit.jupiter.api.Test;

class DownloadRequestTest {

  @Test
  void shouldDownloadRequestByPrescriptionId() {
    val expectedPrescriptionId = "12345";
    val requestByPrescriptionId = new DownloadRequestByPrescriptionId(expectedPrescriptionId);

    val httpBRequest = requestByPrescriptionId.getHttpBRequest();
    assertNotNull(httpBRequest);
    assertEquals(HttpRequestMethod.GET, httpBRequest.method());
    assertTrue(
        httpBRequest.urlPath().contains(expectedPrescriptionId),
        "The URL path should contain the expected PrescriptionId");
  }

  @Test
  void shouldDownloadRequestByKvnr() {
    val expectedKvnr = "123456";
    val requestByKvnr = new DownloadRequestByKvnr(expectedKvnr);

    val httpBRequest = requestByKvnr.getHttpBRequest();
    assertNotNull(httpBRequest);
    assertEquals(HttpRequestMethod.GET, httpBRequest.method());
    assertTrue(
        httpBRequest.urlPath().contains(expectedKvnr),
        "The URL path should contain the expected Kvnr");
  }
}
