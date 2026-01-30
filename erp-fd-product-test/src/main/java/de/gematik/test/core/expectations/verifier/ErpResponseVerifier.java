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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.client.rest.ErpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.openqa.selenium.InvalidArgumentException;

/*
Why is java:S1452 suppressed?
We do require only the outer ErpResponse for these checks, so the concrete payload type is not
required to be known! Introducing concrete generics will increase the complexity tremendously
without any real benefit in this case
*/
@SuppressWarnings("java:S1452")
public class ErpResponseVerifier {

  private ErpResponseVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCode(int expected) {
    return returnCodeIs(expected);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCode(
      int expected, RequirementsSet req) {
    return returnCodeIs(expected, req);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIs(int expected) {
    return returnCodeIs(expected, ErpAfos.A_19514);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIs(
      int expected, RequirementsSet req) {
    return returnCodeIsIn(req, expected);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsNot(int expected) {
    return returnCodeIsNot(expected, ErpAfos.A_19514);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsNot(
      int expected, RequirementsSet req) {
    Predicate<ErpResponse<? extends Resource>> predicate = r -> r.getStatusCode() != expected;
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            req, format("ReturnCode darf nicht {0} entsprechen", expected));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeBetween(
      int lower, int upper) {
    return returnCodeIsBetween(lower, upper);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsBetween(
      int lower, int upper) {
    return returnCodeIsBetween(lower, upper, ErpAfos.A_19514);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsBetween(
      int lower, int upper, RequirementsSet req) {
    val list = IntStream.rangeClosed(lower, upper).boxed().toList();
    return returnCodeIsIn(list, req);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsIn(
      RequirementsSet req, int... expected) {
    val list = Arrays.stream(expected).boxed().toList();
    return returnCodeIsIn(list, req);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsIn(int... expected) {
    return returnCodeIsIn(ErpAfos.A_19514, expected);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsIn(
      List<Integer> expected) {
    return returnCodeIsIn(expected, ErpAfos.A_19514);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> returnCodeIsIn(
      List<Integer> expected, RequirementsSet req) {
    String codes;

    if (expected.isEmpty()) {
      throw new InvalidArgumentException("At least one expected Return Code is required");
    } else if (expected.size() > 10) {
      codes = format("[{0} .. {1}]", expected.get(0), expected.get(expected.size() - 1));
    } else {
      codes = expected.stream().map(String::valueOf).collect(Collectors.joining(" | "));
    }

    Predicate<ErpResponse<? extends Resource>> predicate =
        r -> expected.stream().anyMatch(e -> r.getStatusCode() == e);
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            req.getRequirement(), format("ReturnCode muss {0} entsprechen", codes));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErpResponse<? extends Resource>> headerContentContains(
      String header, String expected, RequirementsSet req) {
    Predicate<ErpResponse<? extends Resource>> predicate =
        r -> r.getHeaderValue(header).contains(expected);
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            req.getRequirement(),
            format(
                "Der Header Content für {0} entsprach nicht dem erwarteten {1}", header, expected));
    return step.predicate(predicate).accept();
  }

  public static <R extends Resource>
      VerificationStep<ErpResponse<? extends Resource>> payloadIsOfType(
          Class<R> type, RequirementsSet req) {
    return payloadIsOfType(type, req.getRequirement());
  }

  public static <R extends Resource>
      VerificationStep<ErpResponse<? extends Resource>> payloadIsOfType(
          Class<R> type, Requirement req) {
    val predicate = resourceTypePredicate(type);
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            req, format("Payload muss vom Type {0} sein", type.getSimpleName()));
    return step.predicate(predicate).accept();
  }

  public static <R extends Resource>
      VerificationStep<ErpResponse<? extends Resource>> payloadIsNotOfType(
          Class<R> type, RequirementsSet req) {
    return payloadIsNotOfType(type, req.getRequirement());
  }

  public static <R extends Resource>
      VerificationStep<ErpResponse<? extends Resource>> payloadIsNotOfType(
          Class<R> type, Requirement req) {
    val predicate = resourceTypePredicate(type).negate();
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            req, format("Payload darf nicht vom Type {0} sein", type.getSimpleName()));
    return step.predicate(predicate).accept();
  }

  private static <R extends Resource>
      Predicate<ErpResponse<? extends Resource>> resourceTypePredicate(Class<R> type) {
    return r -> r.isResourceOfType(type);
  }

  public static VerificationStep<ErpResponse<? extends Resource>> warningHeaderIsUnset() {
    Predicate<ErpResponse<? extends Resource>> predicate =
        r -> {
          String headerValue = r.getHeaderValue("Warning");
          return headerValue == null || headerValue.isEmpty();
        };
    val step =
        new VerificationStep.StepBuilder<ErpResponse<? extends Resource>>(
            Requirement.custom("Unspecified Requirement"),
            "Der Warning-Header darf beim Löschen einer bereits abgerufenen Communication nicht"
                + " gesetzt sein!");
    return step.predicate(predicate).accept();
  }
}
