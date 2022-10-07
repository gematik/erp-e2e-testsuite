/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.lei.cfg.ActorConfiguration;
import de.gematik.test.erezept.lei.cfg.DoctorConfiguration;
import de.gematik.test.erezept.lei.cfg.EnvironmentConfiguration;
import de.gematik.test.erezept.lei.cfg.PharmacyConfiguration;
import de.gematik.test.erezept.primsys.rest.data.ActorData;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmcB;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public abstract class BaseActor {

  @Getter private final String name;
  @Getter private final String identifier;
  @Getter private ActorRole role;
  @Getter private final ErpClient client;
  @Getter private SmcB smcb;

  protected BaseActor(DoctorConfiguration cfg, EnvironmentConfiguration env, SmartcardArchive sca) {
    this(cfg, env);
    this.role = ActorRole.DOCTOR;
    this.smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn(), cfg.getCryptoAlgorithm());
    this.client.authenticateWith(smcb);
  }

  protected BaseActor(
      PharmacyConfiguration cfg, EnvironmentConfiguration env, SmartcardArchive sca) {
    this(cfg, env);
    this.role = ActorRole.PHARMACY;
    this.smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn(), cfg.getCryptoAlgorithm());
    this.client.authenticateWith(smcb);
  }

  @SneakyThrows
  private BaseActor(ActorConfiguration cfg, EnvironmentConfiguration env) {
    this.name = cfg.getName();
    val erpClientConfig = cfg.toErpClientConfig(env, ClientType.PS);
    this.client = ErpClientFactory.createErpClient(erpClientConfig);

    val md = MessageDigest.getInstance("MD5"); // NOSONAR no cryptography involved here!
    this.identifier =
        new BigInteger(1, md.digest(this.name.getBytes(StandardCharsets.UTF_8))).toString(16);
  }

  public final <R extends Resource> ErpResponse erpRequest(final ICommand<R> command) {
    return this.client.request(command);
  }

  public abstract ActorData getBaseData();
}
