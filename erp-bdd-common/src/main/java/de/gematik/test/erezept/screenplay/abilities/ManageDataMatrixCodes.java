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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.exceptions.MissingStackException;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.HasTeardown;

/**
 * This ability is intended to be used by the Patient-Actor for managing his
 * (Low-Detail)-Prescriptions which he receives from the doctor
 */
@Slf4j
public class ManageDataMatrixCodes implements Ability, HasTeardown {

  @Getter private final ManagedList<DmcPrescription> dmcs;
  @Getter private final ManagedList<DmcPrescription> deletedDmcs;

  private ManageDataMatrixCodes() {
    this.dmcs =
        new ManagedList<>(
            new MissingPreconditionError("No Prescriptions via DataMatrixCode received so far"));
    this.deletedDmcs =
        new ManagedList<>(new MissingPreconditionError("No DataMatrixCode were deleted so far"));
  }

  public ManagedList<DmcPrescription> chooseStack(DmcStack stack) {
    ManagedList<DmcPrescription> ret;
    if (stack.equals(DmcStack.ACTIVE)) {
      ret = dmcs;
    } else if (stack.equals(DmcStack.DELETED)) {
      ret = deletedDmcs;
    } else {
      throw new MissingStackException(
          this.getClass(), stack.getClass()); // should not happen anyways!
    }
    return ret;
  }

  public void appendDmc(DmcPrescription dmc) {
    log.info(
        format(
            "Patient has received a 'paper-based' Prescription on a Data Matrix Code for Task {0}",
            dmc.getTaskId()));
    this.dmcs.append(dmc);
  }

  public DmcPrescription getLastDmc() {
    return this.dmcs.getLast();
  }

  public DmcPrescription consumeLastDmc() {
    return this.dmcs.consumeLast();
  }

  public DmcPrescription getFirstDmc() {
    return this.dmcs.getFirst();
  }

  public DmcPrescription consumeFirstDmc() {
    return this.dmcs.consumeFirst();
  }

  public List<DmcPrescription> getDmcList() {
    return this.dmcs.getRawList();
  }

  @Override
  public String toString() {
    return "DataMatrixCode Manager für die Verwaltung von verschriebenen E-Rezepten auf Papier";
  }

  @Override
  public void tearDown() {
    /* TODO:  // NOSONAR still needs to be refactored
    implement TearDown where this Patient iterates over all of his DMCs (unassigned
    Prescriptions) and deletes/aborts these */
    log.info("Teardown not implemented yet: Abort all unassigned (pending) Prescriptions");
  }

  public static ManageDataMatrixCodes heGetsPrescribed() {
    return new ManageDataMatrixCodes();
  }

  public static ManageDataMatrixCodes sheGetsPrescribed() {
    // gender-sensitive Java :)
    return heGetsPrescribed();
  }
}
