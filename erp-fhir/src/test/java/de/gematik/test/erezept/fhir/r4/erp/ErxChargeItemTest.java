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
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class ErxChargeItemTest extends ErpFhirParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";
  private final String BASE_PATH_120 = "fhir/valid/erp/1.2.0/";

  @Test
  void shouldEncodeValidErxChargeItems() {
    val rawFiles = List.of("ChargeItem_01.xml", "ChargeItem_01.json");

    rawFiles.forEach(
        fileName -> {
          val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
          val chargeItem = parser.decode(ErxChargeItem.class, content);
          assertNotNull(chargeItem, "Valid ErxMedicationDispense must be parseable");
          assertFalse(chargeItem.hasInsuranceProvider());
          assertFalse(chargeItem.hasSubsidy());
          assertFalse(chargeItem.hasTaxOffice());
          assertEquals("160.123.456.789.123.58", chargeItem.getPrescriptionId().getValue());
          assertEquals(ChargeItem.ChargeItemStatus.BILLABLE, chargeItem.getStatus());
          assertEquals("X234567890", chargeItem.getSubjectKvnr().getValue());
          assertEquals("606358757", chargeItem.getEntererTelematikId().getValue());
          assertFalse(chargeItem.isFromNewProfiles());
        });
  }

  @Test
  void shouldEncodeValidErxChargeItemsAsResource() {
    val rawFiles = List.of("ChargeItem_01.xml", "ChargeItem_01.json");

    rawFiles.forEach(
        fileName -> {
          val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
          val chargeItem = parser.decode(content);
          assertNotNull(chargeItem, "Valid ChargeItem must be parseable");
          assertEquals(ResourceType.ChargeItem, chargeItem.getResourceType());
          assertEquals(ErxChargeItem.class, chargeItem.getClass());
        });
  }

  @Test
  @SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.2.0")
  void shouldFindNewProfilePrescriptionId() {
    val content = ResourceLoader.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);
    // change system of the prescription id
    chargeItem.getIdentifier().stream()
        .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
        .forEach(
            identifier ->
                identifier.setSystem(
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID_121.getCanonicalUrl()));
    assertEquals(
        new PrescriptionId(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121, "160.123.456.789.123.58"),
        chargeItem.getPrescriptionId());
  }

  @Test
  void shouldThrowOnMissingFields() {
    val content = ResourceLoader.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
    val chargeItem = parser.decode(ErxChargeItem.class, content);

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
    val prescriptionId = PrescriptionId.random();
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val kbvBundle = KbvErpBundleFaker.builder().fake();
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
    val abgabeReference = new AbgabedatensatzReference(UUID.randomUUID().toString());
    val changedChargeItem =
        chargeItem.withChangedContainedBinaryData(abgabeReference, changedResource.getBytes());
    assertEquals(changedResource, new String(changedChargeItem.getContainedBinaryData()));

    val vr = ValidatorUtil.encodeAndValidate(parser, changedChargeItem);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldFindNewProfileMarkingFlags() {
    val content = ResourceLoader.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
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
    val content = ResourceLoader.readFileFromResource(BASE_PATH + "ChargeItem_01.xml");
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
    return ErxChargeItemFaker.builder()
        .withPrescriptionId(new PrescriptionId("testIdForChargeItem123456789"))
        .withAccessCode(code)
        .fake();
  }
}
