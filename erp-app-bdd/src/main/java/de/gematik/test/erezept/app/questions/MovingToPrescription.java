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

package de.gematik.test.erezept.app.questions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
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
  private boolean isEVDGA;

  @Override
  @Step("{0} navigiert zu dem E-Rezept #taskId")
  public Optional<ErxPrescriptionBundle> answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    app.logEvent(format("Find the prescription {0}", taskId.getValue()));

    isEVDGA = taskId.getFlowType().equals(PrescriptionFlowType.FLOW_TYPE_162);

    val fdPrescription = this.find(erpClient);
    fdPrescription.ifPresentOrElse(
        p -> proceedWithBackendPrescription(p, app), () -> checkPrescriptionNotShown(app));
    return fdPrescription;
  }

  private void checkPrescriptionNotShown(UseIOSApp app) {
    log.info(
        "Prescription {} was not found in backend: ensure its not shown in the app",
        taskId.getValue());
    /*
     How many prescriptions should be checked to ensure the prescription is not shown anymore
     Note: this operation is costly and can take up to several minutes,
     especially when the App-DOM contains many prescriptions.
     To save on execution time, we will check only the first two prescriptions maximum!!
    */
    val prescriptionElement = PrescriptionsViewElement.withoutName();
    val amountToCheck = Math.min(2, app.getWebElementListLen(prescriptionElement));

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
        "Prescription {} was found in backend: ensure its shown in the app", taskId.getValue());

    val prescriptionElement =
        PrescriptionsViewElement.named(findNameOfPrescription(fdPrescription));
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

  private String findNameOfPrescription(ErxPrescriptionBundle fdPrescription) {
    // Since there are no PKV EVDGA, the only Code for them is currently 162
    if (isEVDGA) {
      return fdPrescription
          .getEvdgaBundle()
          .map(KbvEvdgaBundle::getHealthAppRequest)
          .map(KbvHealthAppRequest::getName)
          .orElseThrow(
              () ->
                  new MissingFieldException(
                      fdPrescription.getClass(), KbvItvEvdgaStructDef.HEALTH_APP_REQUEST));

    } else {
      KbvErpMedication medication =
          fdPrescription
              .getKbvBundle()
              .map(KbvErpBundle::getMedication)
              .orElseThrow(
                  () ->
                      new MissingFieldException(
                          fdPrescription.getClass(), KbvItaErpStructDef.BUNDLE));
      return medication.getMedicationName();
    }
  }

  private boolean find(UseTheApp<?> app, ListPageElement listPageElement) {
    app.tap(listPageElement);
    if (isEVDGA) {
      app.waitUntilElementIsVisible(EVDGADetails.DISMISS_FHIR_VZD_DIALOG, 5000);
      if (app.isDisplayed(EVDGADetails.DISMISS_FHIR_VZD_DIALOG)) {
        app.acceptAlert();
      }
    }

    if (app.isDisplayed(EVDGADetails.DIGA_TITLE)) {
      // DIGA-specific flow
      app.tap(EVDGADetails.DIGA_EXTENDED_DETAILS);
      app.swipeIntoView(SwipeDirection.UP, EVDGADetails.TECHNICAL_INFORMATION);
      app.tap(EVDGADetails.TECHNICAL_INFORMATION);

      val currentTaskId = app.getText(EVDGATechnicalInformation.TASKID);
      app.tap(EVDGATechnicalInformation.BACK);

      app.waitUntilElementIsVisible(EVDGADetails.DISMISS_FHIR_VZD_DIALOG, 5000);
      if (app.isDisplayed(EVDGADetails.DISMISS_FHIR_VZD_DIALOG)) {
        app.acceptAlert();
      }

      val found = currentTaskId.equals(taskId.getValue());

      if (!found) {
        app.tap(EVDGADetails.LEAVE_DIGA_DETAILS);
      } else {
        app.swipeIntoView(SwipeDirection.DOWN, EVDGADetails.DIGA_OVERVIEW);
        app.tap(EVDGADetails.DIGA_OVERVIEW);
      }
      return found;
    } else {
      // Regular Prescription flow
      app.swipeIntoView(SwipeDirection.UP, PrescriptionDetails.TECHNICAL_INFORMATION);
      app.tap(PrescriptionDetails.TECHNICAL_INFORMATION);
      val currentTaskId = app.getText(PrescriptionTechnicalInformation.TASKID);
      app.tap(PrescriptionTechnicalInformation.BACK);

      val found = currentTaskId.equals(taskId.getValue());

      if (!found) app.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
      return found;
    }
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
