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

package de.gematik.test.erezept.screenplay.util;

import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ICommunicationType;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangedCommunication {

  @Nullable private final String communicationId;
  private final ICommunicationType<?> type;
  private final String senderName;
  private final String receiverName;

  public Optional<String> getCommunicationId() {
    return Optional.ofNullable(this.communicationId);
  }

  public static ExchangedCommunication.Builder from(String senderName) {
    return new Builder(senderName);
  }

  public static class Builder {
    private final String senderName;
    private String receiverName;

    private Builder(String senderName) {
      this.senderName = senderName;
    }

    public Builder to(String receiverName) {
      this.receiverName = receiverName;
      return this;
    }

    public ExchangedCommunication sent(ErxCommunication com) {
      return new ExchangedCommunication(
          com.getUnqualifiedId(), com.getType(), senderName, receiverName);
    }

    public ExchangedCommunication dispenseRequest() {
      return ofType(CommunicationType.DISP_REQ);
    }

    public ExchangedCommunication infoRequest() {
      return ofType(CommunicationType.INFO_REQ);
    }

    public <T extends ICommunicationType<?>> ExchangedCommunication ofType(T type) {
      return new ExchangedCommunication(null, type, senderName, receiverName);
    }
  }
}
