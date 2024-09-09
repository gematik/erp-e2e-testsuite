/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.konnektor;

import static java.text.MessageFormat.*;

import de.gematik.test.cardterminal.*;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorContextConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorType;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.konnektor.commands.*;
import de.gematik.test.konnektor.exceptions.*;
import de.gematik.test.konnektor.profile.*;
import de.gematik.test.konnektor.soap.*;
import de.gematik.ws.conn.connectorcontext.v2.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public class KonnektorImpl implements Konnektor {

  private final ContextType ctx;
  @Getter private final String name;
  @Getter private final KonnektorType type;
  protected final ServicePortProvider serviceProvider;
  @Getter private final CardTerminalManager cardTerminalManager;

  public KonnektorImpl(
      ContextType ctx,
      String name,
      KonnektorType type,
      ServicePortProvider serviceProvider,
      Collection<CardTerminalClient> cardTerminalClients) {
    this.name = name;
    this.ctx = ctx;
    this.type = type;
    this.serviceProvider = serviceProvider;
    this.cardTerminalManager = new CardTerminalManager(this, cardTerminalClients);
  }

  public KonnektorImpl(
      KonnektorContextConfiguration ctxConfig,
      String name,
      KonnektorType type,
      ServicePortProvider serviceProvider,
      CardTerminalClient... cardTerminalClients) {
    this(KonnektorFactory.fromConfig(ctxConfig), name, type, serviceProvider, cardTerminalClients);
  }

  public KonnektorImpl(
      ContextType ctx,
      String name,
      KonnektorType type,
      ServicePortProvider serviceProvider,
      CardTerminalClient... cardTerminalClients) {
    this(
        ctx,
        name,
        type,
        serviceProvider,
        Arrays.stream(cardTerminalClients).collect(Collectors.toSet()));
  }

  @Override
  public final ProfileType getProfileType() {
    return serviceProvider.getType();
  }

  @Override
  public final <R> KonnektorResponse<R> execute(KonnektorCommand<R> cmd) {
    log.info(format("Execute {0} on {1}", cmd.getClass().getSimpleName(), this));
    val start = Instant.now();
    val response = cmd.execute(ctx, serviceProvider);
    val duration = Duration.between(start, Instant.now());
    log.trace(
        format(
            "Received Response for {0} from {1} within {2}",
            cmd.getClass().getSimpleName(), this, duration.toMillis()));
    return new KonnektorResponse<>(response, duration);
  }

  @Override
  public final <R> Optional<KonnektorResponse<R>> safeExecute(KonnektorCommand<R> cmd) {
    try {
      return Optional.of(execute(cmd));
    } catch (SOAPRequestException sre) {
      log.warn(
          format(
              "Execute {0} produced an error: {1}",
              cmd.getClass().getSimpleName(), sre.getMessage()));
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    val ctxString =
        format(
            "ctx=[clientSystem={0}, mandant={1}, wp={2}, user={3}]",
            ctx.getClientSystemId(), ctx.getMandantId(), ctx.getWorkplaceId(), ctx.getUserId());
    return format(
        "Konnektor \"{0}\" with {1} and profile {2}",
        this.getName(), ctxString, this.serviceProvider);
  }
}
