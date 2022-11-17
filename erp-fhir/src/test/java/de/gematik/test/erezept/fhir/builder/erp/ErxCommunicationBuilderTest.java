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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxCommunicationBuilderTest extends ParsingTest {

  @Test
  void buildCommunicationInfoReqFixedValues() {
    val medication = KbvErpMedicationBuilder.faker().build();
    val infoReq =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId("4711")
            .status("unknown")
            .medication(medication)
            .insurance(IKNR.from("104212059"))
            .recipient("606358757")
            .substitution(false)
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildInfoReq("Hallo, das ist meine Request Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, infoReq);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldThrowOnMissingMedicationForInfoReq() {
    val builder =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId("4711")
            .insurance(IKNR.from("104212059"))
            .recipient("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160);

    assertThrows(
        BuilderException.class,
        () -> builder.buildInfoReq("Hallo, das ist meine Request Nachricht!"));
  }

  @Test
  void shouldThrowOnMissingFlowTypeForInfoReq() {
    val medication = KbvErpMedicationBuilder.faker().build();
    val builder =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId("4711")
            .insurance(IKNR.from("104212059"))
            .medication(medication)
            .recipient("606358757");

    assertThrows(
        BuilderException.class,
        () -> builder.buildInfoReq("Hallo, das ist meine Request Nachricht!"));
  }

  @Test
  void buildDispReqFixedValues() {
    val dispReq =
        ErxCommunicationBuilder.builder()
            .basedOnTask("4711", "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea")
            .recipient("606358757")
            .buildDispReq("Bitte schicken Sie einen Boten.");

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildRepresentative() {
    val representative =
        ErxCommunicationBuilder.builder()
            .basedOnTask("4711", "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea")
            .recipient("X123456789")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildRepresentative("Bitte für mich abholen");

    val result = ValidatorUtil.encodeAndValidate(parser, representative);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldThrowOnMissingTaskForDispReq() {
    val builder = ErxCommunicationBuilder.builder().recipient("606358757");

    assertThrows(
        BuilderException.class, () -> builder.buildDispReq("Bitte schicken Sie einen Boten."));
  }

  @Test
  void buildCommunicationReplyFixedValues() {
    val reply =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId("4711")
            .recipient("X234567890")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.onPremise())
            .buildReply("Hallo, das ist meine Response Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildCommunicationChargeChangeRequest() {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    val changeReq =
        ErxChargeItemCommunicationBuilder.builder()
            .basedOnChargeItem(chargeItem)
            .recipient("606358757")
            .sender("X234567890")
            .buildReq("Hallo, das ist meine ChargeItem Change Request Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReq);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildCommunicationChargeChangeReply() {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    val changeReply =
        ErxChargeItemCommunicationBuilder.builder()
            .basedOnChargeItem(chargeItem)
            .recipient("X234567890")
            .sender("606358757")
            .buildReply("ChargeItem Change Request erhalten, das hier ist die Antwort!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReply);
    assertTrue(result.isSuccessful());
  }
}
