/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.core.expectations;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.client.rest.ErpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ErpResponseExpectation<T extends Resource> {

  private final ErpResponse actual;
  private final Class<T> expectedPayloadType;
  private final List<VerificationStep<ErpResponse>>
      responseSteps; // verifying the outer ErpResponse
  private final List<VerificationStep<T>> payloadSteps; // verifying the contained FHIR Resource

  public ErpResponseExpectation(ErpResponse actual, Class<T> expectedPayloadType) {
    this.actual = actual;
    this.responseSteps = new LinkedList<>();
    this.payloadSteps = new LinkedList<>();
    this.expectedPayloadType = expectedPayloadType;
  }

  public static <T extends Resource> ErpResponseExpectation<T> expectFor(
      ErpResponse actual, Class<T> expectedPayloadType) {
    return new ErpResponseExpectation<>(actual, expectedPayloadType);
  }

  public ErpResponseExpectation<T> responseWith(VerificationStep<ErpResponse> step) {
    this.responseSteps.add(step);
    return this;
  }

  public ErpResponseExpectation<T> hasResponseWith(VerificationStep<ErpResponse> step) {
    return responseWith(step);
  }

  public ErpResponseExpectation<T> andResponse(VerificationStep<ErpResponse> step) {
    return responseWith(step);
  }

  public ErpResponseExpectation<T> has(VerificationStep<T> step) {
    this.payloadSteps.add(step);
    return this;
  }

  public ErpResponseExpectation<T> and(VerificationStep<T> step) {
    return this.has(step);
  }

  public ErpResponseExpectation<T> is(VerificationStep<T> step) {
    return this.has(step);
  }

  public void ensure() {
    log.info(this.toString());

    // first assert all steps of the ErpResponse itself as otherwise it would make no sense to
    // verify the payload,
    // if e.g. the ErpResponse already signalises that the payload is not of the expected type
    int stepIdx = 1;
    for (val step : this.responseSteps) {
      log.info(
          format("+- [{0}] {1}: {2}", stepIdx++, step.getRequirement(), step.getExpectation()));
      step.apply(actual);
    }

    /*
    Throw an AssertionError if the payload does not match the expected payload because it is not ensured
    that the payload was checked already. If payload was not checked and does not match the expected payload
    the following payloadSteps would otherwise fail with an unexpected Exception and result in broken tests
     */
    val payload =
        actual
            .getResourceOptional(expectedPayloadType)
            .orElseThrow(
                () ->
                    new AssertionError(
                        format(
                            "{0} is not of expected type {1}",
                            actual, expectedPayloadType.getSimpleName())));

    // now assert the steps for the payload separately
    for (val step : this.payloadSteps) {
      log.info(
          format("+- [{0}] {1}: {2}", stepIdx++, step.getRequirement(), step.getExpectation()));
      step.apply(payload);
    }
  }

  private List<String> getDistinctRequirementIds() {
    val responseReqs = getRequirementIdsOf(responseSteps);
    val payloadReqs = getRequirementIdsOf(payloadSteps);
    return Stream.concat(responseReqs, payloadReqs).distinct().collect(Collectors.toList());
  }

  private <R> Stream<String> getRequirementIdsOf(List<VerificationStep<R>> steps) {
    return steps.stream()
        .map(VerificationStep::getRequirement)
        .filter(req -> !req.isCustom())
        .distinct()
        .map(Requirement::getId);
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

    return format("Erwartung {0} für {1}", verificationNote, actual);
  }
}
