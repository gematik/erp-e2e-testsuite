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

import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.createTestActor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
class EnsureThatThePrescriptionTest extends ErpFhirParsingTest {

  private Actor theAppUser;

  void putDMCPrescriptionOnStack(PrescriptionFlowType flowType) {
    val prescriptionId = PrescriptionId.random(flowType);
    theAppUser
        .abilityTo(ManageDataMatrixCodes.class)
        .appendDmc(DmcPrescription.ownerDmc(TaskId.from(prescriptionId), AccessCode.random()));
  }

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    theAppUser = createTestActor(ManageDataMatrixCodes.sheGetsPrescribed());
    doNothing().when(theAppUser).attemptsTo(any(EnsureThatTheEVDGAPrescription.class));
    doNothing().when(theAppUser).attemptsTo(any(EnsureThatThePharmaceuticalPrescription.class));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCallEnsureEVDGAPrescription() {
    putDMCPrescriptionOnStack(PrescriptionFlowType.FLOW_TYPE_162);
    val ensureEVDGA = EnsureThatThePrescription.fromStack(DequeStrategy.LIFO).isShownCorrectly();
    assertDoesNotThrow(() -> theAppUser.attemptsTo(ensureEVDGA));

    verify(theAppUser, times(1)).attemptsTo(ensureEVDGA);
  }

  @Test
  void shouldCallEnsurePharmaceuticalPrescription() {
    putDMCPrescriptionOnStack(PrescriptionFlowType.FLOW_TYPE_160);
    val ensurePharmaceutical =
        EnsureThatThePrescription.fromStack(DequeStrategy.LIFO).isShownCorrectly();
    assertDoesNotThrow(() -> theAppUser.attemptsTo(ensurePharmaceutical));

    verify(theAppUser, times(1)).attemptsTo(ensurePharmaceutical);
  }
}
