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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import java.util.function.*;
import javax.annotation.*;
import lombok.*;

public class DispensedPrescriptionStrategy extends PharmacyPrescriptionStrategy {

  private DispenseReceipt receipt;

  private DispensedPrescriptionStrategy(
      DequeStrategy deque, @Nullable AccessCode customAccessCode, @Nullable Secret customSecret) {
    super(deque, customAccessCode, customSecret);
  }

  @Override
  public void init(ManagePharmacyPrescriptions prescriptions) {
    this.receipt = deque.chooseFrom(prescriptions.getDispensedPrescriptions());
  }

  @Override
  public TaskId getTaskId() {
    this.ensureInitialized(this.receipt);
    return this.receipt.getTaskId();
  }

  @Override
  public PrescriptionId getPrescriptionId() {
    this.ensureInitialized(this.receipt);
    return this.receipt.getPrescriptionId();
  }

  @Override
  public AccessCode getAccessCode() {
    this.ensureInitialized(this.receipt);
    return Optional.ofNullable(customAccessCode).orElse(this.receipt.getAccessCode());
  }

  @Override
  public Secret getSecret() {
    this.ensureInitialized(this.receipt);
    return Optional.ofNullable(customSecret).orElse(this.receipt.getSecret());
  }

  @Override
  public KVNR getReceiverKvnr() {
    this.ensureInitialized(this.receipt);
    return this.receipt.getReceiverKvnr();
  }

  public static class ConcreteBuilder<T> extends PharmacyPrescriptionStrategy.Builder<T> {
    public ConcreteBuilder(
        DequeStrategy deque, Function<PharmacyPrescriptionStrategy, T> finalBuilderProvider) {
      super(deque, finalBuilderProvider);
    }

    public T and() {
      val strategy = new DispensedPrescriptionStrategy(deque, accessCode, secret);
      return finalBuilderProvider.apply(strategy);
    }
  }
}
