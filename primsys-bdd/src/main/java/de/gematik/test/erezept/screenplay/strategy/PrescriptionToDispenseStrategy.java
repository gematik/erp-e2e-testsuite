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

package de.gematik.test.erezept.screenplay.strategy;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class PrescriptionToDispenseStrategy {

  private TaskId taskId;
  private KVNR kvnr;
  private Actor patient;
  private Secret secret;
  private AccessCode accessCode;
  private PrescriptionId prescriptionId;
  private ManagePharmacyPrescriptions prescriptionManager;
  private ErxAcceptBundle taskToDispense;
  private DequeStrategy dequeue;

  private PrescriptionToDispenseStrategy() {
    // use the builder instead
  }

  public static Builder withDequeue(DequeStrategy dequeue) {
    return new Builder(dequeue);
  }

  public boolean hasPatient() {
    return patient != null;
  }

  public Optional<Actor> getPatient() {
    return Optional.ofNullable(patient);
  }

  public boolean hasConsent() {
    return taskToDispense.hasConsent();
  }

  public TaskId getTaskId() {
    if (this.taskId == null) {
      this.taskId = taskToDispense.getTaskId();
    }
    return this.taskId;
  }

  public PrescriptionId getPrescriptionId() {
    if (this.prescriptionId == null) {
      this.prescriptionId = taskToDispense.getTask().getPrescriptionId();
    }
    return this.prescriptionId;
  }

  public KVNR getKvnr() {
    if (this.kvnr == null) {
      this.kvnr =
          taskToDispense
              .getTask()
              .getForKvnr()
              .orElseThrow(
                  () -> new MissingFieldException(ErxTask.class, DeBasisProfilNamingSystem.KVID));
    }
    return this.kvnr;
  }

  public Secret getSecret() {
    if (this.secret == null) {
      this.secret =
          taskToDispense
              .getTask()
              .getSecret()
              .orElseThrow(
                  () -> new MissingFieldException(ErxTask.class, ErpWorkflowNamingSystem.SECRET));
    }
    return this.secret;
  }

  public AccessCode getAccessCode() {
    if (this.accessCode == null) {
      this.accessCode = taskToDispense.getTask().getAccessCode();
    }
    return this.accessCode;
  }

  public String getKbvBundleId() {
    return taskToDispense.getKbvBundleId();
  }

  public String getKbvBundleAsString() {
    return LocalVerifier.parse(taskToDispense.getSignedKbvBundle()).getDocument();
  }

  /**
   * Finally teardown the strategy by consuming the used the acceptedPrescription. This method needs
   * to be called by intention after a successful operation.
   *
   * <p>By this the strategy remains idempotent on failures and consumes the prescription only after
   * successful dispensation
   */
  public void teardown() {
    val p = this.dequeue.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    prescriptionManager.getAcceptedList().remove(p);
  }

  public static class Builder {
    private final PrescriptionToDispenseStrategy strategy;

    public Builder(DequeStrategy dequeue) {
      this.strategy = new PrescriptionToDispenseStrategy();
      this.strategy.dequeue = dequeue;
    }

    public Builder taskId(String taskId) {
      return taskId(TaskId.from(taskId));
    }

    public Builder taskId(TaskId taskId) {
      this.strategy.taskId = taskId;
      return this;
    }

    public Builder kvnr(KVNR kvnr) {
      this.strategy.kvnr = kvnr;
      return this;
    }

    public Builder patient(Actor patient) {
      val patientData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
      this.strategy.kvnr = patientData.getKvnr();
      this.strategy.patient = patient;
      return this;
    }

    public Builder secret(String secret) {
      return this.secret(new Secret(secret));
    }

    public Builder secret(Secret secret) {
      this.strategy.secret = secret;
      return this;
    }

    public PrescriptionToDispenseStrategy initialize(
        ManagePharmacyPrescriptions prescriptionManager) {
      this.strategy.prescriptionManager = prescriptionManager;
      this.strategy.taskToDispense = getPrescriptionToDispense(prescriptionManager);
      return this.strategy;
    }

    private ErxAcceptBundle getPrescriptionToDispense(
        ManagePharmacyPrescriptions prescriptionManager) {
      return this.strategy.dequeue.chooseFrom(prescriptionManager.getAcceptedPrescriptions());
    }
  }
}
