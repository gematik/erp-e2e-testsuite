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

package de.gematik.test.erezept.fhir.resources.erp;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxCommunicationTest extends ParsingTest {

  private final String BASE_PATH_111 = "fhir/valid/erp/1.1.1/";
  private final String BASE_PATH_120 = "fhir/valid/erp/1.2.0/";

  @Test
  void shouldEncodeSingleCommunicationDispReq() {
    List.of("CommunicationDispReq_01.xml", "CommunicationDispReq_01.json")
        .forEach(
            fileName -> {
              val content = ResourceLoader.readFileFromResource(BASE_PATH_111 + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull(communication, "Valid ErxCommunicationDispReq must be parseable");

              val expectedTaskId = TaskId.from("4711");
              val expectedAccessCode =
                  new AccessCode(
                      "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
              val expectedSenderKvid = "X234567890";
              val expectedRecipientId = "606358757";
              val expectedMessage = "Bitte schicken Sie einen Boten.";
              val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
              val expectedDate =
                  ZonedDateTime.parse("2020-03-12T18:01:10+00:00")
                      .toInstant()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime();

              assertFalse(communication.hasAboutReference());
              assertFalse(communication.getAboutReference().isPresent());
              assertEquals(CommunicationType.DISP_REQ, communication.getType());
              assertEquals(expectedTaskId, communication.getBasedOnReferenceId());
              assertTrue(communication.getBasedOnAccessCode().isPresent());
              assertEquals(expectedAccessCode, communication.getBasedOnAccessCode().orElseThrow());
              assertEquals(expectedSenderKvid, communication.getSenderId());
              assertEquals(expectedRecipientId, communication.getRecipientId());
              assertEquals(expectedMessage, communication.getMessage());
              assertEquals(expectedStatus, communication.getStatus());
              assertEquals(expectedDate, communication.getSentDate());
            });
  }

  @Test
  void shouldEncodeSingleCommunicationInfoReq() {
    List.of("CommunicationInfoReq_01.xml", "CommunicationInfoReq_01.json")
        .forEach(
            fileName -> {
              val content = ResourceLoader.readFileFromResource(BASE_PATH_111 + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull(communication, "Valid ErxCommunicationInfoReq must be parseable");

              val expectedTaskId = TaskId.from("4711");
              val expectedAboutRef = "#5fe6e06c-8725-46d5-aecd-e65e041ca3de";
              val expectedSenderKvid = "X234567890";
              val expectedRecipientId = "606358757";
              val expectedMessage =
                  "Hallo, ich wollte gern fragen, ob das Medikament bei Ihnen vorraetig ist.";
              val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
              val expectedDate =
                  ZonedDateTime.parse("2020-03-12T18:01:10+00:00")
                      .toInstant()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime();

              assertEquals(CommunicationType.INFO_REQ, communication.getType());
              assertEquals(expectedTaskId, communication.getBasedOnReferenceId());
              assertTrue(communication.hasAboutReference());
              assertEquals(expectedAboutRef, communication.getAboutReference().orElseThrow());
              assertEquals(expectedSenderKvid, communication.getSenderId());
              assertEquals(expectedRecipientId, communication.getRecipientId());
              assertEquals(expectedMessage, communication.getMessage());
              assertEquals(expectedStatus, communication.getStatus());
              assertEquals(expectedDate, communication.getSentDate());
              assertTrue(communication.isSubstitutionAllowed());
            });
  }

  @Test
  void shouldEncodeSingleCommunicationReply() {
    List.of("CommunicationReply_01.xml", "CommunicationReply_01.json")
        .forEach(
            fileName -> {
              val content = ResourceLoader.readFileFromResource(BASE_PATH_111 + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull(communication, "Valid ErxCommunicationInfoReq must be parseable");

              val expectedTaskId = TaskId.from("4711");
              val expectedRecipientKvid = "X234567890";
              val expectedSenderId = "606358757";
              val expectedMessage =
                  "Hallo, wir haben das Medikament vorrätig. Kommen Sie gern in die Filiale oder"
                      + " wir schicken einen Boten.";
              val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
              val expectedDate =
                  ZonedDateTime.parse("2020-03-12T18:01:10+00:00")
                      .toInstant()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDateTime();

              assertEquals(CommunicationType.REPLY, communication.getType());
              assertFalse(communication.hasAboutReference());
              assertFalse(communication.getAboutReference().isPresent());
              assertEquals(expectedTaskId, communication.getBasedOnReferenceId());
              assertEquals(expectedRecipientKvid, communication.getRecipientId());
              assertEquals(expectedSenderId, communication.getSenderId());
              assertEquals(expectedMessage, communication.getMessage());
              assertEquals(expectedStatus, communication.getStatus());
              assertEquals(expectedDate, communication.getSentDate());
            });
  }

  @Test
  void shouldMatchChargeItemBasedOn() {
    val com = new ErxCommunication();
    val taskId = TaskId.from("200.000.001.205.914.41");
    val acValue = "796a39e35bab3cb9c9ea44a80703bb4953ec7411d010a984a447108194d9b576";
    val referenceValue = format("ChargeItem/{0}?ac={1}", taskId, acValue);
    val reference = new Reference(referenceValue);
    com.setBasedOn(List.of(reference));

    assertTrue(com.getBasedOnAccessCode().isPresent());
    assertEquals(AccessCode.fromString(acValue), com.getBasedOnAccessCode().orElseThrow());
    assertFalse(com.getBasedOnReferenceId().getValue().isEmpty());
    assertEquals(taskId, com.getBasedOnReferenceId());
  }

  @Test
  void shouldMatchBasedOnReference() {
    List.of("a218a36e-f2fd-4603-ba67-c827acfef01b.json", "a218a36e-f2fd-4603-ba67-c827acfef01b.xml")
        .forEach(
            fileName -> {
              val content =
                  ResourceLoader.readFileFromResource(
                      format("{0}communication/{1}", BASE_PATH_120, fileName));
              val communication = parser.decode(ErxCommunication.class, content);
              assertEquals(
                  "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
                  communication.getBasedOnAccessCodeString().orElseThrow());
              assertEquals(
                  TaskId.from("160.000.033.491.280.78"), communication.getBasedOnReferenceId());
            });
  }

  @Test
  void shouldDecodeWithoutExpectedType() {
    List.of("CommunicationReply_01.xml", "CommunicationReply_01.json")
        .forEach(
            fileName -> {
              val content = ResourceLoader.readFileFromResource(BASE_PATH_111 + fileName);
              val communication = parser.decode(content);
              assertEquals(ResourceType.Communication, communication.getResourceType());
              assertEquals(ErxCommunication.class, communication.getClass());
            });
  }

  @Test
  void shouldReadSearchSetBundleCorrectly() {
    List.of("CommunicationSearchBundle.json", "CommunicationSearchBundle.xml")
        .forEach(
            fileName -> {
              val bundleContent = ResourceLoader.readFileFromResource(BASE_PATH_111 + fileName);
              val bundle = parser.decode(Bundle.class, bundleContent);

              val firstEntry = bundle.getEntry().get(0);
              if (firstEntry.getResource() instanceof ErxCommunication com) {
                assertEquals(
                    "89e0747c0360a1a25c267bd69b37a51ab930cefa95d80277b58ed28cb0d822de",
                    com.getBasedOnAccessCodeString().orElseThrow());
                assertEquals(TaskId.from("160.000.006.306.676.73"), com.getBasedOnReferenceId());
              } else {
                fail(
                    format(
                        "First Entry must be of type {0} but was {1}",
                        ErxCommunication.class, firstEntry.getResource().getResourceType()));
              }
            });
  }

  @Test
  void shouldProvideKvidNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.REPLY, CommunicationType.REPRESENTATIVE);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                DeBasisNamingSystem.KVID,
                type.getRecipientNamingSystem(ErpWorkflowVersion.V1_1_1)));
  }

  @Test
  void shouldProvideKvidNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REPLY);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                DeBasisNamingSystem.KVID_PKV,
                type.getRecipientNamingSystem(ErpWorkflowVersion.getDefaultVersion())));
  }

  @Test
  void shouldProvidePharmacyNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.INFO_REQ, CommunicationType.DISP_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                ErpWorkflowNamingSystem.TELEMATIK_ID,
                type.getRecipientNamingSystem(ErpWorkflowVersion.V1_1_1)));
  }

  @Test
  void shouldProvidePharmacyNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                ErpWorkflowNamingSystem.TELEMATIK_ID_SID,
                type.getRecipientNamingSystem(ErpWorkflowVersion.getDefaultVersion())));
  }
}
