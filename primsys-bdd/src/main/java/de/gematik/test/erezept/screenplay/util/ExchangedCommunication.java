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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ICommunicationType;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangedCommunication {

  @Nullable private final String communicationId;
  private final PrescriptionId basedOn;
  private final ICommunicationType<?> type;
  private final String senderName;
  private final String senderId;
  private final String receiverName;
  private final String receiverId;

  public Optional<String> getCommunicationId() {
    return Optional.ofNullable(this.communicationId);
  }

  public static ExchangedCommunication.ErxCommunicationBuilder from(ErxCommunication com) {
    return new ErxCommunicationBuilder(com);
  }

  public static ExchangedCommunication.FdvCommunicationBuilder sentBy(Actor fdvPatient) {
    val egkAbility = SafeAbility.getAbility(fdvPatient, ProvideEGK.class);
    return sentBy(fdvPatient.getName(), egkAbility);
  }

  public static ExchangedCommunication.FdvCommunicationBuilder sentBy(
      String senderActorName, ProvideEGK egkAbility) {
    return sentBy(senderActorName, egkAbility.getKvnr());
  }

  public static ExchangedCommunication.FdvCommunicationBuilder sentBy(
      String senderActorName, KVNR kvnr) {
    return new FdvCommunicationBuilder(senderActorName, kvnr.getValue());
  }

  /**
   * Builder for ExchangedCommunication when based on an ErxCommunication. This provides a shortcut
   * because the concrete ID of the ErxCommunication is known.
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ErxCommunicationBuilder {
    private final ErxCommunication com;

    public ExchangedCommunication withActorNames(Actor sender, Actor receiver) {
      return withActorNames(sender.getName(), receiver.getName());
    }

    public ExchangedCommunication withActorNames(String senderName, String receiverName) {
      val prescriptionId = com.getBasedOnReferenceId().toPrescriptionId();
      return new ExchangedCommunication(
          com.getUnqualifiedId(),
          prescriptionId,
          com.getType(),
          senderName,
          com.getSenderId(),
          receiverName,
          com.getRecipientId());
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class FdvCommunicationBuilder {
    private final String senderName;
    private final String senderId;
    private String receiverName;
    private String receiverId;

    public FdvCommunicationBuilder to(Actor receiverPharmacy) {
      this.receiverName = receiverPharmacy.getName();
      this.receiverId = SafeAbility.getAbility(receiverPharmacy, UseSMCB.class).getTelematikID();
      return this;
    }

    public ExchangedCommunication dispenseRequestBasedOn(PrescriptionId prescriptionId) {
      return new ExchangedCommunication(
          null,
          prescriptionId,
          CommunicationType.DISP_REQ,
          senderName,
          senderId,
          receiverName,
          receiverId);
    }
  }
}
