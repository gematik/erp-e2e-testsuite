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

package de.gematik.test.erezept.eml;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
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
    long maxWait = 360000L;
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

  public List<ErpEmlLog> pollRequest(EpaMockDownloadRequest request) {
    return this.pollRequest(request, "");
  }

  private List<ErpEmlLog> filterByPathPart(List<ErpEmlLog> list, String filter) {
    if (Strings.isNullOrEmpty(filter)) {
      return list;
    } else {
      return list.stream().filter(log -> log.request().urlPath().contains(filter)).toList();
    }
  }

  @SneakyThrows
  public List<ErpEmlLog> pollRequest(EpaMockDownloadRequest request, String filter) {
    long startTime = System.currentTimeMillis();
    List<ErpEmlLog> list;
    var count = 0;
    do {
      count++;

      list = filterByPathPart(this.downloadRequest(request), filter);
      if (!list.isEmpty()) {
        break;
      }
      log.info(
          "tried to download from EpaMock with {} Iterations and a Period of: {} ",
          count,
          (System.currentTimeMillis() - startTime));
      Thread.sleep(interval); // busy wait by intention
    } while (System.currentTimeMillis() - startTime < maxWait);
    log.info("Duration for download was {} millis", (System.currentTimeMillis() - startTime));

    if (list.isEmpty()) {
      throw new PollingTimeoutException(
          format(
              "No request returned after {0} seconds wait´n for call: {1}",
              (maxWait / 1000), request.getHttpBRequest().urlPath()));
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
