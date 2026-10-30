/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.erezept.remotefdv.api.model.Prescription;
import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

/**
 * Take the last Data Matrix Code received from a Doctor and try to fetch this prescription from the
 * FD
 */
@Slf4j
public class TheLastPrescription implements Question<Boolean> {

  private final DmcStack stack;

  private TheLastPrescription(DmcStack stack) {
    this.stack = stack;
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val dataMatrixCodes = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcStack = dataMatrixCodes.chooseStack(stack);

    val lastDmc = dmcStack.getLast();

    val response =
        client.sendRequest(PatientRequests.getPrescriptionById(lastDmc.getTaskId().getValue()));
    return response
        .getResourceOptional()
        .map(prescription -> checkPrescription(actor, prescription, lastDmc))
        .orElse(false);
  }

  private boolean checkPrescription(Actor actor, Prescription prescription, DmcPrescription dmc) {
    // 1. check the prescription ID is okay
    val answer = new AtomicBoolean(checkTaskId(prescription, dmc));

    // 2. check the access code
    answer.compareAndSet(true, checkAccessCode(prescription, dmc));

    // 3. check if the KVNR is okay only if this is not a representative DMC
    if (!dmc.isRepresentative()) {
      answer.compareAndSet(true, checkPatientKvid(actor, prescription));
    }

    return answer.get();
  }

  /**
   * Check if the received prescription does have the expected TaskId
   *
   * @param prescription is the received Prescription from FD
   * @param dmc is the received DMC from the Doctor
   * @return true if prescription and DMC have the same Prescription-ID false otherwise
   */
  private boolean checkTaskId(Prescription prescription, DmcPrescription dmc) {
    return prescription.getPrescriptionId().equals(dmc.getTaskId().getValue());
  }

  /**
   * Check if the received prescription does have the expected Access Code. If the prescription does
   * not have an Access Code this might be due tu direct assignment, in this case no check will be
   * performed
   *
   * @param prescription is the received Prescription from FD
   * @param dmc is the received DMC from the Doctor
   * @return true if prescription and DMC have the same AccessCode or this is direct assignment
   *     false otherwise
   */
  private boolean checkAccessCode(Prescription prescription, DmcPrescription dmc) {
    val flowType = prescription.getWorkFlow().getValue();
    boolean ret;
    if (flowType.equals("169") || flowType.equals("209")) {
      // if direct assignment, make sure the prescription does NOT contain the access code for the
      // patient!
      // see A_21360
      ret = prescription.getAccessCode() == null;
    } else {
      // check the AccessCode only if this is not a direct assignment
      ret = prescription.getAccessCode().equals(dmc.getAccessCode().getValue());
    }
    return ret;
  }

  /**
   * If the received prescription does have PKV KVID check also against the given KVID of the actor
   *
   * @param actor is the patient who receives the prescription
   * @param prescription is the received Prescription-Bundle from FD
   * @return false if the prescription has a KVID which does not match the KVID of the actor, true
   *     otherwise
   */
  private boolean checkPatientKvid(Actor actor, Prescription prescription) {
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val expectedKviId = baseData.getKvnr();

    return prescription.getPatient().getKvnr().equals(expectedKviId.getValue());
  }

  public static Builder from(String stack) {
    return from(DmcStack.fromString(stack));
  }

  public static Builder from(DmcStack stack) {
    return new Builder(stack);
  }

  public static Builder prescribed() {
    return from(DmcStack.ACTIVE);
  }

  public static Builder deleted() {
    return from(DmcStack.DELETED);
  }

  public static class Builder {

    private final DmcStack stack;

    private Builder(DmcStack stack) {
      this.stack = stack;
    }

    public TheLastPrescription existsInBackend() {
      return new TheLastPrescription(stack);
    }
  }
}
