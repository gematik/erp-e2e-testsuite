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

package de.gematik.test.erezept.fhir.resources.dav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.sql.Date;
import java.time.LocalDate;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.Before;
import org.junit.Test;

public class DavAbgabedatenBundleTest {

  private final String BASE_PATH = "fhir/valid/dav/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void testEncodeSingleValidBundle() {
    val expectedID = "ad80703d-8c62-44a3-b12b-2ea66eda0aa2";
    val expectedPrescriptionId = new PrescriptionId("200.100.000.000.081.90");

    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val bundle = parser.decode(DavAbgabedatenBundle.class, content);

    assertEquals(expectedID, bundle.getLogicalId());
    assertEquals(expectedPrescriptionId, bundle.getPrescriptionId());

    val pharm = bundle.getPharmacy();
    assertNotNull(pharm);
    assertEquals("Adler-Apotheke", pharm.getName());

    val dispensed = bundle.getDispensedMedication();
    assertNotNull(dispensed);
    assertEquals(expectedPrescriptionId, dispensed.getPrescriptionId());
    assertEquals(
        "urn:uuid:5dc67a4f-c936-4c26-a7c0-967673a70740", dispensed.getPerformerReference());
    assertEquals(MedicationDispense.MedicationDispenseStatus.COMPLETED, dispensed.getStatus());
    assertEquals(Date.valueOf(LocalDate.of(2022, 3, 24)), dispensed.getWhenHandedOver());

    val invoice = bundle.getInvoice();
    assertNotNull(invoice);
    assertEquals(Invoice.InvoiceStatus.ISSUED, invoice.getStatus());
    assertEquals("06313728", invoice.getPzn());
    assertEquals(30.33, invoice.getTotalPrice(), 0.001);
    assertEquals(0.0, invoice.getTotalCoPayment(), 0.001);
    assertEquals("EUR", invoice.getCurrency());
    assertEquals(19.0, invoice.getVAT(), 0.001);
  }
}
