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

package de.gematik.test.erezept.abilities;

import com.beust.jcommander.Strings;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.test.erezept.client.exceptions.FhirValidationException;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockClient;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import one.util.streamex.EntryStream;

@Slf4j
public class UseTheTRegisterMockClient implements Ability {

  private final FhirParser parser;
  private final TRegisterMockClient tRegisterMockClient;

  private UseTheTRegisterMockClient(TRegisterMockClient tRegisterMockClient, FhirParser parser) {
    this.tRegisterMockClient = tRegisterMockClient;
    this.parser = parser;
  }

  public static UseTheTRegisterMockClient with(
      TRegisterMockClient tRegisterMockClient, FhirParser parser) {
    return new UseTheTRegisterMockClient(tRegisterMockClient, parser);
  }

  public static UseTheTRegisterMockClient with(HttpBClient restClient, FhirParser parser) {
    return with(TRegisterMockClient.withRestClient(restClient), parser);
  }

  /**
   * @deprecated (forRemoval = true) Use {@link #pollRequest(TRegisterMockDownloadRequest)} instead,
   *     which includes validation of the response. Downloads logs from T-Register using the given
   *     download request. actually only used in HelloWorld and unitTests
   */
  @Deprecated(forRemoval = true)
  public List<TRegisterLog> downloadRequest(TRegisterMockDownloadRequest request) {
    log.debug("Downloading T-Register logs using request {}", request);
    return tRegisterMockClient.downloadRequest(request);
  }

  /**
   * Polls the CarbonCopy from T-Register-Mock for logs using the given download request and
   * validates the FHIR content of the response.
   *
   * <p>This method sends a poll request to the T-Register-Mock using the provided {@link
   * TRegisterMockDownloadRequest}. The response is validated to ensure that all returned FHIR
   * resources are valid. If any validation fails, a {@link FhirValidationException} is thrown with
   * details about the validation errors.
   *
   * @param request the {@link TRegisterMockDownloadRequest} containing the parameters for the poll
   *     request
   * @return a list of {@link TRegisterLog} objects representing the logs returned by the T-Register
   * @throws FhirValidationException if any of the returned FHIR resources are invalid
   */
  public List<TRegisterLog> pollRequest(TRegisterMockDownloadRequest request) {
    log.debug("Polling T-Register logs using request {}", request);
    val req = tRegisterMockClient.pollRequest(request);

    return validateCarbonCopy(req);
  }

  private List<TRegisterLog> validateCarbonCopy(List<TRegisterLog> request) {
    val re = request.stream().map(log -> log.request().bodyAsString()).toList();
    val validationResults =
        re.stream().map(parser::validate).filter(vr -> !vr.isSuccessful()).toList();
    if (!validationResults.isEmpty()) {
      log.debug("Response From FD {}", request.get(0).request().bodyAsString());
      val errorMessage =
          EntryStream.of(validationResults)
              .map(
                  entry ->
                      "Message: "
                          + entry.getKey()
                          + "\n "
                          + Strings.join("\n", entry.getValue().getMessages().toArray()))
              .joining("\n");
      throw new FhirValidationException(errorMessage);
    }
    return request;
  }
}
