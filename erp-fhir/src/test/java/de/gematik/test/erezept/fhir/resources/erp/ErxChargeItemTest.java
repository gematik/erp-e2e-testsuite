/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import de.gematik.test.erezept.fhir.values.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.*;

class ErxChargeItemTest extends ParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";
  private final String BASE_PATH_120 = "fhir/valid/erp/1.2.0/";

  @Test
  void shouldEncodeValidErxChargeItems() {
    val rawFiles = List.of("ChargeItem_01.xml", "ChargeItem_01.json");

    rawFiles.forEach(
        fileName -> {
          val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
          val chargeItem = parser.decode(ErxChargeItem.class, content);
          assertNotNull(chargeItem, "Valid ErxMedicationDispense must be parseable");
          assertFalse(chargeItem.hasInsuranceProvider());
          assertFalse(chargeItem.hasSubsidy());
          assertFalse(chargeItem.hasTaxOffice());
          assertEquals(
              new PrescriptionId("160.123.456.789.123.58"), chargeItem.getPrescriptionId());
          assertEquals(ChargeItem.ChargeItemStatus.BILLABLE, chargeItem.getStatus());
          assertEquals("X234567890", chargeItem.getSubjectKvnr().getValue());
          assertEquals("606358757", chargeItem.getEntererTelematikId());
          assertFalse(chargeItem.isFromNewProfiles());
        });
  }

  @Test
  void shouldEncodeValidErxChargeItemsAsResource() {
    val rawFiles = List.of("ChargeItem_01.xml", "ChargeItem_01.json");

    rawFiles.forEach(
        fileName -> {
          val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
          val chargeItem = parser.decode(content);
          assertNotNull(chargeItem, "Valid ChargeItem must be parseable");
          assertEquals(ResourceType.ChargeItem, chargeItem.getResourceType());
          assertEquals(ErxChargeItem.class, chargeItem.getClass());
        });
  }

  @Test
  @SetSystemProperty(key = "erp.fhir.profile", value = "1.2.0")
  void shouldFindNewProfilePrescriptionId() {
    val content = ResourceUtils.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);
    // change system of the prescription id
    chargeItem.getIdentifier().stream()
        .filter(
            identifier ->
                ErpWorkflowNamingSystem.PRESCRIPTION_ID
                    .getCanonicalUrl()
                    .equals(identifier.getSystem()))
        .forEach(
            identifier ->
                identifier.setSystem(
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID_121.getCanonicalUrl()));
    assertEquals(
        new PrescriptionId(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121, "160.123.456.789.123.58"),
        chargeItem.getPrescriptionId());
  }

  @Test
  void shouldThrowOnMissingPrescriptionId() {
    val content = ResourceUtils.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);

    // remove the prescription id
    val prescriptionIdIdentifier =
        chargeItem.getIdentifier().stream()
            .filter(
                identifier ->
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID
                            .getCanonicalUrl()
                            .equals(identifier.getSystem())
                        || ErpWorkflowNamingSystem.PRESCRIPTION_ID_121
                            .getCanonicalUrl()
                            .equals(identifier.getSystem()))
            .findFirst()
            .orElseThrow();
    chargeItem.getIdentifier().remove(prescriptionIdIdentifier);
    assertThrows(MissingFieldException.class, chargeItem::getPrescriptionId);
  }

  @Test
  void shouldChangeContainedData() {
    val prescriptionId = PrescriptionId.random();
    val davBundle = DavAbgabedatenBuilder.faker(prescriptionId).build();
    val kbvBundle = KbvErpBundleBuilder.faker().build();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .version(PatientenrechnungVersion.V1_0_0)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.from("X234567890"), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .markingFlag(false, false, true)
            .verordnung(kbvBundle)
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())) // concrete signed object won't be checked anyway
            .build();

    val changedResource = "changed binary data";
    val abgabeRefenrece = new AbgabedatensatzReference(UUID.randomUUID().toString());
    val changedChargeItem = chargeItem.withChangedContainedBinaryData(abgabeRefenrece, changedResource.getBytes());
    assertEquals(changedResource, new String(changedChargeItem.getContainedBinaryData()));

    val encodedChargeItem = parser.encode(changedChargeItem, EncodingType.XML);
    assertTrue(parser.isValid(encodedChargeItem));
  }

  @Test
  void shouldThrowOnMissingAccessCode() {
    val content = ResourceUtils.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);

    chargeItem.getIdentifier().clear();
    assertThrows(MissingFieldException.class, chargeItem::getPrescriptionId);
  }

  @Test
  void shouldFindNewProfileMarkingFlags() {
    val content = ResourceUtils.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);

    // remove the marking flags extension
    chargeItem.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(ErpWorkflowStructDef.MARKING_FLAG.getCanonicalUrl()))
        .forEach(
            identifier ->
                identifier.setUrl(PatientenrechnungStructDef.MARKING_FLAG.getCanonicalUrl()));
    assertFalse(chargeItem.hasInsuranceProvider());
    assertFalse(chargeItem.hasSubsidy());
    assertFalse(chargeItem.hasTaxOffice());
  }

  @Test
  void shouldThrowOnMissingMarkingFlags() {
    val content = ResourceUtils.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);

    // remove the marking flags extension
    val markingFlagExtension =
        chargeItem.getExtension().stream()
            .filter(
                ext ->
                    ext.getUrl().equals(ErpWorkflowStructDef.MARKING_FLAG.getCanonicalUrl())
                        || ext.getUrl()
                            .equals(PatientenrechnungStructDef.MARKING_FLAG.getCanonicalUrl()))
            .findFirst()
            .orElseThrow();
    chargeItem.getExtension().remove(markingFlagExtension);
    assertThrows(MissingFieldException.class, chargeItem::hasTaxOffice);
  }

  @Test
  void generateChargeItem() {
    val chargeItem = generateWithAccessCodeByFaker("AccessTestCode123456");
    val res = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(res.isSuccessful());
    assertTrue(
        chargeItem.getAccessCode().orElseThrow().getValue().contains("AccessTestCode123456"));
  }

  @Test
  void checkForAccessCodeShouldWork() {
    ErxChargeItem chargeItem = generateWithAccessCodeByFaker("AccessTestCode123456");
    assertTrue(chargeItem.getAccessCode().isPresent());
  }

  private ErxChargeItem generateWithAccessCodeByFaker(String code) {
    return ErxChargeItemBuilder.faker(new PrescriptionId("testIdForChargeItem123456789"))
        .accessCode(code)
        .build();
  }
}
