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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.DequeableAbilityStrategy;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.val;

public class AuthorizedChargeItemStrategy
    extends DequeableAbilityStrategy<ManagePharmacyPrescriptions> {

  @Nullable protected final AccessCode customAccessCode;

  private ChargeItemChangeAuthorization authorization;

  private AuthorizedChargeItemStrategy(DequeStrategy deque, @Nullable AccessCode customAccessCode) {
    super(ManagePharmacyPrescriptions.class, deque);
    this.customAccessCode = customAccessCode;
  }

  @Override
  public void init(ManagePharmacyPrescriptions prescriptions) {
    this.authorization = deque.chooseFrom(prescriptions.getChargeItemChangeAuthorizations());
  }

  public PrescriptionId getPrescriptionId() {
    this.ensureInitialized(this.authorization);
    return this.authorization.getPrescriptionId();
  }

  public AccessCode getAccessCode() {
    this.ensureInitialized(this.authorization);
    return Optional.ofNullable(customAccessCode).orElse(this.authorization.getAccessCode());
  }

  public ErxChargeItem getChargeItem() {
    this.ensureInitialized(this.authorization);
    return this.authorization.getChargeItem();
  }

  public static class ConcreteBuilder<T> {
    private final DequeStrategy deque;
    private final Function<AuthorizedChargeItemStrategy, T> finalBuilderProvider;

    public ConcreteBuilder(
        DequeStrategy deque, Function<AuthorizedChargeItemStrategy, T> finalBuilderProvider) {
      this.deque = deque;
      this.finalBuilderProvider = finalBuilderProvider;
    }

    protected AccessCode accessCode;

    public ConcreteBuilder<T> withRandomAccessCode() {
      return withCustomAccessCode(AccessCode.random());
    }

    public ConcreteBuilder<T> withCustomAccessCode(String accessCode) {
      return withCustomAccessCode(AccessCode.fromString(accessCode));
    }

    public ConcreteBuilder<T> withCustomAccessCode(AccessCode accessCode) {
      this.accessCode = accessCode;
      return this;
    }

    public T and() {
      val strategy = new AuthorizedChargeItemStrategy(deque, accessCode);
      return finalBuilderProvider.apply(strategy);
    }
  }
}
