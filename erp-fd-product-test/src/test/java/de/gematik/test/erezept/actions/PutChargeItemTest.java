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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.chargeitem.PutChargeItem;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ChargeItemPutCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PutChargeItemTest extends ErpFhirParsingTest {
  private static final String ACCEPT_BUNDLE_PATH =
      "fhir/valid/erp/1.4.0/acceptbundle/2c08c718-3d3b-447e-babc-7afef76f0a48.xml";
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PharmacyActor pharmacyActor;
  private ErxAcceptBundle acceptBundle;

  private static ErxAcceptBundle getDecodedAcceptBundle() {
    return parser.decode(
        ErxAcceptBundle.class, ResourceLoader.readFileFromResource(ACCEPT_BUNDLE_PATH));
  }

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();

    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    pharmacyActor = actorStage.getPharmacyNamed("Am Flughafen");

    acceptBundle = getDecodedAcceptBundle();
  }

  @Test
  void shouldPutChargeItem() {
    val davAbgabedatenBundle =
        DavPkvAbgabedatenFaker.builder(PrescriptionId.from("123.456.789")).fake();
    val referencedChargeItem = ErxChargeItemFaker.builder().fake();
    val erpResponse =
        mockUtil.createErpResponse(ErxChargeItemFaker.builder().fake(), ErxChargeItem.class, 400);
    when(erpClientMock.request(any(ChargeItemPutCommand.class))).thenReturn(erpResponse);
    val putChargeItem =
        PutChargeItem.withPatientsAccessCode(AccessCode.random())
            .davBundle(davAbgabedatenBundle)
            .andReferencedChargeItem(referencedChargeItem);
    assertNotNull(putChargeItem);
  }

  @Test
  void shouldPutChargeItemWithoutAbgabedatenBundle() {
    val referencedChargeItem = ErxChargeItemFaker.builder().fake();
    val erpResponse =
        mockUtil.createErpResponse(ErxChargeItemFaker.builder().fake(), ErxChargeItem.class, 400);
    when(erpClientMock.request(any(ChargeItemPutCommand.class))).thenReturn(erpResponse);
    val putChargeItem =
        PutChargeItem.withPatientsAccessCode(AccessCode.random())
            .andReferencedChargeItem(referencedChargeItem);
    assertNotNull(putChargeItem);
  }

  @Test
  void shouldPutChargeItemAndManipulate() {
    val davAbgabedatenBundle =
        DavPkvAbgabedatenFaker.builder(PrescriptionId.from("123.456.789")).fake();
    val referencedChargeItem = ErxChargeItemFaker.builder().fake();
    referencedChargeItem.addSupportingInformation(davAbgabedatenBundle.getReference());
    val erpResponse =
        mockUtil.createErpResponse(ErxChargeItemFaker.builder().fake(), ErxChargeItem.class, 400);
    when(erpClientMock.request(any(ChargeItemPutCommand.class))).thenReturn(erpResponse);
    val manipulatorList = ErxChargeItemManipulatorFactory.supportingReferenceManipulator();
    manipulatorList.forEach(
        m -> {
          val putChargeItem =
              PutChargeItem.withPatientsAccessCode(AccessCode.random())
                  .davBundle(davAbgabedatenBundle)
                  .withMutators(List.of(m))
                  .andReferencedChargeItem(referencedChargeItem);
          assertDoesNotThrow(() -> pharmacyActor.performs(putChargeItem));
        });
  }
}
