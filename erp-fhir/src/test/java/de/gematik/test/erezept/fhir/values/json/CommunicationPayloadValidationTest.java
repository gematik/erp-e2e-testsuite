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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.networknt.schema.Error;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCodeNCPeH;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class CommunicationPayloadValidationTest {

  private static CommunicationPayloadValidation communicationDspReqValidator;

  @BeforeAll
  static void setup() {
    communicationDspReqValidator =
        new CommunicationPayloadValidation("erpcom/CommunicationDispReqPayloadV3.json");
  }

  @Test
  void shouldValidateCorrect() {
    val builder = messageBuilderForOrder("hint er her", "text ");

    val result = builder.build();
    assertTrue(communicationDspReqValidator.isValid(result));
  }

  private void logIfFail(List<Error> errors) {
    errors.forEach(e -> log.info(e.getMessage()));
  }

  @Test
  void shouldValidateCorrectWithPrintoutAndFail() {
    val builder =
        messageBuilderForOrder("hint", "text").communicationType(CommunicationPayloadType.TEXT);

    val resource = builder.build();
    val result = communicationDspReqValidator.validate(resource);
    logIfFail(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void shouldValidateCorrectWithPrintout() {
    val builder = messageBuilderForOrder("hint", "text");

    val comReply = builder.build();
    assertTrue(communicationDspReqValidator.isValid(comReply));
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
    builder.address(GemFaker.fakerStreetName());
    builder.firstname(GemFaker.fakerFirstName());
    builder.lastname(GemFaker.fakerLastName());
    builder.postcode(GemFaker.fakerZipCode());
    builder.city(GemFaker.fakerCity());
    builder.country(randomElement(IsoCountryCodeNCPeH.values()).getCode());
    builder.phone(fakePhoneNumberWithStartingDoubleOAsE164());
    builder.email(GemFaker.fakerEMail());
    builder.transactionID(UUID.randomUUID());
    return builder;
  }
}
