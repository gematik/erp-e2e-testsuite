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
 */

package de.gematik.test.erezept.integration.chargeitem;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.chargeitem.GetChargeItemById;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Tag("CHARGE_ITEM")
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
public class GetChargeItemByIdForGkv extends ErpTest {
  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @Test
  @TestcaseId("ERP_CHARGE_ITEM_GKV_01")
  @DisplayName("Abrufen von Abrechnungsinformation für einen GKV Versicherten")
  void getChargeItemForGkv() {
    sina.changePatientInsuranceType(InsuranceTypeDe.GKV);
    val activation = doctor.performs(IssuePrescription.forPatient(sina).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    val chargeItem =
        sina.performs(
            GetChargeItemById.withPrescriptionId(task.getPrescriptionId()).withoutAccessCode());

    sina.attemptsTo(
        Verify.that(chargeItem)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }
}
