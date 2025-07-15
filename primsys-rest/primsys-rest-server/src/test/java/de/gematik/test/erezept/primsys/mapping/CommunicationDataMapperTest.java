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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.data.communication.CommunicationDtoType;
import lombok.val;
import org.junit.jupiter.api.Test;

class CommunicationDataMapperTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CommunicationDataMapper.class));
  }

  @Test
  void shouldMapInfoReq() {
    val com =
        ErxCommunicationBuilder.forInfoRequest("Hallo, das ist meine Request Nachricht!")
            .basedOn(TaskId.from("4711"))
            .receiver("606358757")
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .insurance(IKNR.asDefaultIknr("104212059"))
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .build();

    val dto = assertDoesNotThrow(() -> CommunicationDataMapper.from(com));
    assertEquals(CommunicationDtoType.INFO_REQ, dto.getType());
  }
}
