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

package de.gematik.test.erezept.lei.integration;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.smartcard.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.actors.*;
import org.hl7.fhir.r4.model.*;
import org.junit.*;

public class DrugDispensationTest {

  private PrescriptionId prescriptionId;

  private Actor patient;
  private UseTheErpClient useMockClientAbility;
  private ProvideEGK egkAbility;
  private ReceiveDispensedDrugs dispensedDrugs;

  @Before
  public void setUp() {
    this.prescriptionId = new PrescriptionId("123");
    this.useMockClientAbility = mock(UseTheErpClient.class);
    this.dispensedDrugs = new ReceiveDispensedDrugs();

    OnStage.setTheStage(Cast.ofStandardActors());
    patient = OnStage.theActorCalled("Marty");
    patient.can(useMockClientAbility);
    givenThat(patient).can(dispensedDrugs);

    val mockEgk = mock(Egk.class);
    when(mockEgk.getKvnr()).thenReturn("X123456789");
    egkAbility = ProvideEGK.sheOwns(mockEgk);
    givenThat(patient).can(egkAbility);
    givenThat(patient).can(ManageChargeItems.sheReceives());
  }

  @Test
  public void thenReceivedDrugsWithChargeItem() {

    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemBuilder.faker(prescriptionId)
            .subject(egkAbility.getKvnr(), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .build();
    val chargeItemBundle = mock(ErxChargeItemBundle.class);
    val response = new ErpResponse(200, Map.of(), chargeItemBundle);

    when(chargeItemBundle.getChargeItem()).thenReturn(chargeItem);
    when(chargeItemBundle.getReceipt()).thenReturn(Optional.of(erxReceipt));

    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId);
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertTrue(then(patient).asksFor(HasChargeItem.forLastDispensedDrug().asPatient()));
  }

  @Test
  public void thenReceivedDrugsWithoutChargeItem() {
    val operationOutcome = createOperationOutcome();
    val response = new ErpResponse(404, Map.of(), operationOutcome);
    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId);
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertFalse(then(patient).asksFor(HasChargeItem.forLastDispensedDrug().asPatient()));
  }

  /**
   * Create a simple OperationOutcome for testing purposes
   *
   * <p><b>NOTE:</b> This method is a duplicate from [erp-fhir].FhirTestResourceUtil
   *
   * @return
   */
  private static OperationOutcome createOperationOutcome() {
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    val oo = new OperationOutcome();
    oo.setIssue(List.of(issue));

    oo.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
    oo.getText().setDivAsString("<div>narrative</div>");

    return oo;
  }
}
