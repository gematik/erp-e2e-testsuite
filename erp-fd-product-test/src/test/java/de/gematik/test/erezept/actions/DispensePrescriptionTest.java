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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispenseMedicationCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class DispensePrescriptionTest {

  @Test
  void shouldDispenseWithManipulatedPerformer() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val useErpClient = mock(UseTheErpClient.class);
    val useSmcb = mock(UseSMCB.class);

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    val manipulatedPerformerId = "I don't care";

    doAnswer(
            (Answer<ErpResponse>)
                invovation -> {
                  val args = invovation.getArguments();
                  val cmd = (DispenseMedicationCommand) args[0];
                  assertTrue(cmd.getRequestBody().isPresent());
                  val medDisp = (ErxMedicationDispense) cmd.getRequestBody().orElseThrow();
                  assertEquals(manipulatedPerformerId, medDisp.getPerformerIdFirstRep());

                  return new ErpResponse(
                      404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
                })
        .when(useErpClient)
        .request(any(DispenseMedicationCommand.class));

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockTask = mock(ErxTask.class);

    when(mockAcceptBundle.getTaskId()).thenReturn("1234567890");
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvid()).thenReturn(Optional.of("X123456789"));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleBuilder.faker("X123456789").build());
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");

    pharmacy.performs(
        DispensePrescription.alternative()
            .performer(manipulatedPerformerId)
            .acceptedWith(mockAcceptBundle));
  }
}
