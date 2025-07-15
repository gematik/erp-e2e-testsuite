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

package de.gematik.test.erezept.actions.rawhttpactions;

import de.gematik.test.core.expectations.*;
import de.gematik.test.core.expectations.verifier.*;
import kong.unirest.core.HttpResponse;
import lombok.*;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.*;
import net.serenitybdd.screenplay.*;

@AllArgsConstructor
public class VerifyRawHttp<T> implements Performable {

  private final HttpResponseExpectation<T> expectation;

  public static <R> Builder<R> that(HttpResponse<R> interaction, Class<R> payloadType) {
    return new Builder<>(HttpResponseExpectation.expectFor(interaction, payloadType));
  }

  @Override
  @Step("{0} verifiziert #expectation")
  public <A extends Actor> void performAs(A t) {
    expectation.ensure();
  }

  public static class Builder<R> {
    private final HttpResponseExpectation<R> expectation;

    private Builder(HttpResponseExpectation<R> expectation) {
      this.expectation = expectation;
    }

    public Builder<R> responseWith(VerificationStep<HttpResponse<?>> step) {
      this.expectation.responseWith(step);
      return this;
    }

    public Builder<R> hasResponseWith(VerificationStep<HttpResponse<?>> step) {
      return responseWith(step);
    }

    public Builder<R> andHttp(VerificationStep<HttpResponse<?>> step) {
      return responseWith(step);
    }

    public Builder<R> has(VerificationStep<R> step) {
      this.expectation.payloadHas(step);
      return this;
    }

    public Builder<R> and(VerificationStep<R> step) {
      this.expectation.payloadIs(step);
      return this;
    }

    public Builder<R> is(VerificationStep<R> step) {
      this.expectation.andPayload(step);
      return this;
    }

    @SuppressWarnings({"unchecked"})
    public <V> VerifyRawHttp<V> isCorrect() {
      Object[] params = {expectation};
      val kl = (Class<VerifyRawHttp<V>>) (Object) VerifyRawHttp.class;
      return new Instrumented.InstrumentedBuilder<>(kl, params).newInstance();
    }
  }
}
