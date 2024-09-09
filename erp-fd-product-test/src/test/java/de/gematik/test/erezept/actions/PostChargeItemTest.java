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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.chargeitem.PostChargeItem;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostChargeItemTest extends ParsingTest {
  private static final String ACCEPT_BUNDLE_PATH =
      "fhir/valid/erp/1.2.0/acceptbundle/cef4b960-7ce4-4755-b4ce-3b01a30ec2f0.xml";
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PharmacyActor pharmacyActor;
  private PatientActor patientActor;

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
    pharmacyActor = actorStage.getPharmacyNamed("Stadtapotheke");
    patientActor = actorStage.getPatientNamed("Leonie Hütter");

    acceptBundle = getDecodedAcceptBundle();
  }

  @Test
  void shouldPostChargeItem() {
    val davAbgabedatenBundle =
        DavAbgabedatenFaker.builder(PrescriptionId.from("123.456.789")).fake();

    val erpResponse =
        mockUtil.createErpResponse(ErxChargeItemFaker.builder().fake(), ErxChargeItem.class, 201);
    when(erpClientMock.request(any(ChargeItemPostCommand.class))).thenReturn(erpResponse);
    val postChargeItem =
        PostChargeItem.forPatient(patientActor)
            .davBundle(davAbgabedatenBundle)
            .withAcceptBundle(acceptBundle);
    assertDoesNotThrow(() -> pharmacyActor.performs(postChargeItem));
  }
}
