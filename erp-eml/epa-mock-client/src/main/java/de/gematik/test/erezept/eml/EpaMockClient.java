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

package de.gematik.test.erezept.eml;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.RawHttpCodec;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class EpaMockClient {
  private final HttpBClient restClient;
  private final ObjectMapper om =
      new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  private final RawHttpCodec codec = RawHttpCodec.defaultCodec();
  private final long interval;
  private final long maxWait;

  private EpaMockClient(HttpBClient restClient, long interval, long maxWait) {
    this.restClient = restClient;
    this.interval = interval;
    this.maxWait = maxWait;
  }

  public static EpaMockClient withRestClient(HttpBClient restClient) {
    long interval = 5000L;
    long maxWait = 60000L;
    return withRestClient(restClient, interval, maxWait);
  }

  public static EpaMockClient withRestClient(HttpBClient restClient, long interval, long maxWait) {
    return new EpaMockClient(restClient, interval, maxWait);
  }

  @SneakyThrows
  public List<ErpEmlLog> downloadRequest(EpaMockDownloadRequest request) {
    val req = request.getHttpBRequest();
    val response = restClient.send(req);
    val body = response.isEmptyBody() ? "[]" : response.bodyAsString();
    List<ErpEmlDto> dtoList = om.readValue(body, new TypeReference<>() {});
    return dtoList.stream()
        .map(
            dto ->
                new ErpEmlLog(
                    dto.tsp(),
                    dto.prescriptionId(),
                    dto.key(),
                    codec.decodeRequest(dto.request()),
                    codec.decodeResponse(dto.response())))
        .toList();
  }

  @SneakyThrows
  public List<ErpEmlLog> pollRequest(EpaMockDownloadRequest request) {
    long startTime = System.currentTimeMillis();
    List<ErpEmlLog> list;
    var count = 0;
    do {
      count++;
      list = this.downloadRequest(request);
      if (!list.isEmpty()) {
        break;
      }
      log.info(
          format(
              "tried to download from EpaMock with {0} Iterations and a Period of: {1} ",
              count, (System.currentTimeMillis() - startTime)));
      Thread.sleep(interval);
    } while (System.currentTimeMillis() - startTime < maxWait);

    if (list.isEmpty()) {
      throw new PollingTimeoutException(
          "No request returned after " + maxWait / 1000 + " seconds wait time");
    }
    return list;
  }

  public boolean configRequest(EpaMockConfigRequest request) {
    HttpBResponse response = restClient.send(request.getHttpBRequest());
    return response.statusCode() < 205;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ErpEmlDto(
      Long tsp, String prescriptionId, String key, String request, String response) {}
}
