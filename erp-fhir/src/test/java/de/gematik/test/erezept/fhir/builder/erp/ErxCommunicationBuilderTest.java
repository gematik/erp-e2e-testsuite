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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxCommunicationBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldBuildCommunicationInfoReqFixedValues(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val infoReq =
        ErxCommunicationBuilder.forInfoRequest("Hallo, das ist meine Request Nachricht!")
            .basedOn(TaskId.from("4711"))
            .status("unknown")
            .medication(medication)
            .insurance(IKNR.asDefaultIknr("104212059"))
            .receiver("606358757")
            .substitution(false)
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, infoReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldThrowOnMissingMedicationForInfoReq(ErpWorkflowVersion version) {
    val builder =
        ErxCommunicationBuilder.forInfoRequest("Hallo, das ist meine Request Nachricht!")
            .version(version)
            .basedOn(TaskId.from("4711"))
            .insurance(IKNR.asDefaultIknr("104212059"))
            .receiver("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160);

    assertThrows(BuilderException.class, builder::build);
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationInfoReq with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldThrowOnMissingFlowTypeForInfoReq(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val builder =
        ErxCommunicationBuilder.forInfoRequest("Hallo, das ist meine Request Nachricht!")
            .basedOn(TaskId.from("4711"))
            .insurance(IKNR.asDefaultIknr("104212059"))
            .medication(medication)
            .receiver("606358757");

    assertThrows(BuilderException.class, builder::build);
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationDispReq with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildDispReqFixedValues(ErpWorkflowVersion version) {
    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();

    val dispReq =
        ErxCommunicationBuilder.forDispenseRequest(new CommunicationDisReqMessage())
            .version(version)
            .basedOn(taskId, accessCode)
            .receiver("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());

    // check basedOn-Reference
    assertEquals(taskId, dispReq.getBasedOnReferenceId());
    assertEquals(accessCode, dispReq.getBasedOnAccessCode().orElseThrow());
  }

  @Test
  void shouldBuildDispReqFixedValuesForDiGA() {
    val version = ErpWorkflowVersion.V1_4;
    val taskId = TaskId.from(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162));
    val accessCode = AccessCode.random();

    val dispReq =
        ErxCommunicationBuilder.forDiGADispenseRequest()
            .version(version)
            .basedOn(taskId, accessCode)
            .receiver("606358757")
            .flowType(PrescriptionFlowType.FLOW_TYPE_162)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, dispReq);
    assertTrue(result.isSuccessful());

    assertTrue(dispReq.getPayload().isEmpty(), "Payload should be empty for DiGA Workflow 162");
    assertEquals(taskId, dispReq.getBasedOnReferenceId());
    assertEquals(accessCode, dispReq.getBasedOnAccessCode().orElseThrow());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationRepresentative with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildRepresentative(ErpWorkflowVersion version) {
    val taskId = TaskId.from("4711");
    val accessCode =
        AccessCode.from("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    val representative =
        ErxCommunicationBuilder.forRepresentative("Bitte für mich abholen")
            .version(version)
            .basedOn(taskId, accessCode)
            .receiver("X123456789")
            .sender("X987654321")
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, representative);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build CommunicationDispReq with E-Rezept FHIR Profiles {0} and missing"
              + " Task")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldThrowOnMissingTaskForDispReq(ErpWorkflowVersion version) {
    val message = new CommunicationDisReqMessage();
    val builder =
        ErxCommunicationBuilder.forDispenseRequest(message).version(version).receiver("606358757");
    assertThrows(BuilderException.class, builder::build);
  }

  @ParameterizedTest(name = "[{index}] -> Build CommunicationReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldBuildCommunicationReplyFixedValues(ErpWorkflowVersion version) {
    val reply =
        ErxCommunicationBuilder.asReply(new CommunicationReplyMessage())
            .version(version)
            .basedOn(TaskId.from("4711"))
            .receiver("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build CommunicationReply with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldBuildCommunicationReplyWithDefaultSupplyOptions(ErpWorkflowVersion version) {
    val reply =
        ErxCommunicationBuilder.asReply(new CommunicationReplyMessage())
            .version(version)
            .basedOn(TaskId.from("4711"))
            .receiver("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .build();

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
        ErxCommunicationBuilder.forChargeItemChangeRequest(
                "Hallo, das ist meine ChargeItem Change Request Nachricht!")
            .version(version)
            .basedOn(chargeItem)
            .receiver("606358757")
            .sender("X234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, changeReq);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build CommunicationChangeRequest with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
  void shouldBuildCommunicationChargeChangeRequest02(ErpWorkflowVersion version) {
    val taskId = TaskId.from("123");
    val changeReq =
        ErxCommunicationBuilder.forChargeItemChangeRequest(
                "Hallo, das ist meine ChargeItem Change Request Nachricht!")
            .version(version)
            .basedOn(taskId)
            .receiver("606358757")
            .sender("X234567890")
            .build();

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
        ErxCommunicationBuilder.forChargeItemChangeReply(
                "ChargeItem Change Request erhalten, das hier ist die Antwort!")
            .version(version)
            .basedOn(chargeItem)
            .receiver("X234567890")
            .sender("606358757")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, changeReply);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build CommunicationReply for DiGA with text payload and E-Rezept Workflow"
              + " {0}")
  @EnumSource(
      value = ErpWorkflowVersion.class,
      names = {"V1_5"})
  void shouldBuildDiGARequestWithTextPayload(ErpWorkflowVersion version) {
    val taskId = TaskId.from(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162));
    val accessCode = AccessCode.random();
    val payloadText = "Dies ist ein Freitext für DiGA Communication Reply";

    val reply =
        ErxCommunicationBuilder.asReply(payloadText)
            .version(version)
            .basedOn(taskId, accessCode)
            .receiver("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .flowType(PrescriptionFlowType.FLOW_TYPE_162)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, reply);
    assertTrue(result.isSuccessful());

    val payload = reply.getPayloadFirstRep();
    assertNotNull(payload, "Payload must not be null");
    assertTrue(
        payload.getContent() instanceof StringType, "Payload content must be of type StringType");
    assertEquals(
        payloadText,
        payload.getContent().primitiveValue(),
        "Payload content must match the given text");
  }

  @Test
  void shouldSetCorrectProfileForDiGAReply() {
    val taskId = TaskId.from(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162));
    val accessCode = AccessCode.random();
    val reply =
        ErxCommunicationBuilder.asReply("DiGA Text")
            .version(ErpWorkflowVersion.V1_5)
            .flowType(PrescriptionFlowType.FLOW_TYPE_162)
            .basedOn(taskId, accessCode)
            .receiver("X234567890")
            .sender("606358757")
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .build();

    List<CanonicalType> profiles = reply.getMeta().getProfile();
    assertTrue(
        profiles.stream()
            .anyMatch(
                p ->
                    p.getValue()
                        .equals(
                            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DiGA|1.5")),
        "Expected profile for DiGA communication reply not found");

    assertTrue(reply.getPayloadFirstRep().getContent() instanceof StringType);
  }
}
