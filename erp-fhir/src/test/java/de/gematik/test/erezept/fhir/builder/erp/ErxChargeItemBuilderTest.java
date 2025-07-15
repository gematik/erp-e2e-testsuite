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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxChargeItemBuilderTest extends ErpFhirParsingTest {

  @Test
  void buildChargeItemFixedValues() {
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val kbvBundle = KbvErpBundleFaker.builder().fake();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.asPkv("X234567890"), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .markingFlag(false, false, true)
            .verordnung(kbvBundle)
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())) // concrete signed object won't be checked anyway
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildChargeItemFixedValuesWithoutMarkingFlags() {
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_209);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.randomPkv(), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .verordnung(UUID.randomUUID().toString())
            .abgabedatensatz(davBundle, "signed bundle".getBytes())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildChargeItemForPost() {
    // intentionally 160 because why not? should be allowed!
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_160);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.randomPkv(), GemFaker.insuranceName())
            .abgabedatensatz(davBundle, "signed bundle".getBytes())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldRemoveContainedResources() {
    val chargeItem =
        ErxChargeItemFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    chargeItem.removeContainedResources();
    assertFalse(chargeItem.hasContained());
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldRemoveAccessCode() {
    val chargeItem =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(PrescriptionId.random())
            .withAccessCode("123")
            .fake();
    assertTrue(chargeItem.getAccessCode().isPresent());
    chargeItem.removeAccessCode();
    assertFalse(chargeItem.getAccessCode().isPresent());

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build ErxChargeItem with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldSetPrescriptionIdSystemCorrect(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);

    val prescriptionId = PrescriptionId.random();
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.asPkv("X234567890"), GemFaker.insuranceName())
            .abgabedatensatz(davBundle, "signed bundle".getBytes())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Disabled("Not possible to activate now because refactorings in testsuites are required first!")
  @ParameterizedTest(
      name = "[{index}] -> Build ErxChargeItem for GKV KVNR with E-Rezept FHIR Profiles {0}")
  @MethodSource(
      "de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpFhirProfileVersions")
  @ClearSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE)
  void shouldNotAllowChargeItemsForGkvKVNRs(String erpFhirProfileVersion) {
    System.setProperty(ERP_FHIR_PROFILES_TOGGLE, erpFhirProfileVersion);

    val prescriptionId = PrescriptionId.random();
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();

    val builder =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.asGkv("X234567890"), GemFaker.insuranceName())
            .abgabedatensatz(davBundle, "signed bundle".getBytes());

    assertThrows(BuilderException.class, builder::build);
  }
}
