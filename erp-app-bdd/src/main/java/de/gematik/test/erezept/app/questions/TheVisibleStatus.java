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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.EVDGAStatus;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class TheVisibleStatus implements Question<EVDGAStatus> {

  private final ErxPrescriptionBundle evdgaBundle;

  @Override
  @Step("{0} navigiert zu dem E-Rezept #taskId")
  public EVDGAStatus answeredBy(Actor actor) {

    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val taskId = evdgaBundle.getTask().getTaskId();

    app.logEvent(format("Find the status of EVDGA {0}", taskId.getValue()));

    // Set to top of screen before start
    app.swipe(SwipeDirection.DOWN);
    assertTrue(app.isDisplayed(EVDGADetails.DIGA_TITLE), "Not on DIGA details screen!");
    return determineStatus(app);
  }

  private EVDGAStatus determineStatus(UseTheApp<?> app) {
    // All stati should be uniquely identifiable, enabling the verification by elements on screen.
    if (isShownReady(app)) {
      return EVDGAStatus.READY_FOR_REQUEST;
    } else if (isShownAccepted(app)) {
      return EVDGAStatus.WAITING_OR_ACCEPTED;
    } else if (isShownDeclined(app)) {
      return EVDGAStatus.DECLINED;
    } else if (isShownGranted(app)) {
      return EVDGAStatus.GRANTED;
    } else if (isShownDownloaded(app)) {
      return EVDGAStatus.DOWNLOADED;
    } else if (isShownActivated(app)) {
      return EVDGAStatus.ACTIVATED;
    }
    // Missing Deleted Status, as it is currently not properly implemented
    // Reset to top of screen
    app.swipe(SwipeDirection.UP);
    return EVDGAStatus.NULL;
  }

  private boolean isShownReady(UseTheApp<?> app) {
    app.swipe(SwipeDirection.UP);
    // The validity drawer and corresponding icon are only shown in ready state
    return app.isDisplayed(EVDGADetails.OPEN_VALIDITY_DRAWER);
  }

  private boolean isShownRequested(UseTheApp<?> app) {
    app.swipe(SwipeDirection.DOWN);
    return app.isDisplayed(EVDGADetails.DIGA_REQUESTED_ICON);
  }

  private boolean isShownAccepted(UseTheApp<?> app) {
    return isShownRequested(app);
  }

  private boolean isShownDeclined(UseTheApp<?> app) {
    return app.isDisplayed(EVDGADetails.DIGA_DECLINE_NOTE);
  }

  private boolean isShownGranted(UseTheApp<?> app) {
    app.swipe(SwipeDirection.DOWN);
    // The copy to clipboard function only exists on screen when the code has been received
    return app.isDisplayed(EVDGADetails.COPY_CODE_ICON);
  }

  /*The following two client only states might not be necessary*/
  private boolean isShownDownloaded(UseTheApp<?> app) {
    app.swipe(SwipeDirection.UP);
    return app.isDisplayed(EVDGADetails.DIGA_DOWNLOADED_DISPLAY);
  }

  private boolean isShownActivated(UseTheApp<?> app) {
    app.swipe(SwipeDirection.UP);
    return app.isDisplayed(EVDGADetails.DIGA_ACTIVATED_DISPLAY);
  }

  public static TheVisibleStatus ofThe(ErxPrescriptionBundle evdgaBundle) {
    return Instrumented.instanceOf(TheVisibleStatus.class).withProperties(evdgaBundle);
  }
}
