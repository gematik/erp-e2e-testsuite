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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErxChargeItemBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build ChargeItem using old profiles {0}")
  @ValueSource(booleans = {true, false})
  void buildChargeItemFixedValues(boolean oldProfiles) {
    val prescriptionIdSystem =
        oldProfiles
            ? ErpWorkflowNamingSystem.PRESCRIPTION_ID
            : ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    val prescriptionId = PrescriptionId.random(prescriptionIdSystem);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val kbvBundle = KbvErpBundleFaker.builder().fake();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItemBuilder =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.from("X234567890"), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .markingFlag(false, false, true)
            .verordnung(kbvBundle)
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())); // concrete signed object won't be checked anyway

    if (!oldProfiles) chargeItemBuilder.version(PatientenrechnungVersion.V1_0_0);
    val chargeItem = chargeItemBuilder.build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build ChargeItem using old profiles {0}")
  @ValueSource(booleans = {true, false})
  void buildChargeItemFixedValuesWithoutMarkingFlags(boolean oldProfiles) {
    val prescriptionIdSystem =
        oldProfiles
            ? ErpWorkflowNamingSystem.PRESCRIPTION_ID
            : ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    val prescriptionId = PrescriptionId.random(prescriptionIdSystem);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    val erxReceipt = mock(ErxReceipt.class);
    when(erxReceipt.getId()).thenReturn("Bundle/12345");

    val chargeItemBuilder =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .accessCode(AccessCode.random().getValue())
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.from("X234567890"), GemFaker.insuranceName())
            .receipt(erxReceipt)
            .verordnung(UUID.randomUUID().toString())
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())); // concrete signed object won't be checked anyway

    if (!oldProfiles) chargeItemBuilder.version(PatientenrechnungVersion.V1_0_0);
    val chargeItem = chargeItemBuilder.build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> Build ChargeItem using old profiles {0}")
  @ValueSource(booleans = {true, false})
  void buildChargeItemForPost(boolean oldProfiles) {
    val prescriptionIdSystem =
        oldProfiles
            ? ErpWorkflowNamingSystem.PRESCRIPTION_ID
            : ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    val prescriptionId = PrescriptionId.random(prescriptionIdSystem);
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();

    val chargeItemBuilder =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .status("billable")
            .enterer("606358757")
            .subject(KVNR.from("X234567890"), GemFaker.insuranceName())
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())); // concrete signed object won't be checked anyway

    if (!oldProfiles) chargeItemBuilder.version(PatientenrechnungVersion.V1_0_0);
    val chargeItem = chargeItemBuilder.build();

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
            .withVersion(
                PatientenrechnungVersion
                    .V1_0_0) // accesscode only when Patientrechnung 1.1.0 and above is set
            .withAccessCode("123")
            .fake();
    assertTrue(chargeItem.isFromNewProfiles());
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
            .subject(KVNR.from("X234567890"), GemFaker.insuranceName())
            .abgabedatensatz(
                davBundle,
                (b -> "helloworld".getBytes())) // concrete signed object won't be checked anyway
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }
}
