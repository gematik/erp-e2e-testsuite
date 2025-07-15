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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class ExchangedCommunicationTest extends ErpFhirBuildingTest {

  @Test
  void shouldBuildExchangedCommunicationCorrectly() {
    val sender = new Actor("Marty McFly");
    val receiver = new Actor("Doc Brown");
    val com =
        ErxCommunicationBuilder.asReply(new CommunicationReplyMessage())
            .basedOn("123")
            .receiver("X123456789")
            .sender("TelematikId")
            .build();
    val exc = ExchangedCommunication.from(com).withActorNames(sender, receiver);

    assertEquals("Marty McFly", exc.getSenderName());
    assertEquals("TelematikId", exc.getSenderId());
    assertEquals("Doc Brown", exc.getReceiverName());
    assertEquals("X123456789", exc.getReceiverId());
    assertEquals("123", exc.getBasedOn().getValue());
    assertEquals(com.getUnqualifiedId(), exc.getCommunicationId().orElseThrow());
  }

  @Test
  void shouldBuildExchangedCommunicationWithoutKnownCommunicationId() {
    val sca = SmartcardArchive.fromResources();

    val sender = new Actor("Marty McFly");
    val senderEgk = sca.getEgk(0);
    sender.can(ProvideEGK.heOwns(senderEgk));
    val receiver = new Actor("Doc Brown");
    val receiverSmcb = sca.getSmcB(0);
    receiver.can(UseSMCB.itHasAccessTo(receiverSmcb));
    val basedOn = PrescriptionId.random();

    val exc = ExchangedCommunication.sentBy(sender).to(receiver).dispenseRequestBasedOn(basedOn);

    assertEquals("Marty McFly", exc.getSenderName());
    assertEquals(senderEgk.getKvnr(), exc.getSenderId());
    assertEquals("Doc Brown", exc.getReceiverName());
    assertEquals(receiverSmcb.getTelematikId(), exc.getReceiverId());
    assertEquals(basedOn.getValue(), exc.getBasedOn().getValue());
    assertTrue(exc.getCommunicationId().isEmpty());
  }
}
