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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.data.communication.CommunicationDtoType;
import lombok.val;
import org.junit.jupiter.api.Test;

class CommunicationDataMapperTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CommunicationDataMapper.class));
  }

  @Test
  void shouldMapInfoReq() {
    val com =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId(TaskId.from("4711"))
            .recipient("606358757")
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .insurance(IKNR.from("104212059"))
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .buildInfoReq("Hallo, das ist meine Request Nachricht!");

    val dto = assertDoesNotThrow(() -> CommunicationDataMapper.from(com));
    assertEquals(CommunicationDtoType.INFO_REQ, dto.getType());
  }
}
