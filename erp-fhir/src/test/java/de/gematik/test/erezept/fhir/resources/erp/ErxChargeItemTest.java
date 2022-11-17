/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.junit.Before;
import org.junit.Test;

public class ErxChargeItemTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeValidErxChargeItems() {
    val rawFiles = List.of("ChargeItem_01.xml", "ChargeItem_01.json");

    rawFiles.forEach(
        fileName -> {
          val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
          val chargeItem = parser.decode(ErxChargeItem.class, content);
          assertNotNull("Valid ErxMedicationDispense must be parseable", chargeItem);
          assertFalse(chargeItem.hasInsuranceProvider());
          assertFalse(chargeItem.hasSubsidy());
          assertFalse(chargeItem.hasTaxOffice());
          assertEquals(
              new PrescriptionId("160.123.456.789.123.58"), chargeItem.getPrescriptionId());
          assertEquals(ChargeItem.ChargeItemStatus.BILLABLE, chargeItem.getStatus());
          assertEquals("X234567890", chargeItem.getSubjectKvid());
          assertEquals("606358757", chargeItem.getEntererTelematikId());
        });
  }
}
