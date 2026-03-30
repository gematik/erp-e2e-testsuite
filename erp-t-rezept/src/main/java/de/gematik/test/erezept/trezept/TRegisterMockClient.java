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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.RawHttpCodec;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class TRegisterMockClient {

  private final HttpBClient restClient;
  private final ObjectMapper om =
      new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  private final RawHttpCodec codec = RawHttpCodec.defaultCodec();
  private final long interval;
  private final long maxWait;

  private TRegisterMockClient(HttpBClient restClient, long interval, long maxWait) {
    this.restClient = restClient;
    this.interval = interval;
    this.maxWait = maxWait;
  }

  public static TRegisterMockClient withRestClient(HttpBClient restClient) {
    long interval = 5000L;
    long maxWait = 30000L;
    return withRestClient(restClient, interval, maxWait);
  }

  public static TRegisterMockClient withRestClient(
      HttpBClient restClient, long interval, long maxWait) {
    return new TRegisterMockClient(restClient, interval, maxWait);
  }

  /** Downloads logs from T-Register for the given request. */
  @SneakyThrows
  public List<TRegisterLog> downloadRequest(TRegisterMockDownloadRequest request) {
    val response = restClient.send(request);
    val body = response.isEmptyBody() ? "[]" : response.bodyAsString();
    if (response.statusCode() > 400) {
      throw new AssertionError(
          "Request to T-Register-Mock failed with status code "
              + response.statusCode()
              + ". This might be caused by a missing or incorrect proxy parameter"
              + " (https.gematikProxy).");
    }
    List<TRegisterDto> dtoList = om.readValue(body, new TypeReference<>() {});
    if (!dtoList.isEmpty()) {
      val first =
          dtoList.stream()
              .filter(entry -> entry.request() != null && entry.request().length() > 500)
              .findFirst()
              .orElse(null);
      List<TRegisterLog> responseList = new ArrayList<>();
      if (first != null) {
        if (codec.decodeRequest(first.request()).isEmptyBody()) {
          responseList.add(
              new TRegisterLog(
                  first.tsp(),
                  first.xRequestId(),
                  first.key(),
                  HttpBRequest.post().withPayload("{" + first.request().split("\\{", 2)[1])));

        } else {
          responseList.add(
              new TRegisterLog(
                  first.tsp(),
                  first.xRequestId(),
                  first.key(),
                  codec.decodeRequest(first.request())));
        }
      }

      return responseList;
    } else {
      return List.of();
    }
  }

  @SneakyThrows
  public List<TRegisterLog> pollRequest(TRegisterMockDownloadRequest request) {
    long startTime = System.currentTimeMillis();
    List<TRegisterLog> list;
    int count = 0;

    do {
      count++;

      list = this.downloadRequest(request);
      if (!list.isEmpty()) {
        break;
      }

      log.info(
          "Tried to download from TRegisterMock with {} iterations and a period of {} ms",
          count,
          (System.currentTimeMillis() - startTime));

      Thread.sleep(interval);

    } while (System.currentTimeMillis() - startTime < maxWait);

    if (list.isEmpty()) {
      throw new PollingTimeoutException(
          String.format(
              "No request returned after %d seconds waiting for call: %s",
              (maxWait * 3 / 1000), request.urlPath()));
    }

    return list;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record TRegisterDto(Long tsp, String xRequestId, String key, String request) {}
}
