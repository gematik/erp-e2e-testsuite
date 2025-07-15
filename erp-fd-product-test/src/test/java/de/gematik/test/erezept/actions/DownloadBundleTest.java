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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.BundlePagingCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownloadBundleTest extends ErpFhirParsingTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;

  private PatientActor patientActor;

  private static ErxAuditEventBundle erxAuditEventBundle;

  @BeforeAll
  static void prepare() {
    erxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.4.0/auditeventbundle/561f4a7e-0616-4c92-b6d5-91217aea136f.json"));
  }

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldGetAuditEventCorrectByExplicitCommand() {
    val erpResponse =
        mockUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question =
        DownloadBundle.withBundlePagingCommand(
            BundlePagingCommand.getNextFrom(erxAuditEventBundle));
    when(erpClientMock.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetSelfAuditEventCorrectByExplicitCommand() {
    val erpResponse =
        mockUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question =
        DownloadBundle.withBundlePagingCommand(
            BundlePagingCommand.getSelfFrom(erxAuditEventBundle));
    when(erpClientMock.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectByNextForCall() {
    val erpResponse =
        mockUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.nextFor(erxAuditEventBundle);
    when(erpClientMock.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectBySelfForCall() {
    val erpResponse =
        mockUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.selfFor(erxAuditEventBundle);
    when(erpClientMock.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectByPreviousForCall() {
    val erpResponse =
        mockUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.previousFor(erxAuditEventBundle);
    when(erpClientMock.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }
}
