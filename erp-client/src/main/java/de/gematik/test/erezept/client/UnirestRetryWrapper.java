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

package de.gematik.test.erezept.client;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.vau.VauException;
import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;
import kong.unirest.core.HttpRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class UnirestRetryWrapper {

  private static final int MAX_CONNECT_ATTEMPTS = 12;
  public static final int CONNECT_TIMEOUT = 10; // in seconds

  /**
   * due to issues while executing on jenkins we need to retry the request because of sporadic {@see
   * java.net.http.HttpConnectTimeoutException}
   *
   * @param req is the request to be performed
   * @return the HttpResponse if the request was successful within {@value MAX_CONNECT_ATTEMPTS}
   *     attempts each taking up to {@value CONNECT_TIMEOUT}s
   */
  public <R extends HttpRequest<?>> HttpResponse<byte[]> requestWithRetries(HttpRequest<R> req) {
    var attempt = 0;
    Throwable lastException = null;
    while (attempt <= MAX_CONNECT_ATTEMPTS) {
      try {
        return req.asBytes();
      } catch (UnirestException ure) {
        val causeType = ure.getCause().getClass();
        if (causeType.equals(HttpConnectTimeoutException.class)
            || causeType.equals(IOException.class)) {
          // retry only on HttpConnectTimeoutException
          log.warn(
              "Retry VAU-Request due to {}: {}/{}",
              causeType.getSimpleName(),
              attempt,
              MAX_CONNECT_ATTEMPTS);
          lastException = ure.getCause();
          attempt++;
        } else {
          // and re-throw on all other exceptions
          throw new VauException(
              format(
                  "Error while sending request to VAU on attempt {0}/{1} with {2}",
                  attempt, MAX_CONNECT_ATTEMPTS, ure.getClass().getSimpleName()),
              ure);
        }
      }
    }
    throw new VauException(
        format("Error while sending request to VAU: retries exhausted after {0} attempts", attempt),
        lastException);
  }
}
