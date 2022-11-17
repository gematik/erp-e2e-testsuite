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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.AccessCode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Communication;
import org.junit.Before;
import org.junit.Test;

public class ErxCommunicationTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleCommunicationDispReq() {
    List.of("CommunicationDispReq_01.xml", "CommunicationDispReq_01.json")
        .forEach(
            fileName -> {
              val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull("Valid ErxCommunicationDispReq must be parseable", communication);

              val expectedTaskId = "4711";
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
  public void shouldEncodeSingleCommunicationInfoReq() {
    List.of("CommunicationInfoReq_01.xml", "CommunicationInfoReq_01.json")
        .forEach(
            fileName -> {
              val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull("Valid ErxCommunicationInfoReq must be parseable", communication);

              val expectedTaskId = "4711";
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
  public void shouldEncodeSingleCommunicationReply() {
    List.of("CommunicationReply_01.xml", "CommunicationReply_01.json")
        .forEach(
            fileName -> {
              val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
              val communication = parser.decode(ErxCommunication.class, content);
              assertNotNull("Valid ErxCommunicationInfoReq must be parseable", communication);

              val expectedTaskId = "4711";
              val expectedRecipientKvid = "X234567890";
              val expectedSenderId = "606358757";
              val expectedMessage =
                  "Hallo, wir haben das Medikament vorrätig. Kommen Sie gern in die Filiale oder wir schicken einen Boten.";
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
  public void shouldProvideKvidNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.REPLY, CommunicationType.REPRESENTATIVE);
    kvidReceiving.forEach(
        type -> assertEquals(DeBasisNamingSystem.KVID, type.getRecipientNamingSystem()));
  }

  @Test
  public void shouldProvideKvidNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REPLY);
    kvidReceiving.forEach(
        type -> assertEquals(DeBasisNamingSystem.KVID_PKV, type.getRecipientNamingSystem()));
  }

  @Test
  public void shouldProvidePharmacyNamingSystemFromType01() {
    val kvidReceiving = List.of(CommunicationType.INFO_REQ, CommunicationType.DISP_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(ErpWorkflowNamingSystem.TELEMATIK_ID, type.getRecipientNamingSystem()));
  }

  @Test
  public void shouldProvidePharmacyNamingSystemFromType02() {
    val kvidReceiving = List.of(ChargeItemCommunicationType.CHANGE_REQ);
    kvidReceiving.forEach(
        type ->
            assertEquals(
                ErpWorkflowNamingSystem.TELEMATIK_ID_SID, type.getRecipientNamingSystem()));
  }
}
