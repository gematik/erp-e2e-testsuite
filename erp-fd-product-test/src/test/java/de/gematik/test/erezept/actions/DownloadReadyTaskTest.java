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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.EgkP12;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardOwnerData;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DownloadReadyTaskTest extends ErpFhirBuildingTest {

  private static VsdmExamEvidence examEvidence;
  private static PharmacyActor pharmacist;
  private static PatientActor sina;

  @BeforeAll
  static void setup() {
    val useErpClient = mock(UseTheErpClient.class);
    pharmacist = new PharmacyActor("PhaMoc");
    pharmacist.can(useErpClient);

    sina = new PatientActor("sina");
    val sca = SmartcardArchive.fromResources();
    val egk = sca.getEgkByKvnr("X110498565");
    sina.can(ProvideEGK.sheOwns(egk));
    val providePatientBaseData =
        ProvidePatientBaseData.forGkvPatient(KVNR.from(egk.getKvnr()), "sina");
    sina.can(providePatientBaseData);

    examEvidence =
        VsdmExamEvidence.asOnlineMode(VsdmService.instantiateWithTestKey(), sina.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);

    val mockResponse =
        ErpResponse.forPayload(new ErxTaskBundle(), ErxTaskBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(mockResponse);
  }

  @Test
  void withExamEvidence() {
    assertDoesNotThrow(
        () -> pharmacist.performs(DownloadReadyTask.with(examEvidence, sina.getEgk())));
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.with(examEvidence, sina.getKvnr(), LocalDate.now(), "")));
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.with(
                    examEvidence.encode(), sina.getKvnr(), LocalDate.now(), "")));
  }

  @Test
  void withExamEvidenceAndKVNR() {
    assertDoesNotThrow(
        () -> pharmacist.performs(DownloadReadyTask.with(examEvidence, sina.getEgk())));
  }

  @Test
  void withoutExamEvidence() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.withoutPnwParameter(
                    sina.getKvnr(),
                    sina.getEgk().getInsuranceStartDate(),
                    sina.getEgk().getOwnerData().getStreet())));
  }

  @Test
  void withoutKvnr() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.withoutKvnrParameter(
                    examEvidence,
                    sina.getEgk().getInsuranceStartDate(),
                    sina.getEgk().getOwnerData().getStreet())));
  }

  @Test
  void withoutHcv() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.withoutHcvParameter(examEvidence, sina.getKvnr())));
  }

  @Test
  void shoudGenerateHcvWithStreet() {
    val egk = mock(EgkP12.class);
    val ownerData = mock(SmartcardOwnerData.class);
    when(ownerData.getStreet()).thenReturn("Beispielstraße");
    when(egk.getOwnerData()).thenReturn(ownerData);
    when(egk.getKvnr()).thenReturn(sina.getEgk().getKvnr());
    when(egk.getInsuranceStartDate()).thenReturn(sina.getEgk().getInsuranceStartDate());

    assertDoesNotThrow(
        () -> pharmacist.performs(DownloadReadyTask.with(examEvidence, egk, List.of())));
  }

  @Test
  void withExamEvidenceKvnrAndAdditionalPagingParams() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.with(
                    examEvidence,
                    sina.getEgk(),
                    IQueryParameter.search().withOffset(5).createParameter())));
  }

  @Test
  void shouldBuildDownloadReadyTaskWithPagingParamsAsPatient() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadReadyTask.asPatient(
                    IQueryParameter.search().withOffset(5).createParameter())));
  }
}
