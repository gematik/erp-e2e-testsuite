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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxChargeItemFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakeChargeItemWithVersion() {
    val chargeItem =
        ErxChargeItemFaker.builder().withVersion(PatientenrechnungVersion.V1_0_0).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFakeChargeItemWithCustomAccessCode01() {
    val chargeItem = ErxChargeItemFaker.builder().withAccessCode(AccessCode.random()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
    assertTrue(chargeItem.getAccessCode().isPresent());
  }

  @Test
  void shouldFakeChargeItemWithCustomAccessCode02() {
    val chargeItem = ErxChargeItemFaker.builder().withAccessCode("AccessTestCode123456").fake();
    val res = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(res.isSuccessful());
    assertTrue(chargeItem.getAccessCode().isPresent());
    val accessCode = chargeItem.getAccessCode().get();
    assertEquals("AccessTestCode123456", accessCode.getValue());
  }

  @Test
  void buildFakeChargeItemWithStatus() {
    val chargeItem = ErxChargeItemFaker.builder().withStatus("billable").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithSubject() {
    val chargeItem = ErxChargeItemFaker.builder().withSubject(KVNR.randomPkv(), fakerName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithReceipt() {
    val erxReceipt = new ErxReceipt();
    erxReceipt.setId("12345");
    val chargeItem = ErxChargeItemFaker.builder().withReceipt(erxReceipt).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithEnterer() {
    val telematikId = mock(TelematikID.class);
    val chargeItem = ErxChargeItemFaker.builder().withEnterer(telematikId.getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithEnteredDate() {
    val chargeItem = ErxChargeItemFaker.builder().withEnteredDate(new Date()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithMarkingFlag() {
    val chargeItem = ErxChargeItemFaker.builder().withMarkingFlag(true, false, false).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithVerordnung() {
    val kbvBundle = KbvErpBundleFaker.builder().fake();
    val chargeItem = ErxChargeItemFaker.builder().withVerordnung(kbvBundle.asReference()).fake();
    val chargeItem2 = ErxChargeItemFaker.builder().withVerordnung(kbvBundle).fake();
    val chargeItem3 = ErxChargeItemFaker.builder().withVerordnung(kbvBundle.getLogicalId()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    val result3 = ValidatorUtil.encodeAndValidate(parser, chargeItem3);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
    assertTrue(result3.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithAbgabedatensatz() {
    val davBundle = DavPkvAbgabedatenFaker.builder(PrescriptionId.random()).fake();
    val chargeItem =
        ErxChargeItemFaker.builder()
            .withAbgabedatensatz(davBundle, b -> "helloworld".getBytes())
            .fake();
    val chargeItem2 =
        ErxChargeItemFaker.builder()
            .withAbgabedatensatz(UUID.randomUUID().toString(), "faked binary content".getBytes())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }
}
