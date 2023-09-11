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

package de.gematik.test.erezept.primsys.model.actor;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.cfg.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.primsys.rest.data.*;
import de.gematik.test.smartcard.*;
import java.math.*;
import java.nio.charset.*;
import java.security.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

@Getter
public abstract class BaseActor {

  private final String name;
  private final String identifier;
  private final ErpClient client;
  private final ActorRole role;
  private final SmcB smcb;

  protected BaseActor(DoctorConfiguration cfg, EnvironmentConfiguration env, SmartcardArchive sca) {
    this.name = cfg.getName();
    this.identifier = createIdentifier(this.name);
    this.role = ActorRole.DOCTOR;
    this.smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());

    this.client = ErpClientFactory.createErpClient(env, cfg);
    this.client.authenticateWith(smcb);
  }

  protected BaseActor(
      PharmacyConfiguration cfg, EnvironmentConfiguration env, SmartcardArchive sca) {
    this.name = cfg.getName();
    this.identifier = createIdentifier(this.name);
    this.role = ActorRole.PHARMACY;
    this.smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());

    this.client = ErpClientFactory.createErpClient(env, cfg);
    this.client.authenticateWith(smcb);
  }

  public final <R extends Resource> ErpResponse<R> erpRequest(final ICommand<R> command) {
    return this.client.request(command);
  }

  public abstract ActorData getBaseData();

  @SneakyThrows
  private static String createIdentifier(String name) {
    val md = MessageDigest.getInstance("MD5"); // NOSONAR no cryptography involved here!
    return new BigInteger(1, md.digest(name.getBytes(StandardCharsets.UTF_8))).toString(16);
  }
}
