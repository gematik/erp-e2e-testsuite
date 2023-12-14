/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ICommunicationType;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetReceivedCommunication implements Question<Optional<ErxCommunication>> {

  private final Actor sender;
  private final ICommunicationType<?> type;
  private final DequeStrategy dequeStrategy;

  @Override
  public Optional<ErxCommunication> answeredBy(Actor actor) {
    val senderId = getSenderId();
    val expected = getExpected(actor);
    val newMessages = actor.asksFor(DownloadNewMessages.fromServer());

    val mapped =
        newMessages.getCommunications().stream()
            .filter(com -> com.getType().equals(expected.getType()))
            .filter(com -> com.getSenderId().equals(senderId))
            .toList();

    if (mapped.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(dequeStrategy.chooseFrom(mapped));
    }
  }

  private ExchangedCommunication getExpected(Actor actor) {
    val expecting =
        SafeAbility.getAbility(actor, ManageCommunications.class).getExpectedCommunications();

    val filtered =
        expecting.getRawList().stream()
            .filter(com -> com.getType().equals(this.type))
            .filter(com -> com.getSenderName().equals(sender.getName()))
            .collect(Collectors.toList());

    if (filtered.isEmpty()) {
      throw new MissingPreconditionError(
          format(
              "{0} is not expecting any {1} Messages from {2}",
              actor.getName(), this.type, this.sender.getName()));
    } else {
      return dequeStrategy.chooseFrom(filtered);
    }
  }

  private String getSenderId() {
    String id;
    if (this.type.equals(CommunicationType.REPLY)
        || this.type.equals(ChargeItemCommunicationType.CHANGE_REPLY)) {
      id = SafeAbility.getAbility(sender, UseSMCB.class).getTelematikID();
    } else {
      id = SafeAbility.getAbility(sender, ProvidePatientBaseData.class).getKvnr().getValue();
    }
    return id;
  }

  public static Builder infoRequest() {
    return new Builder(CommunicationType.INFO_REQ);
  }

  public static Builder dispenseRequest() {
    return new Builder(CommunicationType.DISP_REQ);
  }

  public static Builder representative() {
    return new Builder(CommunicationType.REPRESENTATIVE);
  }

  public static Builder reply() {
    return new Builder(CommunicationType.REPLY);
  }

  public static class Builder {
    private final ICommunicationType<?> type;
    private DequeStrategy dequeStrategy;

    Builder(ICommunicationType<?> type) {
      this.type = type;
    }

    public Builder last() {
      return of(DequeStrategy.LIFO);
    }

    public Builder first() {
      return of(DequeStrategy.FIFO);
    }

    public Builder of(DequeStrategy order) {
      this.dequeStrategy = order;
      return this;
    }

    public GetReceivedCommunication from(Actor sender) {
      return new GetReceivedCommunication(sender, type, dequeStrategy);
    }
  }
}
