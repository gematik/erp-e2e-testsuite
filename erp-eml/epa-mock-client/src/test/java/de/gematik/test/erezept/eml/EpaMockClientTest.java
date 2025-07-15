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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.utils.ResourceLoader;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EpaMockClientTest {
  private HttpBClient restClient;
  private EpaMockClient epaMockClient;
  private HttpBResponse mockHttpBResponse;

  @BeforeEach
  void setUp() {
    restClient = mock(HttpBClient.class);
    epaMockClient = EpaMockClient.withRestClient(restClient);
    mockHttpBResponse = mock(HttpBResponse.class);
  }

  @Test
  void testDownloadRequestSize() {
    val example = ResourceLoader.readFileFromResource("example_get_log.json");
    val response = mock(HttpBResponse.class);

    when(response.bodyAsString()).thenReturn(example);
    when(restClient.send(any(HttpBRequest.class))).thenReturn(response);

    val emlRequests = epaMockClient.downloadRequest(new DownloadRequestByPrescriptionId("xy"));
    assertEquals(5, emlRequests.size());
  }

  @Test
  void testDownloadRequestSuccessfulResponse() {
    val mockRequest = mock(EpaMockDownloadRequest.class);
    val httpBRequest = HttpBRequest.get().urlPath("/some-url").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);

    val responseBody = ResourceLoader.readFileFromResource("example_get_log.json");
    when(mockHttpBResponse.bodyAsString()).thenReturn(responseBody);
    when(mockHttpBResponse.statusCode()).thenReturn(200);
    when(restClient.send(any())).thenReturn(mockHttpBResponse);

    val result = epaMockClient.downloadRequest(mockRequest);

    assertNotNull(result);
    assertEquals(5, result.size());
    verify(restClient).send(any());
  }

  @Test
  void testDownloadRequestFailureResponse() {
    val mockRequest = mock(EpaMockDownloadRequest.class);
    val httpBRequest = HttpBRequest.get().urlPath("/some-url").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);
    when(restClient.send(any())).thenThrow(new RuntimeException("Request failed"));

    assertThrows(RuntimeException.class, () -> epaMockClient.downloadRequest(mockRequest));
  }

  @Test
  void testConfigRequestSuccessfulResponse() {
    val mockRequest = mock(EpaMockConfigRequest.class);
    val httpBRequest = HttpBRequest.post().urlPath("/config-url").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);
    when(restClient.send(any())).thenReturn(mockHttpBResponse);
    when(mockHttpBResponse.statusCode()).thenReturn(200);

    boolean result = epaMockClient.configRequest(mockRequest);

    assertTrue(result);
    verify(restClient).send(any());
  }

  @Test
  void testConfigRequestFailureResponse() {
    val mockRequest = mock(EpaMockConfigRequest.class);
    val httpBRequest = HttpBRequest.post().urlPath("/config-url").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);
    when(restClient.send(any())).thenReturn(mockHttpBResponse);
    when(mockHttpBResponse.statusCode()).thenReturn(400);

    boolean result = epaMockClient.configRequest(mockRequest);

    assertFalse(result);
    verify(restClient).send(any());
  }

  @Test
  void testPollRequest() {
    val mockRequest = mock(EpaMockDownloadRequest.class);
    val httpBRequest = HttpBRequest.get().urlPath("/log").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);

    val responseOne = HttpBResponse.status(200).withPayload("[]");
    val responseTwoBody = ResourceLoader.readFileFromResource("example_get_log.json");
    val responseTwo = HttpBResponse.status(200).withPayload(responseTwoBody);
    when(restClient.send(any())).thenReturn(responseOne, responseOne, responseTwo);

    val result = epaMockClient.pollRequest(mockRequest);
    assertNotNull(result);
    assertEquals(5, result.size());
    verify(restClient, times(3)).send(any());
  }

  @Test
  void testPollRequestPollingTimeoutException() {

    val mockRequest = mock(EpaMockDownloadRequest.class);
    val httpBRequest = HttpBRequest.get().urlPath("/log").withoutPayload();
    when(mockRequest.getHttpBRequest()).thenReturn(httpBRequest);

    val emptyResponse = HttpBResponse.status(200).withPayload("[]");
    when(restClient.send(any())).thenReturn(emptyResponse);

    val shortEpaMockClient = EpaMockClient.withRestClient(restClient, 100, 600);
    assertThrows(PollingTimeoutException.class, () -> shortEpaMockClient.pollRequest(mockRequest));
    verify(restClient, atLeast(4)).send(any());
  }

  @ParameterizedTest()
  @ValueSource(strings = {"[]", ""})
  @NullSource
  void shouldRecognizeEmptyResponse(String body) {
    val emptyResponse = HttpBResponse.status(200).withPayload(body);
    when(restClient.send(any())).thenReturn(emptyResponse);

    val result = epaMockClient.downloadRequest(new DownloadRequestByPrescriptionId("xy"));
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
