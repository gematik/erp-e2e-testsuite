/*
 * Copyright (Change Date see Readme), gematik GmbH
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
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.BundlePagingCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DownloadBundleTest extends ErpFhirParsingTest {

  private static ErxAuditEventBundle erxAuditEventBundle;

  @BeforeAll
  static void prepare() {
    erxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.6.0/json/auditEventBundle/AEBByFD58c1f701-c3b4-416b-b32d-381ec396d194.json"));
  }

  @BeforeEach
  void init() {
    StopwatchProvider.init();
  }

  @Test
  void shouldGetAuditEventCorrectByExplicitCommand() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val erpResponse =
        ErpResponseUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question =
        DownloadBundle.withBundlePagingCommand(
            BundlePagingCommand.getNextFrom(erxAuditEventBundle));
    when(patientClient.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetSelfAuditEventCorrectByExplicitCommand() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val erpResponse =
        ErpResponseUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question =
        DownloadBundle.withBundlePagingCommand(
            BundlePagingCommand.getSelfFrom(erxAuditEventBundle));
    when(patientClient.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectByNextForCall() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val erpResponse =
        ErpResponseUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.nextFor(erxAuditEventBundle);
    when(patientClient.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectBySelfForCall() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val erpResponse =
        ErpResponseUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.selfFor(erxAuditEventBundle);
    when(patientClient.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }

  @Test
  void shouldGetAuditEventCorrectByPreviousForCall() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val erpResponse =
        ErpResponseUtil.createErpResponse(erxAuditEventBundle, ErxAuditEventBundle.class, 201);
    val question = DownloadBundle.previousFor(erxAuditEventBundle);
    when(patientClient.request(any(BundlePagingCommand.class))).thenReturn(erpResponse);
    assertDoesNotThrow(() -> patientActor.performs(question));
  }
}
