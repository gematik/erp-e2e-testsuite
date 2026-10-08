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

package de.gematik.test.erezept.fhir.values.json;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakePhoneNumberWithStartingDoubleOAsE164;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCodeNCPeH;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class CommunicationDisReqMessageTest {

  @Test
  void shouldBuildV1CommunicationDisReqMessageCorrectly() {
    val msg =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(SupplyOptionsType.ON_PREMISE)
            .name("John Doe")
            .addressLines(List.of("Street 1"))
            .phone("12345")
            .hint("hint")
            .build();

    assertEquals(1, msg.version());
    assertEquals(SupplyOptionsType.ON_PREMISE.getLabel(), msg.supplyOptionsType());

    assertEquals("John Doe", msg.name());
    assertEquals(List.of("Street 1"), msg.addressLines());

    assertEquals("12345", msg.phone());
    assertEquals("hint", msg.hint());
  }

  @Test
  void shouldBuildV3CommunicationDisReqMessageCorrectly() {
    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType(CommunicationPayloadType.ORDER)
            .supplyOptionsType(SupplyOptionsType.DELIVERY)
            .firstname("first")
            .lastname("last")
            .address("street")
            .postcode("12345")
            .city("city")
            .country("country")
            .phone("phone")
            .email("email@test.com")
            .text("text")
            .hint("hint")
            .build();

    assertEquals(3, msg.version());

    assertEquals("order", msg.communicationType());
    assertEquals(SupplyOptionsType.DELIVERY.getLabel(), msg.supplyOptionsType());

    assertEquals("first", msg.firstname());
    assertEquals("last", msg.lastname());

    assertEquals("street", msg.address());
    assertEquals("12345", msg.postcode());
    assertEquals("city", msg.city());
    assertEquals("country", msg.country());

    assertEquals("phone", msg.phone());
    assertEquals("email@test.com", msg.email());
    assertEquals("text", msg.text());
    assertEquals("hint", msg.hint());
  }

  @Test
  void shouldCreateConvenienceConstructor() {
    val msg =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(SupplyOptionsType.SHIPMENT)
            .hint("info")
            .build();

    assertEquals(1, msg.version());
    assertEquals(SupplyOptionsType.SHIPMENT.getLabel(), msg.supplyOptionsType());

    assertEquals("info", msg.hint());
  }

  @Test
  void shouldSetMessage() {
    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType(CommunicationPayloadType.ORDER)
            .build();

    assertNotNull(msg);

    assertEquals(3, msg.version());
    assertNotNull(msg.supplyOptionsType());
    assertEquals("order", msg.communicationType());
  }

  @Test
  void shouldBuildV1BuilderCorrectly() {
    val msg =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(SupplyOptionsType.ON_PREMISE)
            .name("Max Mustermann")
            .phone("999")
            .hint("test")
            .build();

    assertEquals(1, msg.version());
    assertEquals("Max Mustermann", msg.name());
    assertEquals("999", msg.phone());
  }

  @Test
  void shouldBuildV3BuilderCorrectly() {
    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType("order")
            .firstname("Max")
            .lastname("Mustermann")
            .address("street")
            .postcode("12345")
            .city("Berlin")
            .country("DE")
            .phone("999")
            .email("a@b.com")
            .text("hello")
            .hint("hint")
            .build();

    assertEquals(3, msg.version());
    assertEquals("Max", msg.firstname());
    assertEquals("Berlin", msg.city());
    assertEquals("order", msg.communicationType());
  }

  @Test
  void shouldBuildV1WithPickupCodes() {
    val msg =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(SupplyOptionsType.ON_PREMISE)
            .name("John Doe")
            .phone("12345")
            .hint("hint")
            .pickUpCodeHR("HR-123")
            .pickUpCodeDMC("DMC-456")
            .build();

    assertEquals(1, msg.version());

    assertEquals("HR-123", msg.pickUpCodeHR());
    assertEquals("DMC-456", msg.pickUpCodeDMC());
  }

  @Test
  void shouldBuildV3WithPickupCodes() {
    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType("order")
            .firstname("Max")
            .lastname("Mustermann")
            .address("street")
            .postcode("12345")
            .city("Berlin")
            .country("DE")
            .phone("999")
            .email("a@b.com")
            .text("hello")
            .hint("hint")
            .pickupCodeHR("HR-999")
            .pickupCodeDMC("DMC-888")
            .build();

    assertEquals(3, msg.version());

    assertEquals("HR-999", msg.pickupCodeHR());
    assertEquals("DMC-888", msg.pickupCodeDMC());
  }

  @Test
  void shouldCoverV1BuilderOverloads() {
    val msg =
        CommunicationDisReqMessage.forV1()
            .version(1)
            .supplyOptionsType("delivery")
            .name("Max Mustermann")
            .addressLines("Line1", "Line2")
            .phone("123")
            .hint("hint")
            .pickUpCodeHR("HR1")
            .pickUpCodeDMC("DMC1")
            .build();

    assertEquals(1, msg.version());
    assertEquals("delivery", msg.supplyOptionsType());
    assertEquals(List.of("Line1", "Line2"), msg.addressLines());
    assertEquals("HR1", msg.pickUpCodeHR());
    assertEquals("DMC1", msg.pickUpCodeDMC());
  }

  @Test
  void shouldBuildV3WithOptionalFields() {
    UUID id = UUID.randomUUID();

    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType("order")
            .transactionID(id)
            .supplyOptionsType("delivery")
            .url("https://test.de")
            .pickupCodeHR("HR")
            .pickupCodeDMC("DMC")
            .build();

    assertEquals(3, msg.version());
    assertEquals("order", msg.communicationType());
    assertEquals(id.toString(), msg.transactionID());
    assertEquals("https://test.de", msg.url());
    assertEquals("HR", msg.pickupCodeHR());
    assertEquals("DMC", msg.pickupCodeDMC());
  }

  @RepeatedTest(5)
  void shouldValidateV3PayloadAgainstSchema() {
    val builder = messageBuilderForOrder("hint", "text");
    val isValid = shouldValidate(builder.build());
    assertTrue(isValid);
  }

  @RepeatedTest(5)
  void shouldValidateV3PayloadForMessageAgainstSchema() {
    val communicationDisReqMessagePayload = messageBuilderForText("text").build();
    val isValid = shouldValidate(communicationDisReqMessagePayload);
    assertTrue(isValid);
  }

  public CommunicationDisReqMessage.CommunicationDisReqMessageV3Builder messageBuilderForText(
      @NonNull String text) {
    val builder = getPrefilledBuilder();
    builder.communicationType(CommunicationPayloadType.TEXT.getLabel());
    builder.text(text);

    return builder;
  }

  private CommunicationDisReqMessage.CommunicationDisReqMessageV3Builder messageBuilderForOrder(
      String hint, String text) {
    val builder = getPrefilledBuilder();
    builder.supplyOptionsType(GemFaker.fakerValueSet(SupplyOptionsType.class));
    builder.communicationType(CommunicationPayloadType.ORDER.getLabel());
    Optional.ofNullable(hint).ifPresent(builder::hint);
    Optional.ofNullable(text).ifPresent(builder::text);

    return builder;
  }

  private CommunicationDisReqMessage.CommunicationDisReqMessageV3Builder getPrefilledBuilder() {
    val builder = new CommunicationDisReqMessage.CommunicationDisReqMessageV3Builder();
    builder.address("this.street");
    builder.firstname("Max");
    builder.lastname("Mustermann");
    builder.postcode("12345");
    builder.city("Berlin");
    builder.country(randomElement(IsoCountryCodeNCPeH.values()).getCode());
    builder.phone(fakePhoneNumberWithStartingDoubleOAsE164());
    builder.email(GemFaker.fakerEMail());
    builder.transactionID(UUID.randomUUID());
    return builder;
  }

  private static CommunicationPayloadValidation communicationDispenseRequestValidator;

  @BeforeAll
  static void setup() {
    communicationDispenseRequestValidator =
        new CommunicationPayloadValidation("erpcom/CommunicationDispReqPayloadV3.json");
  }

  public boolean shouldValidate(Object toValidate) {
    return communicationDispenseRequestValidator.validate(toValidate).isEmpty();
  }
}
