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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxChargeItemTest extends ErpFhirParsingTest {

  private final String BASE_PATH_140 = "fhir/valid/erp/1.4.0/chargeitembundle/";

  @Test
  void shouldEncodeValidErxChargeItem() {
    val fileName = "bundle_01.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_140 + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    val chargeItem = bundle.getChargeItem();

    assertNotNull(chargeItem, "Valid ErxMedicationDispense must be parseable");
    assertFalse(chargeItem.hasInsuranceProvider());
    assertFalse(chargeItem.hasSubsidy());
    assertFalse(chargeItem.hasTaxOffice());
    assertEquals("200.000.003.355.048.35", chargeItem.getPrescriptionId().getValue());
    assertEquals(ChargeItem.ChargeItemStatus.BILLABLE, chargeItem.getStatus());
    assertEquals("X110465770", chargeItem.getSubjectKvnr().getValue());
    assertEquals(
        "3-SMC-B-Testkarte-883110000116873", chargeItem.getEntererTelematikId().getValue());
  }

  @Test
  void shouldEncodeMarkingFlags() {
    val fileName = "bundle_02.xml";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_140 + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    val chargeItem = bundle.getChargeItem();

    assertFalse(chargeItem.hasInsuranceProvider());
    assertFalse(chargeItem.hasSubsidy());
    assertFalse(chargeItem.hasTaxOffice());
  }

  @Test
  void shouldThrowOnMissingFields() {
    val fileName = "bundle_02.xml";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_140 + fileName);
    val bundle = parser.decode(ErxChargeItemBundle.class, content);
    val chargeItem = bundle.getChargeItem();

    // remove the prescription id
    chargeItem.setIdentifier(List.of());
    assertThrows(MissingFieldException.class, chargeItem::getPrescriptionId);
    assertTrue(chargeItem.getAccessCode().isEmpty());
  }

  @ParameterizedTest
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldChangeContainedData(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .version(PatientenrechnungVersion.V1_0_0)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.asPkv("X234567890"), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .markingFlag(false, false, true)
            .verordnung(UUID.randomUUID().toString())
            .abgabedatensatz(davBundle, (b -> "helloworld".getBytes()))
            .build();

    val changedResource = "changed binary data";
    val abgabeReference = new AbgabedatensatzReference(UUID.randomUUID().toString());
    val changedChargeItem =
        chargeItem.withChangedContainedBinaryData(abgabeReference, changedResource.getBytes());
    assertEquals(changedResource, new String(changedChargeItem.getContainedBinaryData()));

    val vr = ValidatorUtil.encodeAndValidate(parser, changedChargeItem);
    assertTrue(vr.isSuccessful());
  }
}
