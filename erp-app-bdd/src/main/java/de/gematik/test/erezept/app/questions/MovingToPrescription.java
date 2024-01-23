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

package de.gematik.test.erezept.app.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class MovingToPrescription implements Question<Optional<ErxPrescriptionBundle>> {

  private final TaskId taskId;

  @Override
  @Step("{0} navigiert zu dem E-Rezept #taskId")
  public Optional<ErxPrescriptionBundle> answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    app.logEvent(format("Find the prescription {0}", taskId.getValue()));

    val fdPrescription = this.find(erpClient);
    fdPrescription.ifPresentOrElse(
        p -> proceedWithBackendPrescription(p, app), () -> checkPrescriptionNotShown(app));
    return fdPrescription;
  }

  private void checkPrescriptionNotShown(UseIOSApp app) {
    log.info(
        format(
            "Prescription {0} was not found in backend: ensure its not shown in the app",
            taskId.getValue()));
    /*
     How many prescriptions should be checked to ensure the prescription is not shown anymore
     Note: this operation is costly and can take up to several minutes,
     especially when the App-DOM contains many prescriptions.
     To save on execution time, we will check only the first two prescriptions!!
    */
    val amountToCheck = 2;
    val prescriptionElement = PrescriptionsViewElement.withoutName();
    for (var i = 0; i < amountToCheck; i++) {
      val listPageElement = ListPageElement.forElement(prescriptionElement, i);
      if (find(app, listPageElement)) {
        throw new AppStateMissmatchException(
            format(
                "Prescription with ID {0} (profile {1}) was found in App but is not available"
                    + " (anymore) in Backend",
                taskId.getValue(), app.getCurrentUserProfile()));
      }
    }
  }

  private void proceedWithBackendPrescription(ErxPrescriptionBundle fdPrescription, UseIOSApp app) {
    log.info(
        format(
            "Prescription {0} was found in backend: ensure its shown in the app",
            taskId.getValue()));
    val isMvo = fdPrescription.getKbvBundle().map(KbvErpBundle::isMultiple).orElse(false);
    log.info(
        format(
            "Found {0}Prescription {1} in the backend",
            isMvo ? "MVO-" : "",
            fdPrescription.getKbvBundle().map(KbvErpBundle::getMedicationName)));

    val medication =
        fdPrescription
            .getKbvBundle()
            .map(KbvErpBundle::getMedication)
            .orElseThrow(
                () ->
                    new MissingFieldException(
                        fdPrescription.getClass(), KbvItaErpStructDef.BUNDLE));
    val prescriptionElement = PrescriptionsViewElement.named(medication.getMedicationName());
    val webElements = app.getWebElementListLen(prescriptionElement);

    var foundInApp = false;
    for (var i = 0; i < webElements; i++) {
      val listPageElement = ListPageElement.forElement(prescriptionElement, i);
      foundInApp = find(app, listPageElement);
      if (foundInApp) {
        break;
      }
    }

    if (!foundInApp) {
      throw new AppStateMissmatchException(
          format(
              "Prescription with ID {0} (profile {1}) not found in App",
              taskId.getValue(), app.getCurrentUserProfile()));
    }
  }

  private boolean find(UseTheApp<?> app, ListPageElement listPageElement) {
    app.tap(listPageElement);
    app.scrollIntoView(ScrollDirection.DOWN, PrescriptionDetails.TECHNICAL_INFORMATION);
    app.tap(PrescriptionDetails.TECHNICAL_INFORMATION);
    val currentTaskId = app.getText(PrescriptionTechnicalInformation.TASKID);
    app.tap(PrescriptionTechnicalInformation.BACK);

    val found = currentTaskId.equals(taskId.getValue());

    if (!found) app.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    return found;
  }

  private Optional<ErxPrescriptionBundle> find(UseTheErpClient erpClient) {
    val cmd = new TaskGetByIdCommand(taskId);
    val response = erpClient.request(cmd);
    return response.getResourceOptional();
  }

  public static MovingToPrescription withTaskId(String taskId) {
    return withTaskId(TaskId.from(taskId));
  }

  public static MovingToPrescription withTaskId(TaskId taskId) {
    return Instrumented.instanceOf(MovingToPrescription.class).withProperties(taskId);
  }
}
