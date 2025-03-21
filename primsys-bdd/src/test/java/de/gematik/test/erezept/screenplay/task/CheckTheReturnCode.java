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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.screenplay.questions.FhirResponseQuestion;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.NamedPredicate;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class CheckTheReturnCode implements Task {

  private final FhirResponseQuestion<?> fhirResponseQuestion;
  private final Predicate<Integer> predicate;

  protected CheckTheReturnCode(FhirResponseQuestion<?> question, Predicate<Integer> predicate) {
    this.fhirResponseQuestion = question;
    this.predicate = predicate;
  }

  @Override
  @Step("{0} prüft, ob der ReturnCode von #fhirResponseQuestion dem Wert #predicate entspricht")
  public <T extends Actor> void performAs(T actor) {
    val response = actor.asksFor(fhirResponseQuestion);
    val returnCode = response.getStatusCode();
    log.info(format("Received ReturnCode {0}", returnCode));
    actor.attemptsTo(Ensure.that(predicate.test(returnCode)).isTrue());
  }

  public static Builder of(FhirResponseQuestion<? extends Resource> fhirResponseQuestion) {
    return new Builder(fhirResponseQuestion);
  }

  public static class Builder {
    private final FhirResponseQuestion<?> fhirResponseQuestion;

    private Builder(FhirResponseQuestion<?> fhirResponseQuestion) {
      this.fhirResponseQuestion = fhirResponseQuestion;
    }

    public CheckTheReturnCode isEqualTo(int returnCode) {
      Predicate<Integer> predicate = rc -> rc == returnCode;
      val name = format("genau {0}", returnCode);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode isGreaterThan(int returnCode) {
      Predicate<Integer> predicate = rc -> rc > returnCode;
      val name = format("größer {0}", returnCode);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode isGreaterEqual(int returnCode) {
      Predicate<Integer> predicate = rc -> rc >= returnCode;
      val name = format("größer-gleich {0}", returnCode);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode isLowerThan(int returnCode) {
      Predicate<Integer> predicate = rc -> rc < returnCode;
      val name = format("kleiner {0}", returnCode);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode isLowerEqual(int returnCode) {
      Predicate<Integer> predicate = rc -> rc <= returnCode;
      val name = format("kleiner-gleich {0}", returnCode);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode isInBetween(int min, int max) {
      Predicate<Integer> predicate = rc -> rc >= min && rc <= max;
      val name = format("größer-gleich {0} und kleiner-gleich {1}", min, max);
      return matching(new NamedPredicate<>(name, predicate));
    }

    public CheckTheReturnCode matching(Predicate<Integer> predicate) {
      return Instrumented.instanceOf(CheckTheReturnCode.class)
          .withProperties(fhirResponseQuestion, predicate);
    }
  }
}
