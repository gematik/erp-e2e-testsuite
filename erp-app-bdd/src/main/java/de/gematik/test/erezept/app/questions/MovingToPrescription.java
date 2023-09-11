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

package de.gematik.test.erezept.app.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.PrescriptionStatus;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionsViewElement;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.thucydides.core.annotations.Step;

@RequiredArgsConstructor
public class MovingToPrescription implements Question<Optional<ErxPrescriptionBundle>> {

  private final DequeStrategy deque;
  private final DmcStack stack;

  @Override
  @Step("{0} navigiert zu dem #deque #stack E-Rezept")
  public Optional<ErxPrescriptionBundle> answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(stack));

    app.logEvent(
        format("Find the prescription {0} from {1}", dmc.getTaskId().getValue(), stack.name()));

    // first refresh the screen
    app.tap(Mainscreen.REFRESH_BUTTON);

    val fdPrescription = this.find(erpClient, dmc);
    val foundInBackend = fdPrescription.isPresent();
    val isMvo =
        fdPrescription.map(p -> p.getKbvBundle().getMedicationRequest().isMultiple()).orElse(false);

    val status = fdPrescription.map(PrescriptionStatus::from).orElse(PrescriptionStatus.REDEEMABLE);
    
    val elementBuilder = PrescriptionsViewElement.withStatus(status).asMvo(isMvo);
    val pageElement =
        fdPrescription
            .map(p -> elementBuilder.andPrescriptionName(p.getKbvBundle().getMedicationName()))
            .orElse(elementBuilder.withoutPrescriptionName());
    val webElements = app.getWebElementListLen(pageElement);

    if (webElements == 0 && !foundInBackend) return Optional.empty(); // no prescriptions found

    var foundInApp = false;
    for (var i = 0; i < webElements; i++) {
      val listPageElement = ListPageElement.forElement(pageElement, i);
      foundInApp = find(app, listPageElement, dmc);
      if (foundInApp) {
        break;
      }
    }

    if (foundInApp != foundInBackend) {
      throw new AppStateMissmatchException(
          format(
              "Prescription {0} found in App={1} but found in Backend={2}",
              dmc.getTaskId().getValue(), foundInApp, foundInBackend));
    }

    return fdPrescription;
  }

  private boolean find(UseTheApp<?> app, ListPageElement listPageElement, DmcPrescription dmc) {
    app.tap(listPageElement);
    app.scrollIntoView(ScrollDirection.DOWN, PrescriptionDetails.TECHNICAL_INFORMATION);
    app.tap(PrescriptionDetails.TECHNICAL_INFORMATION);
    val currentTaskId = app.getText(PrescriptionTechnicalInformation.TASKID);
    app.tap(PrescriptionTechnicalInformation.BACK);

    val taskId = dmc.getTaskId().getValue();
    val found = currentTaskId.equals(taskId);

    if (!found) app.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    return found;
  }

  private Optional<ErxPrescriptionBundle> find(UseTheErpClient erpClient, DmcPrescription dmc) {
    val cmd = new TaskGetByIdCommand(dmc.getTaskId());
    val response = erpClient.request(cmd);
    return response.getResourceOptional();
  }

  public static Builder fromStack(String stack) {
    return fromStack(DmcStack.fromString(stack));
  }

  public static Builder fromStack(DmcStack stack) {
    return new Builder(stack);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DmcStack stack;

    public MovingToPrescription withDeque(String order) {
      return withDeque(DequeStrategy.fromString(order));
    }

    public MovingToPrescription withDeque(DequeStrategy deque) {
      return Instrumented.instanceOf(MovingToPrescription.class)
              .withProperties(deque, stack);
    }
  }
}
