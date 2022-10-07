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

package de.gematik.test.erezept.lei.integration;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.HasChargeItem;
import de.gematik.test.erezept.screenplay.questions.HasDispensedDrugs;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;

public class DrugDispensationTest {

  private PrescriptionId prescriptionId;

  private Actor patient;
  private UseTheErpClient useMockClientAbility;
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
  }

  @Test
  public void thenReceivedDrugsWithChargeItem() {
    val chargeItem = ErxChargeItemBuilder.faker(prescriptionId).build();
    val response = new ErpResponse(200, Map.of(), chargeItem);
    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId);
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertTrue(then(patient).asksFor(HasChargeItem.forLastDispensedDrug()));
  }

  @Test
  public void thenReceivedDrugsWithoutChargeItem() {
    val operationOutcome = createOperationOutcome();
    val response = new ErpResponse(404, Map.of(), operationOutcome);
    when(useMockClientAbility.request(any(ChargeItemGetByIdCommand.class))).thenReturn(response);

    dispensedDrugs.append(prescriptionId);
    assertTrue(then(patient).asksFor(HasDispensedDrugs.of("genau", 1)));
    assertFalse(then(patient).asksFor(HasChargeItem.forLastDispensedDrug()));
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
