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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.questions.ResponseOfRejectOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RejectPrescription implements Task {

  private final ResponseOfRejectOperation theResponseOfReject;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val response = actor.asksFor(theResponseOfReject);
    then(Ensure.that(response.getStatusCode()).isEqualTo(204));
  }

  public static Builder withInvalidSecret(String secret) {
    return new Builder(secret);
  }

  public static RejectPrescription fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static RejectPrescription fromStack(DequeStrategy deque) {
    return new RejectPrescription(ResponseOfRejectOperation.fromStack(deque));
  }

  public static class Builder {
    private final String secreteReplacement;

    private Builder(String secret) {
      this.secreteReplacement = secret;
    }

    public RejectPrescription fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public RejectPrescription fromStack(DequeStrategy deque) {
      return new RejectPrescription(
          ResponseOfRejectOperation.withInvalidSecret(this.secreteReplacement).fromStack(deque));
    }
  }
}
