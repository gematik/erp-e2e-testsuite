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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.DequeableAbilityStrategy;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public abstract class PharmacyPrescriptionStrategy
    extends DequeableAbilityStrategy<ManagePharmacyPrescriptions> {

  @Nullable protected final AccessCode customAccessCode;
  @Nullable protected final Secret customSecret;

  protected PharmacyPrescriptionStrategy(
      DequeStrategy deque, @Nullable AccessCode customAccessCode, @Nullable Secret customSecret) {
    super(ManagePharmacyPrescriptions.class, deque);
    this.customSecret = customSecret;
    this.customAccessCode = customAccessCode;
  }

  public abstract TaskId getTaskId();

  public abstract PrescriptionId getPrescriptionId();

  public abstract AccessCode getAccessCode();

  public abstract Secret getSecret();

  public abstract KVNR getReceiverKvnr();

  @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
  public abstract static class Builder<T> {

    protected final DequeStrategy deque;
    protected final Function<PharmacyPrescriptionStrategy, T> finalBuilderProvider;

    protected AccessCode accessCode;
    protected Secret secret;

    public Builder<T> withCustomSecret(String secret) {
      return withCustomSecret(Secret.from(secret));
    }

    public Builder<T> withCustomSecret(Secret secret) {
      this.secret = secret;
      return this;
    }

    public Builder<T> withRandomAccessCode() {
      return withCustomAccessCode(AccessCode.random());
    }

    public Builder<T> withCustomAccessCode(String accessCode) {
      return withCustomAccessCode(AccessCode.from(accessCode));
    }

    public Builder<T> withCustomAccessCode(AccessCode accessCode) {
      this.accessCode = accessCode;
      return this;
    }

    public abstract T and();
  }
}
