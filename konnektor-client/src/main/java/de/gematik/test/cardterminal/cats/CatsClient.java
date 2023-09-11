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

package de.gematik.test.cardterminal.cats;

import static java.text.MessageFormat.format;

import de.gematik.test.cardterminal.CardTerminalClient;
import de.gematik.test.cardterminal.cats.dto.CardConfigurationDto;
import de.gematik.test.cardterminal.cats.dto.CardStatusDto;
import de.gematik.test.cardterminal.exceptions.CardConfigurationException;
import de.gematik.test.cardterminal.exceptions.CardTerminalClientException;
import de.gematik.test.smartcard.Smartcard;
import java.util.concurrent.TimeUnit;
import kong.unirest.JsonObjectMapper;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CatsClient implements CardTerminalClient {

  private static final ObjectMapper OBJECT_MAPPER = new JsonObjectMapper();
  @Getter private final String ctId;
  @NonNull private final String address;

  @Override
  public void insertCard(Smartcard card, int slotId) {
    // deactivate card
    request(
        address + "/config/card/insert",
        OBJECT_MAPPER.writeValue(new CardStatusDto(slotId, false)));

    // change card configuration
    request(
        address + "/config/card/configuration",
        OBJECT_MAPPER.writeValue(new CardConfigurationDto(slotId, toCatsConfigurationPath(card))));

    // activate card
    request(
        address + "/config/card/insert", OBJECT_MAPPER.writeValue(new CardStatusDto(slotId, true)));
  }

  @SneakyThrows
  private void request(String address, String body) {
    log.debug(format("Request with Cats Client to Address {0} and Content {1}", address, body));
    // Workaround to prevent tls client authentication
    Unirest.config().verifySsl(false);
    val resp =
        Unirest.post(address)
            .body(body)
            .contentType("application/json")
            .asString()
            .ifFailure(CardConfigurationException::new)
            .ifSuccess(
                it ->
                    log.debug(format("Request successful {0}", it.getRequestSummary().asString())));
    // Workaround to prevent tls client authentication
    Unirest.config().verifySsl(true);
    if (resp.getStatus() != 200) {
      throw new CardTerminalClientException(address, body, resp);
    }

    TimeUnit.SECONDS.sleep(1);
  }

  private String toCatsConfigurationPath(Smartcard card) {
    return format(
        "/opt/cats-configuration/card-simulation/"
            + "CardSimulationConfigurations/configuration_{0}_{1}.xml",
        card.getType().toConfig(), card.getIccsn());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CatsClient that = (CatsClient) o;

    return ctId.equals(that.ctId);
  }

  @Override
  public int hashCode() {
    return ctId.hashCode();
  }

  @Override
  public String toString() {
    return "CatsClient{" + "ctId='" + ctId + '\'' + ", address='" + address + '\'' + '}';
  }
}
