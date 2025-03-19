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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPrescriptionId;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.Date;
import java.util.UUID;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
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
  void buildFakeChargeItemWithAccessCode() {
    val chargeItem = ErxChargeItemFaker.builder().withAccessCode(AccessCode.random()).fake();
    val chargeItem2 =
        ErxChargeItemFaker.builder().withAccessCode(AccessCode.random().getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithStatus() {
    val chargeItem =
        ErxChargeItemFaker.builder().withStatus(ChargeItem.ChargeItemStatus.BILLABLE).fake();
    val chargeItem2 =
        ErxChargeItemFaker.builder()
            .withStatus(ChargeItem.ChargeItemStatus.BILLABLE.toCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithSubject() {
    val chargeItem = ErxChargeItemFaker.builder().withSubject(KVNR.random(), fakerName()).fake();
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
    val chargeItem = ErxChargeItemFaker.builder().withEnterer(telematikId).fake();
    val chargeItem2 = ErxChargeItemFaker.builder().withEnterer(telematikId.toString()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithEnteredDate() {
    val chargeItem = ErxChargeItemFaker.builder().withEnteredDate(new Date()).fake();
    val chargeItem2 =
        ErxChargeItemFaker.builder()
            .withEnteredDate(new Date(), TemporalPrecisionEnum.SECOND)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeChargeItemWithMarkingFlag() {
    val markingFlag = MarkingFlag.with(true, false, false);
    val chargeItem = ErxChargeItemFaker.builder().withMarkingFlag(markingFlag).fake();
    val chargeItem2 = ErxChargeItemFaker.builder().withMarkingFlag(true, false, false).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, chargeItem);
    val result2 = ValidatorUtil.encodeAndValidate(parser, chargeItem2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
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
    val davBundle = DavPkvAbgabedatenFaker.builder(fakerPrescriptionId()).fake();
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
