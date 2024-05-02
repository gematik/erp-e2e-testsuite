/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.core.expectations.rawhttpverifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import kong.unirest.HttpResponse;
import lombok.val;

@SuppressWarnings("java:S1452")
public class RawHttpResponseVerifier {

  private RawHttpResponseVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<HttpResponse<?>> containsHeaderWith(
      String key, String value, RequirementsSet req) {
    Predicate<HttpResponse<?>> predicate =
        response -> response.getHeaders().getFirst(key).equals(value);
    val step =
        new VerificationStep.StepBuilder<HttpResponse<?>>(
            req.getRequirement(),
            format(
                "Es ist ein Header mit dem Key: {0} und passendem Value: {1} enthalten",
                key, value));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<String> stringBodyContains(
      String containedString, RequirementsSet req) {
    Predicate<String> predicate = response -> response.contains(containedString);
    val step =
        new VerificationStep.StepBuilder<String>(
            req.getRequirement(),
            format("Der Base64 encodierte Body enthielt: {0}", containedString));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<HttpResponse<?>> returnCode(int expected) {
    return returnCodeIs(expected);
  }

  public static VerificationStep<HttpResponse<?>> returnCode(int expected, RequirementsSet req) {
    return returnCodeIs(expected, req);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIs(int expected, RequirementsSet req) {
    return returnCodeIsIn(req, expected);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIs(int expected) {
    return returnCodeIs(expected, ErpAfos.A_19514_03);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIsIn(
      RequirementsSet req, int... expected) {
    val list = Arrays.stream(expected).boxed().toList();
    return returnCodeIsIn(list, req);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIsIn(int... expected) {
    return returnCodeIsIn(ErpAfos.A_19514_03, expected);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIsIn(List<Integer> expected) {
    return returnCodeIsIn(expected, ErpAfos.A_19514_03);
  }

  public static VerificationStep<HttpResponse<?>> returnCodeIsIn(
      List<Integer> expected, RequirementsSet req) {
    String codes;

    if (expected.isEmpty()) {
      throw new IllegalArgumentException("At least one expected Return Code is required");
    } else if (expected.size() > 10) {
      codes = format("[{0} .. {1}]", expected.get(0), expected.get(expected.size() - 1));
    } else {
      codes = expected.stream().map(String::valueOf).collect(Collectors.joining(" | "));
    }

    Predicate<HttpResponse<?>> predicate = r -> expected.stream().anyMatch(e -> r.getStatus() == e);
    val step =
        new VerificationStep.StepBuilder<HttpResponse<?>>(
            req.getRequirement(), format("ReturnCode muss {0} entsprechen", codes));
    return step.predicate(predicate).accept();
  }
}
