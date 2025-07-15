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

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.val;

public class AcceptedPrescriptionStrategy extends PharmacyPrescriptionStrategy {

  private ErxAcceptBundle acceptBundle;

  private AcceptedPrescriptionStrategy(
      DequeStrategy deque, @Nullable AccessCode customAccessCode, @Nullable Secret customSecret) {
    super(deque, customAccessCode, customSecret);
  }

  @Override
  public void init(ManagePharmacyPrescriptions prescriptions) {
    this.acceptBundle = deque.chooseFrom(prescriptions.getAcceptedPrescriptions());
  }

  @Override
  public TaskId getTaskId() {
    this.ensureInitialized(this.acceptBundle);
    return this.acceptBundle.getTaskId();
  }

  @Override
  public PrescriptionId getPrescriptionId() {
    this.ensureInitialized(this.acceptBundle);
    return this.acceptBundle.getTask().getPrescriptionId();
  }

  @Override
  public AccessCode getAccessCode() {
    this.ensureInitialized(this.acceptBundle);
    return Optional.ofNullable(customAccessCode)
        .orElse(this.acceptBundle.getTask().getAccessCode());
  }

  @Override
  public Secret getSecret() {
    this.ensureInitialized(this.acceptBundle);
    return Optional.ofNullable(customSecret)
        .orElse(
            this.acceptBundle
                .getTask()
                .getSecret()
                .orElseThrow(() -> new MissingFieldException(ErxAcceptBundle.class, "Secret")));
  }

  @Override
  public KVNR getReceiverKvnr() {
    this.ensureInitialized(this.acceptBundle);
    return this.acceptBundle
        .getTask()
        .getForKvnr()
        .orElseThrow(() -> new MissingFieldException(ErxAcceptBundle.class, "KVNR"));
  }

  public static class ConcreteBuilder<T> extends PharmacyPrescriptionStrategy.Builder<T> {
    public ConcreteBuilder(
        DequeStrategy deque, Function<PharmacyPrescriptionStrategy, T> finalBuilderProvider) {
      super(deque, finalBuilderProvider);
    }

    public T and() {
      val strategy = new AcceptedPrescriptionStrategy(deque, accessCode, secret);
      return finalBuilderProvider.apply(strategy);
    }
  }
}
