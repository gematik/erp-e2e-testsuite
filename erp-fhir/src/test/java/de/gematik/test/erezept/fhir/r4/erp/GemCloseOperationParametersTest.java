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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class GemCloseOperationParametersTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_5 = "fhir/valid/erp/1.5.0/close/";

  @Test
  void shouldExtractPartsFromCloseOperationParameters() {
    val fileName = "PZN_Nr33_MedicationDispense.xml";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_5 + fileName);
    val closeParams = parser.decode(GemCloseOperationParameters.class, content);

    assertNotNull(closeParams, "Valid GemCloseOperationParameters must be parseable");
    val mdList = assertDoesNotThrow(closeParams::getMedicationDispenses);
    assertEquals(1, mdList.size());

    val md = mdList.get(0);
    assertEquals("160.065.873.704.859.46", md.getPrescriptionId().getValue());
  }
}
