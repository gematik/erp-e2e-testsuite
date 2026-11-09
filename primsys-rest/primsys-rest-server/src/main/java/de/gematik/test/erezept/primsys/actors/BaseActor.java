/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.primsys.actors;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.exceptions.FhirCodecException;
import de.gematik.bbriccs.fhir.validation.ProfileExtractor;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.konnektor.requests.GetCardHandleRequest;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ErpBaseCommand;
import de.gematik.test.erezept.primsys.data.actors.ActorDto;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
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

  // this holds the time until the IDP token from the last request is valid.
  // It is updated on every request to the Fachdienst.
  private Instant idpTokenValidUntil = Instant.now();

  protected final Konnektor konnektor;
  protected final CardInfo smcbHandle;

  protected BaseActor(
      ActorType actorType,
      String name,
      SmcB smcb,
      ErpClient client,
      CryptoSystem algorithm,
      Konnektor konnektor) {
    this.name = name;
    this.identifier = createIdentifier(this.name);
    this.type = actorType;
    this.smcb = smcb;
    this.konnektor = konnektor;

    this.smcbHandle =
        konnektor.execute(GetCardHandleRequest.forSmartcard(this.getSmcb())).getPayload();

    this.client = client;
    this.algorithm = algorithm;
    this.actorInfo = this.initActorSummary();
  }

  @SneakyThrows
  private static String createIdentifier(String name) {
    val md = MessageDigest.getInstance("MD5"); // NOSONAR no cryptography involved here!
    return new BigInteger(1, md.digest(name.getBytes(StandardCharsets.UTF_8))).toString(16);
  }

  public final <R extends Resource> FhirBResponse<R> erpRequest(final ErpBaseCommand<R> command) {
    val response = this.getClient().request(command);
    recordIdpTokenValidUntil(response);
    if (response.isOperationOutcome() || response.getStatusCode() > 299) {
      ErrorResponseBuilder.throwFachdienstError(response);
    }
    return response;
  }

  // Note: when throw an error anyway in erpRequest, why not unpacking the response directly?
  public final <R extends Resource> R erpRequest2(final ErpBaseCommand<R> command) {
    val response = this.getClient().request(command);
    recordIdpTokenValidUntil(response);
    if (response.isOperationOutcome() || response.getStatusCode() > 299) {
      ErrorResponseBuilder.throwFachdienstError(response);
    }
    return response.getExpectedResource();
  }

  private void recordIdpTokenValidUntil(FhirBResponse<?> response) { // NOSONAR
    // NOTE: this is just a workaround, parse the usedJwt from the response and extract the
    // expiration time from it. For now, we just set it to 3 minutes in the future.
    this.idpTokenValidUntil = Instant.now().plus(Duration.ofMinutes(3));
  }

  public <T extends Resource> T decode(Class<T> expectedClass, final String content) {
    try {
      return this.getClient().decode(expectedClass, content);
    } catch (DataFormatException | FhirCodecException fce) {
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
    val ttl = Duration.between(Instant.now(), idpTokenValidUntil);
    return actorInfo
        .setIdpValidUntil(idpTokenValidUntil.toString())
        .setIdpTimeToLive(ttl.toString());
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
