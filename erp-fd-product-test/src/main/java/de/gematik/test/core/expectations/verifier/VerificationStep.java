/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.thucydides.core.annotations.Step;
import org.assertj.core.api.AbstractPredicateAssert;
import org.assertj.core.api.PredicateAssert;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class VerificationStep<T> {
  @Getter private final Predicate<T> predicate;
  @Getter private final Requirement requirement;
  @Getter private final String expectation;
  private final BiFunction<PredicateAssert<T>, T, PredicateAssert<T>> func;

  private Supplier<String> failMessageSupplier(T actual) {
    return () -> format("{1} hat die Erwartung ''{0}'' nicht erfüllt", expectation, actual);
  }

  @Step("Verifiziere Anforderung #requirement: #expectation für {0}")
  public void apply(T actual) {
    // check if the predicate passes
    val passed = predicate.test(actual);
    CoverageReporter.getInstance().add(requirement, passed);
    assertThat(predicate)
        .describedAs(expectation)
        .overridingErrorMessage(this.failMessageSupplier(actual))
        .accepts(actual);
  }

  @Override
  public String toString() {
    return format("muss {0}", expectation);
  }

  static class StepBuilder<U> {
    private final Requirement requirement;
    private final String expectation;
    private Predicate<U> predicate;

    StepBuilder(RequirementsSet requirement, String expectation) {
      this(requirement.getRequirement(), expectation);
    }

    StepBuilder(Requirement requirement, String expectation) {
      this.requirement = requirement;
      this.expectation = expectation;
    }

    public StepBuilder<U> predicate(Predicate<U> predicate) {
      this.predicate = predicate;
      return this;
    }

    @SuppressWarnings("unchecked")
    public VerificationStep<U> accept() {
      val kl = (Class<VerificationStep<U>>) (Object) VerificationStep.class;
      BiFunction<PredicateAssert<U>, U, PredicateAssert<U>> func = AbstractPredicateAssert::accepts;
      Object[] params = {predicate, requirement, expectation, func};
      return new Instrumented.InstrumentedBuilder<>(kl, params).newInstance();
    }
  }
}
