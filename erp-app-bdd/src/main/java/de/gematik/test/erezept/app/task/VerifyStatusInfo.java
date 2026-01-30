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

import static de.gematik.test.erezept.app.parsers.StatusParser.*;
import static java.lang.String.format;
import static org.hl7.fhir.r4.model.Task.TaskStatus;
import static org.hl7.fhir.r4.model.Task.TaskStatus.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class VerifyStatusInfo implements Task {

  private final ErxPrescriptionBundle prescriptionBundle;

  @Step("{0} verifiziert die Statusinformation")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val task = prescriptionBundle.getTask();
    val kbvBundle = prescriptionBundle.getKbvBundle().orElseThrow();

    val actualStatusInfo = getStatusInfoFromApp(app, task.getStatus());
    val expectedStatusInfo = getExpectedStatusInfo(task, kbvBundle);

    assertTrue(
        compareStatusInfo(expectedStatusInfo, actualStatusInfo),
        format(
            "Status info for was wrong. Expected: '%s', but was: '%s'.",
            expectedStatusInfo, actualStatusInfo));
  }

  private String getStatusInfoFromApp(UseIOSApp app, TaskStatus taskStatus) {
    if (taskStatus.equals(READY)) {
      return app.getText(PrescriptionDetails.PRESCRIPTION_STATUS_TEXT);
    } else if (taskStatus.equals(INPROGRESS)) {
      return app.getText(PrescriptionDetails.PRESCRIPTION_STATUS_INFO);
    } else {
      throw new IllegalStateException("Erx Task Status" + taskStatus + "is unknown");
    }
  }

  public static VerifyStatusInfo forInput(ErxPrescriptionBundle prescriptionBundle) {
    return new VerifyStatusInfo(prescriptionBundle);
  }
}
