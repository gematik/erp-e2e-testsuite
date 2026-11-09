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
import de.gematik.test.erezept.actions.chargeitem.PostChargeItem;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostChargeItemTest extends ErpFhirParsingTest {
  private static final String ACCEPT_BUNDLE_PATH =
      "fhir/valid/erp/1.6.0/xml/acceptBundle/acceptFromApiDescrip.xml";

  private final ErxAcceptBundle acceptBundle = getDecodedAcceptBundle();

  private static ErxAcceptBundle getDecodedAcceptBundle() {
    return parser.decode(
        ErxAcceptBundle.class, ResourceLoader.readFileFromResource(ACCEPT_BUNDLE_PATH));
  }

  @BeforeEach
  void init() {
    StopwatchProvider.init();
  }

  @Test
  void shouldPostChargeItem() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();

    val pharmacyMock = ActorMockUtil.createRandomPharmacy();
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val davAbgabedatenBundle =
        DavPkvAbgabedatenFaker.builder(PrescriptionId.from("123.456.789")).fake();

    val erpResponse =
        ErpResponseUtil.createErpResponse(
            ErxChargeItemFaker.builder().fake(), ErxChargeItem.class, 201);
    when(pharmacyClient.request(any(ChargeItemPostCommand.class))).thenReturn(erpResponse);
    val manipulatorList = ErxChargeItemManipulatorFactory.binaryVersionManipulator();
    manipulatorList.forEach(
        m -> {
          val postChargeItem =
              PostChargeItem.forPatient(patientActor)
                  .davBundle(davAbgabedatenBundle)
                  .withCustomStructureAndVersion(m)
                  .withAcceptBundle(acceptBundle);
          assertDoesNotThrow(() -> pharmacyActor.performs(postChargeItem));
        });
  }
}
