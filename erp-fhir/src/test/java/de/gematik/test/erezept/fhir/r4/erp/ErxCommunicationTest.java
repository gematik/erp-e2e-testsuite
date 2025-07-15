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

package de.gematik.test.erezept.fhir.r4.erp;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class ErxCommunicationTest extends ErpFhirParsingTest {

  private final String basePath140 = "fhir/valid/erp/1.4.0/communication/";

  @Test
  void shouldEncodeSingleCommunicationDispReq() {
    val resourceId = "2be1c6ac-5d10-47f6-84ee-8318b2c22c76";
    val fileName = resourceId + ".json";
    val content = ResourceLoader.readFileFromResource(basePath140 + fileName);
    val communication = parser.decode(ErxCommunication.class, content);
    assertNotNull(communication, "Valid ErxCommunicationDispReq must be parseable");

    val expectedTaskId = TaskId.from("162.000.033.491.280.78");
    val expectedAccessCode =
        AccessCode.from("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    val expectedSenderKvid = "X234567890";
    val expectedRecipientId = "8-SMC-B-Testkarte-883110000123465";
    val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
    val expectedDate =
        ZonedDateTime.parse("2025-01-15T15:29:00.434+00:00")
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
    assertEquals(expectedStatus, communication.getStatus());
    assertEquals(expectedDate, communication.getSentDate());
  }

  @Test
  void shouldEncodeSingleCommunicationInfoReq() {
    val resourceId = "8ca3c379-ac86-470f-bc12-178c9008f5c9";
    val fileName = resourceId + ".json";
    val content = ResourceLoader.readFileFromResource(basePath140 + fileName);

    val communication = parser.decode(ErxCommunication.class, content);
    assertNotNull(communication, "Valid ErxCommunicationInfoReq must be parseable");

    val expectedTaskId = TaskId.from("160.000.033.491.280.78");
    val expectedAboutRef = "#SumatripanMedication";
    val expectedSenderKvid = "X234567890";
    val expectedRecipientId = "3-SMC-B-Testkarte-883110000123465";
    val expectedMessage =
        "Hallo, ich wollte gern fragen, ob das Medikament bei Ihnen vorraetig ist.";
    val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
    val expectedDate =
        ZonedDateTime.parse("2025-01-15T15:29:00.434+00:00")
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    assertEquals(CommunicationType.INFO_REQ, communication.getType());
    assertEquals(expectedTaskId, communication.getBasedOnReferenceId());
    assertTrue(communication.hasAboutReference());
    assertEquals(expectedAboutRef, communication.getAboutReference().orElseThrow());
    assertEquals(expectedSenderKvid, communication.getSenderId());
    assertEquals(expectedRecipientId, communication.getRecipientId());
    assertTrue(communication.getMessage().contains(expectedMessage));
    assertEquals(expectedStatus, communication.getStatus());
    assertEquals(expectedDate, communication.getSentDate());
    assertFalse(communication.isSubstitutionAllowed());
  }

  @Test
  void shouldEncodeSingleCommunicationReply() {
    val resourceId = "7977a4ab-97a9-4d95-afb3-6c4c1e2ac596";
    val fileName = resourceId + ".json";
    val content = ResourceLoader.readFileFromResource(basePath140 + fileName);

    val communication = parser.decode(ErxCommunication.class, content);
    assertNotNull(communication, "Valid ErxCommunicationInfoReq must be parseable");

    val expectedTaskId = TaskId.from("160.000.033.491.280.78");
    val expectedRecipientKvid = "X234567890";
    val expectedSenderId = "3-SMC-B-Testkarte-883110000123465";
    val expectedMessage =
        "Hallo, wir haben das Medikament vorrätig. Kommen Sie gern in die Filiale oder"
            + " wir schicken einen Boten.";
    val expectedStatus = Communication.CommunicationStatus.UNKNOWN;
    val expectedDate =
        ZonedDateTime.parse("2025-01-15T15:29:00.434+00:00")
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
    assertEquals(AccessCode.from(acValue), com.getBasedOnAccessCode().orElseThrow());
    assertFalse(com.getBasedOnReferenceId().getValue().isEmpty());
    assertEquals(taskId, com.getBasedOnReferenceId());
  }

  @Test
  void shouldMatchBasedOnReference() {
    List.of("a218a36e-f2fd-4603-ba67-c827acfef01b.json")
        .forEach(
            fileName -> {
              val content =
                  ResourceLoader.readFileFromResource(format("{0}{1}", basePath140, fileName));
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
    ResourceLoader.readFilesFromDirectory(basePath140)
        .forEach(
            content -> {
              val communication = parser.decode(content);
              assertEquals(ResourceType.Communication, communication.getResourceType());
              assertEquals(ErxCommunication.class, communication.getClass());
            });
  }

  @Test
  void shouldProvideKvidNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.REPLY, CommunicationType.REPRESENTATIVE);
    kvidReceiving.forEach(
        type ->
            assertEquals(DeBasisProfilNamingSystem.KVID_GKV_SID, type.getRecipientNamingSystem()));
  }

  @Test
  void shouldProvideKvidNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REPLY);
    kvidReceiving.forEach(
        type ->
            assertEquals(DeBasisProfilNamingSystem.KVID_PKV_SID, type.getRecipientNamingSystem()));
  }

  @Test
  void shouldProvidePharmacyNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.INFO_REQ, CommunicationType.DISP_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                DeBasisProfilNamingSystem.TELEMATIK_ID_SID, type.getRecipientNamingSystem()));
  }

  @Test
  void shouldProvidePharmacyNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                DeBasisProfilNamingSystem.TELEMATIK_ID_SID, type.getRecipientNamingSystem()));
  }
}
