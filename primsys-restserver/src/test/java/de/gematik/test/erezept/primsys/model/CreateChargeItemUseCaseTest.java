/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.smartcard.SmcB;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CreateChargeItemUseCaseTest {

  @Test
  void should404OnUnknownTaskId() {
    val uc = new CreateChargeItemUseCase();
    val pharmacy = mock(Pharmacy.class);
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);

      try (val response = uc.postChargeItem(pharmacy, "123", (InvoiceData) null)) {
        fail("RejectUseCase did not throw the expected Exception");
      } catch (WebApplicationException wae) {
        assertEquals(WebApplicationException.class, wae.getClass());
        assertEquals(404, wae.getResponse().getStatus());
      }
    }
  }

  @Test
  void shouldCreateChargeItem() {
    val taskId = "123";

    val pharmacy = mock(Pharmacy.class);
    val mockClient = mock(ErpClient.class);
    val mockResponse = mock(ErpResponse.class);
    val mockFhir = mock(FhirParser.class);
    val mockSmcb = mock(SmcB.class);
    when(pharmacy.getSmcb()).thenReturn(mockSmcb);
    when(pharmacy.getName()).thenReturn("mockApo");
    when(mockSmcb.getTelematikId()).thenReturn("1233456_TelematikId");
    when(pharmacy.getClient()).thenReturn(mockClient);
    when(mockClient.getFhir()).thenReturn(mockFhir);
    when(mockFhir.decode(eq(KbvErpBundle.class), anyString()))
        .thenReturn(KbvErpBundleBuilder.faker().build());
    when(mockClient.encode(any(DavAbgabedatenBundle.class), eq(EncodingType.XML)))
        .thenReturn("xml encoded DAV Bundle");
    when(pharmacy.signDocument(anyString())).thenReturn("signed xml DAV Bundle".getBytes());
    when(pharmacy.erpRequest(any(ChargeItemPostCommand.class))).thenReturn(mockResponse);
    when(mockResponse.getStatusCode()).thenReturn(200);

    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val receiptId = "123";

      val dispensedData = new DispensedData();
      val acceptData = new AcceptData();

      acceptData.setKbvBundle("encoded KBV-Bundle");
      dispensedData.setReceipt(receiptId);
      dispensedData.setTaskId(taskId);
      dispensedData.setAcceptData(acceptData);
      when(mockActorContext.getDispensedMedications()).thenReturn(List.of(dispensedData));

      val uc = new CreateChargeItemUseCase();
      try (val response = uc.postChargeItem(pharmacy, taskId, (InvoiceData) null)) {
        assertEquals(200, response.getStatus());
      } catch (Exception e) {
        fail("CreateChargeItemUseCase has thrown an unexpected Exception");
      }
    }
  }
}
