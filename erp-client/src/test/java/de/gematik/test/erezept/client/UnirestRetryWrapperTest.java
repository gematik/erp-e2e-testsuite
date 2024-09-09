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

package de.gematik.test.erezept.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.vau.VauException;
import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import java.util.stream.IntStream;
import kong.unirest.core.HttpRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.UnirestException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class UnirestRetryWrapperTest {

  @Mock HttpRequest<?> request = mock(HttpRequest.class);

  @Mock
  @SuppressWarnings("unchecked")
  HttpResponse<byte[]> response = (HttpResponse<byte[]>) mock(HttpResponse.class);

  @Test
  void shouldResponseOnFirstAttempt() {
    val req = (HttpRequest<? extends HttpRequest<?>>) mock(HttpRequest.class);
    when(req.asBytes()).thenReturn(response);

    val urw = new UnirestRetryWrapper();
    assertDoesNotThrow(() -> urw.requestWithRetries(req));
  }

  @Test
  void shouldResponseOnSecondAttempt() {
    when(request.asBytes())
        .thenThrow(new UnirestException(new HttpConnectTimeoutException("test")))
        .thenReturn(response);

    val urw = new UnirestRetryWrapper();
    assertDoesNotThrow(() -> urw.requestWithRetries(request));
  }

  @Test
  void shouldResponseOnThirdAttemptWithMultipleExceptions() {
    when(request.asBytes())
        .thenThrow(new UnirestException(new IOException("test")))
        .thenThrow(new UnirestException(new HttpConnectTimeoutException("test")))
        .thenReturn(response);

    val urw = new UnirestRetryWrapper();
    assertDoesNotThrow(() -> urw.requestWithRetries(request));
  }

  @Test
  void shouldNotRetryOnOtherException() {
    when(request.asBytes()).thenThrow(new UnirestException(new RuntimeException("test")));

    val urw = new UnirestRetryWrapper();
    assertThrows(VauException.class, () -> urw.requestWithRetries(request));
  }

  @Test
  void shouldThrowOnExceedingMaxAttempts() {
    IntStream.of(UnirestRetryWrapper.CONNECT_TIMEOUT + 1)
        .forEach(
            idx ->
                when(request.asBytes())
                    .thenThrow(
                        new UnirestException(new HttpConnectTimeoutException("test " + idx))));

    val urw = new UnirestRetryWrapper();
    val ex = assertThrows(VauException.class, () -> urw.requestWithRetries(request));
    assertEquals(ex.getCause().getClass(), HttpConnectTimeoutException.class);
  }
}
