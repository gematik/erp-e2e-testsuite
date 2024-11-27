/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxCommunicationBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldBuildCommunicationInfoReqFixedValues(String erpFhirProfileVersion) {
    System.setProperty("erp.fhir.profile", erpFhirProfileVersion);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val infoReq =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId(TaskId.from("4711"))
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
  void shouldBuildOldCommunicationInfoReqFixedValues() {
    val medication = KbvErpMedicationPZNFaker.builder().withVersion(KbvItaErpVersion.V1_0_2).fake();
    val infoReq =
        ErxCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnTaskId(TaskId.from("4711"))
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
            .basedOnTaskId(TaskId.from("4711"))
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
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val builder =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId(TaskId.from("4711"))
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
  void shouldBuildDispReqFixedValues(ErpWorkflowVersion version) {
    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();

    val dispReq =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTask(taskId, accessCode)
            .recipient("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildDispReq(new CommunicationDisReqMessage());

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());

    // check basedOn-Reference
    assertEquals(taskId, dispReq.getBasedOnReferenceId());
    assertEquals(accessCode, dispReq.getBasedOnAccessCode().orElseThrow());
  }

  @Test
  void shouldBuildOldDispReqFixedValues() {
    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();

    val dispReq =
        ErxCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnTask(taskId, accessCode)
            .recipient("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildDispReq(new CommunicationDisReqMessage());

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());

    // check basedOn-Reference
    assertEquals(taskId, dispReq.getBasedOnReferenceId());
    assertEquals(accessCode, dispReq.getBasedOnAccessCode().orElseThrow());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationRepresentative with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildRepresentative(ErpWorkflowVersion version) {
    val taskId = TaskId.from("4711");
    val accessCode =
        AccessCode.fromString("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    val representative =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTask(taskId, accessCode)
            .recipient("X123456789")
            .sender("X987654321")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildRepresentative("Bitte für mich abholen");

    val result = ValidatorUtil.encodeAndValidate(parser, representative);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildOldRepresentative() {
    val taskId = TaskId.from("4711");
    val accessCode =
        AccessCode.fromString("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    val representative =
        ErxCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnTask(taskId, accessCode)
            .recipient("X123456789")
            .sender("X987654321")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildRepresentative("Bitte für mich abholen");

    val result = ValidatorUtil.encodeAndValidate(parser, representative);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build CommunicationDispReq with E-Rezept FHIR Profiles {0} and missing"
              + " Task")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldThrowOnMissingTaskForDispReq(ErpWorkflowVersion version) {
    val builder = ErxCommunicationBuilder.builder().version(version).recipient("606358757");
    val message = new CommunicationDisReqMessage();
    assertThrows(BuilderException.class, () -> builder.buildDispReq(message));
  }

  @ParameterizedTest(name = "[{index}] -> Build CommunicationReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldBuildCommunicationReplyFixedValues(ErpWorkflowVersion version) {
    val reply =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTaskId(TaskId.from("4711"))
            .recipient("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .buildReply(new CommunicationReplyMessage());

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildOldCommunicationReplyFixedValues() {
    val reply =
        ErxCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnTaskId(TaskId.from("4711"))
            .recipient("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .buildReply(new CommunicationReplyMessage());

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build CommunicationReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldBuildCommunicationReplyWithDefaultSupplyOptions(ErpWorkflowVersion version) {
    val reply =
        ErxCommunicationBuilder.builder()
            .version(version)
            .basedOnTaskId(TaskId.from("4711"))
            .recipient("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .buildReply(new CommunicationReplyMessage());

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeRequest with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildCommunicationChargeChangeRequest(ErpWorkflowVersion version) {
    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
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

  @Test
  void shouldBuildOldCommunicationChargeChangeRequest() {
    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    val changeReq =
        ErxChargeItemCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnChargeItem(chargeItem)
            .recipient("606358757")
            .sender("X234567890")
            .buildReq("Hallo, das ist meine ChargeItem Change Request Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeRequest with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildCommunicationChargeChangeRequest02(ErpWorkflowVersion version) {
    val taskId = TaskId.from("123");
    val changeReq =
        ErxChargeItemCommunicationBuilder.builder()
            .version(version)
            .basedOnChargeItem(taskId)
            .recipient("606358757")
            .sender("X234567890")
            .buildReq("Hallo, das ist meine ChargeItem Change Request Nachricht!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildCommunicationChargeChangeReply(ErpWorkflowVersion version) {
    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
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

  @Test
  void shouldBuildOldCommunicationChargeChangeReply() {
    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    val changeReply =
        ErxChargeItemCommunicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_1_1)
            .basedOnChargeItem(chargeItem)
            .recipient("X234567890")
            .sender("606358757")
            .buildReply("ChargeItem Change Request erhalten, das hier ist die Antwort!");

    val result = ValidatorUtil.encodeAndValidate(parser, changeReply);
    assertTrue(result.isSuccessful());
  }
}
