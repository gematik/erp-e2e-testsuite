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

package de.gematik.test.core.expectations;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import kong.unirest.core.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class HttpResponseExpectation<T> {
  private final HttpResponse<?> actual;
  private final Class<T> expectedPayloadType;
  private final List<VerificationStep<HttpResponse<?>>> responseSteps;

  private final List<VerificationStep<T>> payloadSteps;

  private HttpResponseExpectation(HttpResponse<?> actual, Class<T> expectedPayloadType) {
    this.actual = actual;
    this.expectedPayloadType = expectedPayloadType;
    this.responseSteps = new LinkedList<>();
    this.payloadSteps = new LinkedList<>();
  }

  public static <T> HttpResponseExpectation<T> expectFor(
      HttpResponse<?> actual, Class<T> expectedPayloadType) {
    return new HttpResponseExpectation<>(actual, expectedPayloadType);
  }

  /**
   * Expect actually as String.class
   *
   * @param actual response received from remote
   * @return for String-Responses
   */
  public static HttpResponseExpectation<String> expectFor(HttpResponse<?> actual) {
    return new HttpResponseExpectation<>(actual, String.class);
  }

  public HttpResponseExpectation<T> responseWith(VerificationStep<HttpResponse<?>> step) {
    this.responseSteps.add(step);
    return this;
  }

  public HttpResponseExpectation<T> hasResponseWith(VerificationStep<HttpResponse<?>> step) {
    return responseWith(step);
  }

  public HttpResponseExpectation<T> andResponse(VerificationStep<HttpResponse<?>> step) {
    return responseWith(step);
  }

  public HttpResponseExpectation<T> payloadHas(VerificationStep<T> step) {
    this.payloadSteps.add(step);
    return this;
  }

  public HttpResponseExpectation<T> payloadIs(VerificationStep<T> step) {
    return this.payloadHas(step);
  }

  public HttpResponseExpectation<T> andPayload(VerificationStep<T> step) {
    return this.payloadHas(step);
  }

  @SuppressWarnings("unchecked")
  public void ensure() {
    log.info(this.toString());
    int stepIdx = 1;
    for (val step : this.responseSteps) {
      log.info(
          format("+- [{0}] {1}: {2}", stepIdx++, step.getRequirement(), step.getExpectation()));
      step.apply(actual);
    }
    for (val step : this.payloadSteps) {
      log.info(
          format("+- [{0}] {1}: {2}", stepIdx++, step.getRequirement(), step.getExpectation()));
      step.apply((T) actual.getBody());
    }
  }

  @Override
  public String toString() {
    val verificationNote = new StringBuilder();
    val reqIds = this.getDistinctRequirementIds();

    if (!reqIds.isEmpty()) {
      verificationNote.append(format("({0})", String.join("; ", reqIds)));
    } else {
      verificationNote.append("(keine Anforderungen)");
    }

    return format(
        "Erwartung {0} für {1} mit expectedType {2}",
        verificationNote, actual, expectedPayloadType);
  }

  private List<String> getDistinctRequirementIds() {
    val responseReqs = getRequirementIdsOf(responseSteps);
    val payloadReqs = getRequirementIdsOf(payloadSteps);
    return Stream.concat(responseReqs, payloadReqs).distinct().toList();
  }

  private <P> Stream<String> getRequirementIdsOf(List<VerificationStep<P>> steps) {
    return steps.stream()
        .map(VerificationStep::getRequirement)
        .filter(req -> !req.isCustom())
        .distinct()
        .map(Requirement::getId);
  }
}
