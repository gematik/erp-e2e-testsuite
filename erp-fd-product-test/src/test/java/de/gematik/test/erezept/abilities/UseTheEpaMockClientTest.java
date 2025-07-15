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

package de.gematik.test.erezept.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.client.exceptions.FhirValidationException;
import de.gematik.test.erezept.eml.EpaMockClient;
import de.gematik.test.erezept.eml.EpaMockDownloadRequest;
import de.gematik.test.erezept.eml.ErpEmlLog;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseTheEpaMockClientTest extends ErpFhirBuildingTest {

  @Test
  void shouldCreateUseTheEpaMockClientInstance() {
    val mockClient = mock(HttpBClient.class);
    val ability = assertDoesNotThrow(() -> UseTheEpaMockClient.with(mockClient));
    assertNotNull(ability);
  }

  @Test
  void shouldDownloadPrescriptionByIdWithUseTheEpaMockClient() {
    val mockClientMock = mock(EpaMockClient.class);
    val prescriptionId = PrescriptionId.random();
    val body =
        ResourceLoader.readFileFromResource(
            "fhir/valid/parameters/Parameters-example-epa-op-provide-prescription-erp-input-parameters-1.json");

    val bRequest = HttpBRequest.get().urlPath("provide-prescription-erp").withPayload(body);
    val mockErpEmlLog =
        new ErpEmlLog(1L, "", "", bRequest, HttpBResponse.status(200).withoutPayload());

    when(mockClientMock.pollRequest(
            any(EpaMockDownloadRequest.class), eq("provide-prescription-erp")))
        .thenReturn(List.of(mockErpEmlLog));

    val useEpaMockClient = UseTheEpaMockClient.with(mockClientMock);
    val result = useEpaMockClient.downloadProvidePrescriptionBy(prescriptionId);

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void shouldThrowWhileDownloadInvalidDispensationByIdWithUseTheEpaMockClient() {
    val mockClientMock = mock(EpaMockClient.class);
    val prescriptionId = PrescriptionId.random();
    val body =
        ResourceLoader.readFileFromResource(
            "fhir/invalid/medication/dispensation/MissingAmountExtension.json");

    val bRequest = HttpBRequest.get().urlPath("provide-dispensation-erp").withPayload(body);
    val erpEmlLog = new ErpEmlLog(1L, "", "", bRequest, HttpBResponse.status(200).withoutPayload());

    when(mockClientMock.pollRequest(
            any(EpaMockDownloadRequest.class), eq("provide-dispensation-erp")))
        .thenReturn(List.of(erpEmlLog));

    val useEpaMockClient = UseTheEpaMockClient.with(mockClientMock);
    assertThrows(
        FhirValidationException.class,
        () -> useEpaMockClient.downloadProvideDispensationBy(prescriptionId));
  }

  @Test
  void shouldDownloadDispensationByIdWithUseTheEpaMockClient() {
    val mockClientMock = mock(EpaMockClient.class);
    val prescriptionId = PrescriptionId.random();
    val body =
        ResourceLoader.readFileFromResource(
            "fhir/valid/parameters/Parameters-example-epa-op-provide-dispensation-erp-input-parameters-1.json");

    val bRequest = HttpBRequest.get().urlPath("provide-dispensation-erp").withPayload(body);
    val mockErpEmlLog =
        new ErpEmlLog(1L, "", "", bRequest, HttpBResponse.status(200).withoutPayload());
    when(mockClientMock.pollRequest(
            any(EpaMockDownloadRequest.class), eq("provide-dispensation-erp")))
        .thenReturn(List.of(mockErpEmlLog));
    val useEpaMockClient = UseTheEpaMockClient.with(mockClientMock);

    val result = useEpaMockClient.downloadProvideDispensationBy(prescriptionId);

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void shouldDownloadCancelPrescriptionByIdWithUseTheEpaMockClient() {
    val mockClientMock = mock(EpaMockClient.class);
    val prescriptionId = PrescriptionId.random();
    val body =
        ResourceLoader.readFileFromResource(
            "fhir/valid/parameters/Parameters-example-epa-op-cancel-prescription-erp-input-parameters-1.json");

    val bRequest = HttpBRequest.get().urlPath("cancel-prescription-erp").withPayload(body);
    val mockErpEmlLog =
        new ErpEmlLog(1L, "", "", bRequest, HttpBResponse.status(200).withoutPayload());

    when(mockClientMock.pollRequest(
            any(EpaMockDownloadRequest.class), eq("cancel-prescription-erp")))
        .thenReturn(List.of(mockErpEmlLog));

    val useEpaMockClient = UseTheEpaMockClient.with(mockClientMock);

    val result = useEpaMockClient.downloadCancelPrescriptionBy(prescriptionId);

    assertNotNull(result);
    assertEquals(1, result.size());
  }
}
