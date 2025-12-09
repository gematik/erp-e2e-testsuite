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

package de.gematik.test.erezept.app.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.questions.MovingToEVDGAPrescription;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.text.SimpleDateFormat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class EnsureThatTheEVDGAPrescription implements Task {

  private final DequeStrategy deque;

  @Override
  @Step("{0} überprüft die Darstellung von dem #deque ausgestellten E-Rezept")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);

    ErxPrescriptionBundle prescriptionBundle = actor.asksFor(MovingToEVDGAPrescription.with(deque));

    val evdgaBundle =
        prescriptionBundle
            .getEvdgaBundle()
            .orElseThrow(
                () ->
                    new MissingFieldException(
                        ErxPrescriptionBundle.class, KbvItaErpStructDef.BUNDLE));

    val evdgaRequest = evdgaBundle.getHealthAppRequest();

    // Accept Insurance missing alert
    app.acceptAlert();

    app.swipeIntoView(SwipeDirection.DOWN, EVDGADetails.DIGA_TITLE);

    val actualEVDGAName = app.getText(EVDGADetails.DIGA_TITLE);
    val expectedEVDGAName = evdgaRequest.getName();

    assertEquals(
        expectedEVDGAName,
        actualEVDGAName,
        String.format(
            "DIGA names were not equal. Name from FD: %s, name from app: %s.",
            expectedEVDGAName, actualEVDGAName));

    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    val dc = DateConverter.getInstance();

    val expectedValidityStartText = formatter.format(prescriptionBundle.getTask().getAuthoredOn());

    val correctedValidityEndDate =
        dc.dateToLocalDate(prescriptionBundle.getTask().getExpiryDate()).minusDays(1);
    val expectedValidityEndText = formatter.format(dc.localDateToDate(correctedValidityEndDate));

    app.swipeIntoView(SwipeDirection.UP, EVDGADetails.OPEN_VALIDITY_DRAWER);
    app.tap(EVDGADetails.OPEN_VALIDITY_DRAWER);
    val actualValidityStartText = app.getText(EVDGADetails.VALIDITY_START);
    val actualValidityEndText = app.getText(EVDGADetails.VALIDITY_END);

    app.swipe(SwipeDirection.DOWN);

    assertEquals(
        expectedValidityStartText,
        actualValidityStartText,
        String.format(
            "Validity starts were not equal. Text from FD: %s, text from app: %s.",
            expectedValidityStartText, actualValidityStartText));

    assertEquals(
        expectedValidityEndText,
        actualValidityEndText,
        String.format(
            "Validity ends were not equal. Text from FD: %s, text from app: %s.",
            expectedValidityEndText, actualValidityEndText));

    app.tap(EVDGADetails.LEAVE_DIGA_DETAILS);
  }

  public static Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Builder fromStack(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public EnsureThatTheEVDGAPrescription isShownCorrectly() {
      return Instrumented.instanceOf(EnsureThatTheEVDGAPrescription.class).withProperties(deque);
    }
  }
}
