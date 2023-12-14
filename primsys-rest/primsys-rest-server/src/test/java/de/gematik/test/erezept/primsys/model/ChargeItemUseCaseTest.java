/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemPutCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.mapping.CoverageDataMapper;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.data.PriceComponentData;
import jakarta.ws.rs.WebApplicationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class ChargeItemUseCaseTest extends TestWithActorContext {

  @Test
  void should404OnUnknownTaskId() {
    val pharmacy = mock(Pharmacy.class);
    val uc = new ChargeItemUseCase(pharmacy);

    try (val response = uc.postChargeItem("123", (InvoiceData) null)) {
      fail("RejectUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(404, wae.getResponse().getStatus());
    }
  }

  @Test
  void shouldCreateChargeItem() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(
                ErxChargeItemBuilder.faker(PrescriptionId.random()).build(), ErxChargeItem.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(ChargeItemPostCommand.class))).thenReturn(mockResponse);

    val taskId = "123";
    val receiptId = "123";

    val dispensedData = new DispensedMedicationDto();
    val acceptData = new AcceptedPrescriptionDto();
    acceptData.setInsurance(CoverageDataMapper.randomDto());
    acceptData.setPrescriptionReference("Bundle/123123");

    dispensedData.setReceipt(receiptId);
    dispensedData.setPrescriptionId(taskId);
    dispensedData.setAcceptData(acceptData);
    ActorContext.getInstance().addDispensedMedications(dispensedData);

    val uc = new ChargeItemUseCase(pharmacy);
    try (val response = uc.postChargeItem(taskId, (InvoiceData) null)) {
      assertEquals(200, response.getStatus());
    } catch (Exception e) {
      fail("CreateChargeItemUseCase has thrown an unexpected Exception", e);
    }
  }

  @Test
  void shouldChangeChargeItem() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val taskId = "123";
    val receiptId = "123";

    val originalChargeItem =
        ErxChargeItemBuilder.faker(PrescriptionId.from(taskId))
            .entered(new Date(), TemporalPrecisionEnum.MILLI)
            .build();
    val chargeItemBundle = new ErxChargeItemBundle();
    chargeItemBundle.addEntry(new Bundle.BundleEntryComponent().setResource(originalChargeItem));
    val mockGetResponse =
        ErpResponse.forPayload(chargeItemBundle, ErxChargeItemBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val mockPostResponse =
        ErpResponse.forPayload(
                ErxChargeItemBuilder.faker(PrescriptionId.random()).build(), ErxChargeItem.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val mockPutResponse =
        ErpResponse.forPayload(
                ErxChargeItemBuilder.faker(PrescriptionId.random()).build(), ErxChargeItem.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(mockClient.request(any(ChargeItemGetByIdCommand.class))).thenReturn(mockGetResponse);
    when(mockClient.request(any(ChargeItemPostCommand.class))).thenReturn(mockPostResponse);
    when(mockClient.request(any(ChargeItemPutCommand.class))).thenReturn(mockPutResponse);

    val dispensedData = new DispensedMedicationDto();
    val acceptData = new AcceptedPrescriptionDto();

    dispensedData.setReceipt(receiptId);
    dispensedData.setPrescriptionId(taskId);
    dispensedData.setAcceptData(acceptData);
    ActorContext.getInstance().addDispensedMedications(dispensedData);

    val uc = new ChargeItemUseCase(pharmacy);
    val invoiceData = new InvoiceData();
    invoiceData.setVatRate(19.0f);
    val pcd = new PriceComponentData();
    pcd.setType("DISCOUNT");
    invoiceData.setPriceComponents(List.of(pcd));
    try (val response = uc.putChargeItem(taskId, "access_code", invoiceData)) {
      assertEquals(200, response.getStatus());
    } catch (Exception e) {
      fail("CreateChargeItemUseCase has thrown an unexpected Exception: ", e);
    }
  }
}
