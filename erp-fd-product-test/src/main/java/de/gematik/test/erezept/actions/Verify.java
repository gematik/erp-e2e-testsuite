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

package de.gematik.test.erezept.actions;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;

import de.gematik.test.core.expectations.*;
import de.gematik.test.core.expectations.requirements.*;
import de.gematik.test.core.expectations.verifier.*;
import de.gematik.test.erezept.*;
import de.gematik.test.erezept.client.rest.*;
import lombok.*;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.*;
import net.serenitybdd.screenplay.*;
import org.hl7.fhir.r4.model.*;

@AllArgsConstructor
public class Verify<T extends Resource> implements Performable {

  private final ErpResponseExpectation<T> expectation;

  @Override
  @Step("{0} verifiziert #expectation")
  public <A extends Actor> void performAs(A t) {
    expectation.ensure();
  }

  public static <R extends Resource> PreBuilder<R> that(ErpInteraction<R> interaction) {
    return new PreBuilder<>(interaction);
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class PreBuilder<R extends Resource> {
    private final ErpInteraction<R> interaction;

    public Builder<R> withExpectedType() {
      return new Builder<>(interaction.expectation());
    }

    public Builder<R> withExpectedType(RequirementsSet req) {
      return withExpectedType(req.getRequirement());
    }

    public Builder<R> withExpectedType(Requirement req) {
      val exp = withExpectedType();
      exp.andResponse(payloadIsOfType(interaction.getExpectedType(), req));
      return exp;
    }

    public Builder<Resource> withIndefiniteType() {
      return new Builder<>(interaction.asIndefiniteResource());
    }

    public Builder<OperationOutcome> withOperationOutcome(RequirementsSet req) {
      return withOperationOutcome(req.getRequirement());
    }

    public Builder<OperationOutcome> withOperationOutcome(Requirement req) {
      val exp = withOperationOutcome();
      exp.andResponse(payloadIsOfType(OperationOutcome.class, req));
      return exp;
    }

    public Builder<OperationOutcome> withOperationOutcome() {
      return new Builder<>(interaction.asOperationOutcome());
    }
  }

  public static class Builder<R extends Resource> {
    private final ErpResponseExpectation<R> expectation;

    private Builder(ErpResponseExpectation<R> expectation) {
      this.expectation = expectation;
    }

    public Builder<R> responseWith(VerificationStep<ErpResponse<? extends Resource>> step) {
      this.expectation.responseWith(step);
      return this;
    }

    public Builder<R> hasResponseWith(VerificationStep<ErpResponse<? extends Resource>> step) {
      return responseWith(step);
    }

    public Builder<R> andResponse(VerificationStep<ErpResponse<? extends Resource>> step) {
      return responseWith(step);
    }

    public Builder<R> has(VerificationStep<R> step) {
      this.expectation.has(step);
      return this;
    }

    public Builder<R> and(VerificationStep<R> step) {
      this.expectation.and(step);
      return this;
    }

    public Builder<R> is(VerificationStep<R> step) {
      this.expectation.is(step);
      return this;
    }

    @SuppressWarnings({"unchecked"})
    public <V extends Resource> Verify<V> isCorrect() {
      Object[] params = {expectation};
      val kl = (Class<Verify<V>>) (Object) Verify.class;
      return new Instrumented.InstrumentedBuilder<>(kl, params).newInstance();
    }
  }
}
