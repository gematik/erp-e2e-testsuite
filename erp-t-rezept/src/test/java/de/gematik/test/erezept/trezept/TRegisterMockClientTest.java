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

package de.gematik.test.erezept.trezept;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TRegisterMockClientTest {

  private final String contentBodyAsString =
      ResourceLoader.readFileFromResource("bFarmMockResponse.json");
  private HttpBClient restClient;
  private TRegisterMockClient tRegisterMockClient;

  @BeforeEach
  void setUp() {
    restClient = mock(HttpBClient.class);
    tRegisterMockClient = TRegisterMockClient.withRestClient(restClient, 10L, 50L);
  }

  @Test
  void DownloadRequestFailureResponse() {
    val key = "98765";
    val request = new TRegisterMockDownloadRequest(key);

    when(restClient.send(request)).thenThrow(new RuntimeException("Request failed"));

    assertThrows(RuntimeException.class, () -> tRegisterMockClient.downloadRequest(request));
  }

  @Test
  void DownloadRequestSuccessfulResponse() {
    val key = "98765";
    val request = new TRegisterMockDownloadRequest(key);
    val response = HttpBResponse.status(200).withPayload(contentBodyAsString);
    when(restClient.send(request)).thenReturn(response);

    List<TRegisterLog> logs = tRegisterMockClient.downloadRequest(request);

    assertNotNull(logs);
    assertEquals(1, logs.size());

    TRegisterLog first = logs.get(0);
    assertEquals("166.000.000.000.973.21", first.key());
    assertEquals("91d74726-8184-45e5-9c9c-aec1b8fa1ec4", first.xRequestId());
    assertNotNull(first.request());

    verify(restClient).send(request);
    verifyNoMoreInteractions(restClient);
  }

  @Test
  void DownloadRequestEmptyBody() {
    val key = "12345";
    val request = new TRegisterMockDownloadRequest(key);

    val response = HttpBResponse.status(200).withoutPayload();
    when(restClient.send(request)).thenReturn(response);
    val answer = tRegisterMockClient.downloadRequest(request);
    assertNotNull(answer);
    assertTrue(answer.isEmpty());
    verify(restClient).send(request);
    verifyNoMoreInteractions(restClient);
  }

  @Test
  void DownloadRequestWithFourOFour() {
    val key = "12345";
    val request = new TRegisterMockDownloadRequest(key);

    val response = HttpBResponse.status(404).withoutPayload();
    when(restClient.send(request)).thenReturn(response);
    assertThrows(AssertionError.class, () -> tRegisterMockClient.downloadRequest(request));
  }

  @Test
  void DownloadRequestInvalidJson() {
    val key = "123456789";
    val request = new TRegisterMockDownloadRequest(key);
    val invalidJson = "{ this is not valid json ]";

    val response = HttpBResponse.status(200).withPayload(invalidJson);
    when(restClient.send(request)).thenReturn(response);

    assertThrows(
        com.fasterxml.jackson.databind.JsonMappingException.class,
        () -> tRegisterMockClient.downloadRequest(request));
  }

  @Test
  void PollRequestSuccess() {

    val key = "123456789";
    val request = new TRegisterMockDownloadRequest(key);

    val response = HttpBResponse.status(200).withPayload(contentBodyAsString);

    when(restClient.send(request)).thenReturn(response);

    List<TRegisterLog> logs = tRegisterMockClient.pollRequest(request);

    assertNotNull(logs);
    assertEquals(1, logs.size());

    verify(restClient, atLeastOnce()).send(request);
  }

  @Test
  void PollRequestTimeout() {

    val key = "000000";
    val request = new TRegisterMockDownloadRequest(key);

    val emptyResponse = HttpBResponse.status(200).withoutPayload();
    when(restClient.send(request)).thenReturn(emptyResponse);

    assertThrows(PollingTimeoutException.class, () -> tRegisterMockClient.pollRequest(request));

    verify(restClient, atLeastOnce()).send(request);
  }

  @Test
  void shouldDownloadRealContent() {

    val responseBody = HttpBResponse.status(200).withPayload(contentBodyAsString);

    val mockRestClient = mock(HttpBClient.class);
    when(mockRestClient.send(any(TRegisterMockDownloadRequest.class))).thenReturn(responseBody);

    val key = "000000";
    val tRegisterMockDownloadRequest = new TRegisterMockDownloadRequest(key);

    val tRegisterClient = TRegisterMockClient.withRestClient(mockRestClient);
    val request = tRegisterClient.downloadRequest(tRegisterMockDownloadRequest);

    assertNotNull(request);
    assertTrue(request.get(0).request().bodyAsString().startsWith("{\"meta\""));
  }

  @Test
  void shouldValidateWhileDownloadRealContent() {

    val responseBody = HttpBResponse.status(200).withPayload(contentBodyAsString);

    val mockRestClient = mock(HttpBClient.class);
    when(mockRestClient.send(any(TRegisterMockDownloadRequest.class))).thenReturn(responseBody);

    val key = "000000";
    val tRegisterMockDownloadRequest = new TRegisterMockDownloadRequest(key);

    val tRegisterClient = TRegisterMockClient.withRestClient(mockRestClient);
    val requst = tRegisterClient.downloadRequest(tRegisterMockDownloadRequest);

    assertNotNull(requst);
  }
}
