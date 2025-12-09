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

package de.gematik.test.cardterminal.cats;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.test.cardterminal.CardTerminalClient;
import de.gematik.test.cardterminal.cats.dto.CardConfigurationDto;
import de.gematik.test.cardterminal.cats.dto.CardStatusDto;
import de.gematik.test.cardterminal.exceptions.CardConfigurationException;
import de.gematik.test.cardterminal.exceptions.CardTerminalClientException;
import de.gematik.test.erezept.config.dto.konnektor.CardTerminalClientConfiguration;
import kong.unirest.core.HttpRequestWithBody;
import kong.unirest.core.Unirest;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor
@Slf4j
public class CatsClient implements CardTerminalClient {

  private static final ObjectMapper OBJECT_MAPPER = new JsonMapper();
  @Getter private final String ctId;
  @NonNull private final String address;

  public CatsClient(CardTerminalClientConfiguration config) {
    this(config.ctId(), config.url());
  }

  @Override
  @SneakyThrows
  public void insertCard(Smartcard card, int slotId) {
    val restClient = Unirest.spawnInstance();
    restClient.config().verifySsl(false);
    request(
        restClient.put(format("{0}/config/card/slot/{1}", address, slotId)),
        OBJECT_MAPPER.writeValueAsString(new CardConfigurationDto(toCatsConfigurationPath(card))));

    request(
        restClient.post(address + "/config/card/insert"),
        OBJECT_MAPPER.writeValueAsString(
            new CardStatusDto(slotId, true, toCatsConfigurationPath(card), false)));
  }

  @SneakyThrows
  private void request(HttpRequestWithBody req, String body) {
    log.debug(format("Request with Cats Client to Address {0} and Content {1}", address, body));
    val resp =
        req.contentType("application/json")
            .body(body)
            .asString()
            .ifFailure(CardConfigurationException::new)
            .ifSuccess(
                it ->
                    log.debug(format("Request successful {0}", it.getRequestSummary().asString())));
    if (resp.getStatus() != 200) {
      log.error(
          format(
              "Unexpected Response from CATS: status: {0} body {1}",
              resp.getStatus(), resp.getBody()));
      throw new CardTerminalClientException(address, body, resp);
    }
  }

  private String toCatsConfigurationPath(Smartcard card) {
    val configTypeName = card.getType().name().toLowerCase().replace("-", "_");
    return format(
        "/opt/cats-configuration/card-simulation/"
            + "CardSimulationConfigurations/configuration_{0}_{1}.xml",
        configTypeName, card.getIccsn());
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
