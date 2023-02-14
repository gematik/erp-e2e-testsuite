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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.extensions.erp.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.*;

class ErxCommunicationBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildCommunicationInfoReqFixedValues(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
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

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldThrowOnMissingMedicationForInfoReq(ErpWorkflowVersion version) {
    val builder =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTaskId("4711")
            .insurance(IKNR.from("104212059"))
            .recipient("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160);

    assertThrows(
        BuilderException.class,
        () -> builder.buildInfoReq("Hallo, das ist meine Request Nachricht!"));
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldThrowOnMissingFlowTypeForInfoReq(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
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

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationDispReq with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildDispReqFixedValues(ErpWorkflowVersion version) {
    val dispReq =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTask("4711", "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea")
            .recipient("606358757")
            .buildDispReq("Bitte schicken Sie einen Boten.");

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationRepresentative with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildRepresentative(ErpWorkflowVersion version) {
    val representative =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTask("4711", "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea")
            .recipient("X123456789")
            .sender("X987654321")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildRepresentative("Bitte für mich abholen");

    val result = ValidatorUtil.encodeAndValidate(parser, representative);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build CommunicationDispReq with E-Rezept FHIR Profiles {0} and missing Task")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldThrowOnMissingTaskForDispReq(ErpWorkflowVersion version) {
    val builder = ErxCommunicationBuilder.builder().version(version).recipient("606358757");

    assertThrows(
        BuilderException.class, () -> builder.buildDispReq("Bitte schicken Sie einen Boten."));
  }

  @ParameterizedTest(name = "[{index}] -> Build CommunicationReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void buildCommunicationReplyFixedValues(ErpWorkflowVersion version) {
    val reply =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTaskId("4711")
            .recipient("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.onPremise())
            .buildReply("Hallo, das ist meine Response Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeRequest with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildCommunicationChargeChangeRequest(ErpWorkflowVersion version) {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    val changeReq =
        ErxChargeItemCommunicationBuilder.builder()
            .version(version)
            .basedOnChargeItem(chargeItem)
            .recipient("606358757")
            .sender("X234567890")
            .buildReq("Hallo, das ist meine ChargeItem Change Request Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void buildCommunicationChargeChangeReply(ErpWorkflowVersion version) {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    val changeReply =
        ErxChargeItemCommunicationBuilder.builder()
            .version(version)
            .basedOnChargeItem(chargeItem)
            .recipient("X234567890")
            .sender("606358757")
            .buildReply("ChargeItem Change Request erhalten, das hier ist die Antwort!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReply);
    assertTrue(result.isSuccessful());
  }
}
