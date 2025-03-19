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
 */

package de.gematik.test.erezept.primsys.actors;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.ProfileExtractor;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.HealthInsuranceConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.primsys.data.actors.ActorDto;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.commands.GetCardHandleCommand;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Getter
public abstract class BaseActor {

  protected final CryptoSystem algorithm;
  private final ActorType type;
  private final String name;
  private final String identifier;
  private final ErpClient client;
  private final SmcB smcb;
  private final ActorDto actorInfo;

  protected final Konnektor konnektor;
  protected final CardInfo smcbHandle;

  protected BaseActor(
      DoctorConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    this(ActorType.DOCTOR, cfg, env, konnektor, sca);
  }

  protected BaseActor(
      PharmacyConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    this(ActorType.PHARMACY, cfg, env, konnektor, sca);
  }

  protected BaseActor(
      HealthInsuranceConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    this(ActorType.HEALTH_INSURANCE, cfg, env, konnektor, sca);
  }

  private BaseActor(
      ActorType actorType,
      PsActorConfiguration cfg,
      EnvironmentConfiguration env,
      Konnektor konnektor,
      SmartcardArchive sca) {
    this.name = cfg.getName();
    this.identifier = createIdentifier(this.name);
    this.type = actorType;
    this.smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());
    this.konnektor = konnektor;

    this.smcbHandle =
        konnektor.execute(GetCardHandleCommand.forSmartcard(this.getSmcb())).getPayload();

    this.client = ErpClientFactory.createErpClient(env, cfg);
    this.client.authenticateWith(smcb);
    this.algorithm = CryptoSystem.fromString(cfg.getAlgorithm());
    this.actorInfo = this.initActorSummary();
  }

  @SneakyThrows
  private static String createIdentifier(String name) {
    val md = MessageDigest.getInstance("MD5"); // NOSONAR no cryptography involved here!
    return new BigInteger(1, md.digest(name.getBytes(StandardCharsets.UTF_8))).toString(16);
  }

  public final <R extends Resource> ErpResponse<R> erpRequest(final ICommand<R> command) {
    val response = this.getClient().request(command);
    if (response.isOperationOutcome() || response.getStatusCode() > 299) {
      ErrorResponseBuilder.throwFachdienstError(response);
    }
    return response;
  }

  // Note: when throw an error anyway in erpRequest, why not unpacking the response directly?
  public final <R extends Resource> R erpRequest2(final ICommand<R> command) {
    val response = this.getClient().request(command);
    if (response.isOperationOutcome() || response.getStatusCode() > 299) {
      ErrorResponseBuilder.throwFachdienstError(response);
    }
    return response.getExpectedResource();
  }

  public <T extends Resource> T decode(Class<T> expectedClass, final String content) {
    try {
      return this.getClient().getFhir().decode(expectedClass, content);
    } catch (DataFormatException dfe) {
      val profileExtractor = new ProfileExtractor();
      val profile =
          profileExtractor
              .extractProfile(content)
              .map(p -> format("with profile {0}", p))
              .orElse("without profile");
      throw ErrorResponseBuilder.createInternalErrorException(
          400,
          format(
              "Unable to decode the given FHIR-Content {0} as {1}",
              profile, expectedClass.getSimpleName()));
    }
  }

  public String encode(Resource resource, EncodingType encoding) {
    return this.getClient().encode(resource, encoding);
  }

  public ActorDto getActorSummary() {
    return this.actorInfo;
  }

  private ActorDto initActorSummary() {
    val summary = new ActorDto();
    summary.setType(this.getType());
    summary.setName(this.getName());
    summary.setId(this.getIdentifier());
    summary.setTid(this.getSmcb().getTelematikId());
    summary.setSmcb(this.getSmcb().getIccsn());

    val pubk =
        Base64.getEncoder()
            .encodeToString(
                this.getSmcb()
                    .getAutCertificate()
                    .getX509Certificate()
                    .getPublicKey()
                    .getEncoded());
    summary.setPublicKey(pubk);
    return summary;
  }
}
