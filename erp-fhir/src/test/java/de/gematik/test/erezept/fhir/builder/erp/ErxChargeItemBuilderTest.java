/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErxChargeItemBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build ChargeItem using old profiles {0}")
  @ValueSource(booleans = {true, false})
  void buildChargeItemFixedValues(boolean oldProfiles) {
    val prescriptionId = PrescriptionId.random();
    val davBundle = DavAbgabedatenBuilder.faker(prescriptionId).build();
    val kbvBundle = KbvErpBundleBuilder.faker().build();
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
    val prescriptionId = PrescriptionId.random();
    val davBundle = DavAbgabedatenBuilder.faker(prescriptionId).build();
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
    val prescriptionId = PrescriptionId.random();
    val davBundle = DavAbgabedatenBuilder.faker(prescriptionId).build();

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
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    chargeItem.removeContainedResources();
    assertFalse(chargeItem.hasContained());

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldRemoveAccessCode() {
    val chargeItem =
        ErxChargeItemBuilder.faker(PrescriptionId.random())
            .version(
                PatientenrechnungVersion
                    .V1_0_0) // accesscode only when Patientrechnung 1.1.0 and above is set
            .accessCode("123")
            .build();
    assertTrue(chargeItem.isFromNewProfiles());
    assertTrue(chargeItem.getAccessCode().isPresent());
    chargeItem.removeAccessCode();
    assertFalse(chargeItem.getAccessCode().isPresent());

    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }
}
