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

package de.gematik.test.konnektor.cfg;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorImpl;
import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.Data;
import lombok.val;

@Data
public class LocalKonnektorConfiguration extends KonnektorConfiguration {

  public LocalKonnektorConfiguration() {
    this.setType(KonnektorType.LOCAL);
  }

  private ContextType getDefaultContextType() {
    val ctx = new ContextType();
    ctx.setMandantId("Mandant1");
    ctx.setClientSystemId("CS1");
    ctx.setWorkplaceId("WP1");
    return ctx;
  }

  @Override
  public Konnektor create() {
    val smartcards = SmartcardFactory.getArchive();
    val ctx = getDefaultContextType();
    val serviceProvider = new MockKonnektorServiceProvider(smartcards);
    return new KonnektorImpl(ctx, this.getName(), KonnektorType.LOCAL, serviceProvider);
  }

  public static Konnektor createMock() {
    return new LocalKonnektorConfiguration().create();
  }

  @Override
  public String toString() {
    return format("Local Mock Konnektor {0}", this.getName());
  }
}
