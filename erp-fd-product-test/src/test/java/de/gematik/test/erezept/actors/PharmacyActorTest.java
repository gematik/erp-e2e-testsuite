/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.json.CommunicationPayloadValidation;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage.PaymentMethod;
import de.gematik.test.erezept.fhir.valuesets.DeliveryStatus;
import de.gematik.test.erezept.fhir.valuesets.ReadyForCollecting;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PharmacyActorTest {

  private static CommunicationPayloadValidation communicationReplyValidator;

  @BeforeAll
  static void setup() {
    communicationReplyValidator =
        new CommunicationPayloadValidation("erpcom/CommunicationReplyPayloadV3.json");
  }

  @Test
  void shouldProvideCorrectData() {
    StopwatchProvider.init();
    val config = ErpFdTestsuiteFactory.create();
    val pharmacy = new PharmacyActor("Am Flughafen");
    val pharmacyConfig = config.getPharmacyConfig(pharmacy.getName());
    val smcb = config.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    pharmacy.can(useSmcb);
    assertNotNull(pharmacy.getCommonName());
    assertNotNull(pharmacy.getTelematikId());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerText() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val testText = GemFaker.fakerCommunicationReplyMessage();
    val payload = pharmacy.communicationReplayFakerText(testText).build();
    assertNotNull(payload);
    assertEquals(testText, payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerText() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy
            .communicationReplayFakerText("testText")
            .readyForCollecting(ReadyForCollecting.NEXT_DAY_PM.getCode())
            .build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerLink() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerLink("testText", "https://test.de").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    assertEquals("https://test.de", payload.url());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerLink() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerLink("testText", "invalidUrl").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerPickupCodeHR() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerPickupCodeHR(12345678, "testText").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    assertEquals("12345678", payload.pickupCodeHR());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerPickupCodeHR() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerPickupCodeHR(123456789, "testText").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerPickupCodeDMC() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerPickupCodeDMC(12345678, "testText").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    assertEquals("12345678", payload.pickupCodeDMC());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerPickupCodeDMC() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload = pharmacy.communicationReplayFakerPickupCodeDMC(1234567, "testText").build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerDeliveryStatus() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy
            .communicationReplayFakerDeliveryStatus("testText", DeliveryStatus.IN_TRANSPORT)
            .build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    assertEquals(DeliveryStatus.IN_TRANSPORT.getCode(), payload.deliveryStatus());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerDeliveryStatus() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy
            .communicationReplayFakerDeliveryStatus("testText", null)
            .deliveryStatus("invalid")
            .build();
    assertNotNull(payload);
    assertNotNull(payload.deliveryStatus());
    assertEquals("testText", payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerPaymentInfo() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val paymentMethods = List.of(new PaymentMethod("cash", null, null));
    val payload =
        pharmacy.communicationReplayFakerPaymentInfo("testText", 1234, paymentMethods).build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());
    assertEquals(1234, payload.totalAmount());
    assertEquals(paymentMethods, payload.paymentMethods());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerPaymentInfo() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy
            .communicationReplayFakerPaymentInfo(
                "testText", 1234, List.of(new PaymentMethod("invalid", null, null)))
            .build();
    assertNotNull(payload);
    assertEquals("testText", payload.text());

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndValidateCorrectForComFakerReservationStatus() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy.communicationReplayFakerReservationStatus(ReadyForCollecting.NEXT_DAY_PM).build();
    assertNotNull(payload);
    assertEquals(ReadyForCollecting.NEXT_DAY_PM.getCode(), payload.readyForCollection());

    val validResult = communicationReplyValidator.validate(payload);
    assertTrue(validResult.isEmpty());
  }

  @Test
  void shouldBuildAndDetectFailureForComFakerReservationStatus() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val payload =
        pharmacy
            .communicationReplayFakerReservationStatus(ReadyForCollecting.NEXT_DAY_PM)
            .readyForCollecting("invalid")
            .build();
    assertNotNull(payload);

    val validResult = communicationReplyValidator.validate(payload);
    assertFalse(validResult.isEmpty());
  }
}
