/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
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
    val erpClientAbility = actor.abilityTo(UseTheErpClient.class);
    val dataMatrixCodes = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcStack = dataMatrixCodes.chooseStack(stack);

    val lastDmc = dmcStack.getLast();
    TaskGetByIdCommand cmd;
    if (lastDmc.isRepresentative()) {
      log.info(
          format(
              "{0} fetches the Prescription {1} as representative with AccessCode {2}",
              actor.getName(), lastDmc.getTaskId(), lastDmc.getAccessCode().getValue()));
      cmd = new TaskGetByIdCommand(lastDmc.getTaskId(), lastDmc.getAccessCode());
    } else {
      log.info(
          format(
              "{0} fetches the Prescription {1} as owner", actor.getName(), lastDmc.getTaskId()));
      cmd = new TaskGetByIdCommand(lastDmc.getTaskId());
    }

    val response = erpClientAbility.request(cmd);

    // be default assume false; if response does not contain the expected body no further checks
    // needed, simply return false
    val answer = new AtomicBoolean(false);

    response
        .getResourceOptional()
        .ifPresent(prescription -> answer.set(checkPrescription(actor, prescription, lastDmc)));

    return answer.get();
  }

  private boolean checkPrescription(
      Actor actor, ErxPrescriptionBundle prescriptionBundle, DmcPrescription dmc) {
    // 1. check the prescription ID is okay
    val answer = new AtomicBoolean(checkTaskId(prescriptionBundle, dmc));

    // 2. check the access code
    answer.compareAndSet(true, checkAccessCode(prescriptionBundle, dmc));

    // 3. check if the KVNR is okay only if this is not a representative DMC
    if (!dmc.isRepresentative())
      answer.compareAndSet(true, checkPatientId(actor, prescriptionBundle));

    return answer.get();
  }

  /**
   * Check if the received prescription does have the expected TaskId
   *
   * @param prescriptionBundle is the received Prescription-Bundle from FD
   * @param dmc is the received DMC from the Doctor
   * @return true if prescription and DMC have the same Prescription-ID false otherwise
   */
  private boolean checkTaskId(ErxPrescriptionBundle prescriptionBundle, DmcPrescription dmc) {
    return prescriptionBundle.getTask().getTaskId().equals(dmc.getTaskId());
  }

  /**
   * Check if the received prescription does have the expected Access Code. If the prescription does
   * not have an Access Code this might be due tu direct assignment, in this case no check will be
   * performed
   *
   * @param prescriptionBundle is the received Prescription-Bundle from FD
   * @param dmc is the received DMC from the Doctor
   * @return true if prescription and DMC have the same AccessCode or this is direct assignment
   *     false otherwise
   */
  private boolean checkAccessCode(ErxPrescriptionBundle prescriptionBundle, DmcPrescription dmc) {
    val flowType = prescriptionBundle.getTask().getFlowType();
    boolean ret;
    if (flowType.isDirectAssignment()) {
      // if direct assignment, make sure the prescription does NOT contain the access code for the
      // patient!
      // see A_21360
      ret = !prescriptionBundle.getTask().hasAccessCode();
    } else {
      // check the AccessCode only if this is not a direct assignment
      ret = prescriptionBundle.getTask().getAccessCode().equals(dmc.getAccessCode());
    }
    return ret;
  }

  /**
   * If the received prescription does have PKV KVID check also against the given KVID of the actor
   *
   * @param actor is the patient who receives the prescription
   * @param prescriptionBundle is the received Prescription-Bundle from FD
   * @return false if the prescription has a KVID which does not match the KVID of the actor, true
   *     otherwise
   */
  private boolean checkPatientId(Actor actor, ErxPrescriptionBundle prescriptionBundle) {
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val expectedKviId = baseData.getKvnr();

    // Assume true by default because prescriptionBundle does not necessarily have a KVID e.g. for
    // PKV
    val kvidCheck = new AtomicBoolean(true);
    prescriptionBundle
        .getKbvBundle()
        .flatMap(kbvErpBundle -> kbvErpBundle.getPatient().getGkvId())
        .ifPresent(kvid -> kvidCheck.set(expectedKviId.equals(kvid)));
    return kvidCheck.get();
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
