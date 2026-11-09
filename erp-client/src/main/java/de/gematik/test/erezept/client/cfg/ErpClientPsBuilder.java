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

package de.gematik.test.erezept.client.cfg;

import static de.gematik.test.erezept.client.cfg.ErpClientFactory.createIdpAuthCustomizer;

import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.idp.IdpTokenHeaderProvider;
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.idp.client.IIdpClient;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This class is staged {@link ErpClient} creation for Primärsystem (PS) actors, i.e. doctors and
 * pharmacies that authenticate with an SMC-B.
 *
 * <p>Instances are never created directly; they are handed out by {@link
 * ErpClientFactory#prepareStagedClientFor(de.gematik.test.erezept.config.dto.actor.PsActorConfiguration)}
 * after the root factory has applied all environment- and actor-independent configuration to the
 * wrapped {@link ErpClient.ErpClientBuilder}. This class deliberately exposes only the thin
 * remaining surface:
 *
 * <ul>
 *   <li><b>optional overrides</b>: {@link #withIdpClientFrom} and {@link #withVauClientFrom}
 *       replace the default IDP-/VAU-client strategies inherited from the root factory. Callers
 *       normally do not need these.
 *   <li><b>terminal methods</b>: exactly one of {@link #createWithSmcBAuthentication()} or
 *       {@link#createWithExternalAuthentication()} must be called. It fixes the authentication
 *       strategy, wires the request customizers and VAU client, and builds the final {@link
 *       ErpClient}.
 * </ul>
 *
 * <p>A stage is a single-use object: the terminal call consumes the wrapped builder, so the stage
 * should not be reused to build a second client.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ErpClientPsBuilder {
  private final LinkedList<HttpBRequestCustomizer> requestCustomizers = new LinkedList<>();

  private final BackendRouteConfiguration backendRouteConfiguration;
  private final X509Certificate vauCertificate;
  private final ErpClient.ErpClientBuilder stagedPsClient;
  @Getter private final SmcB smcb;

  // customizable configurations for PS client
  private Function<BackendRouteConfiguration, IIdpClient> idpClientFactory;
  private BiFunction<BackendRouteConfiguration, X509Certificate, HttpBClient> vauClientFactory;

  /**
   * Sets the factory function to create an IIdpClient based on the provided
   * BackendRouteConfiguration. This allows for dynamic creation of the IIdpClient depending on the
   * backend route configuration. The user can provide a custom implementation of the IIdpClient if
   * needed but does not need to because the default implementation is already provided by the root
   * factory. The default implementation is based on the backend route configuration and will be
   * used if no custom implementation is provided.
   *
   * @param idpClientFactory A function that takes a BackendRouteConfiguration and returns an
   *     IIdpClient. This function will be used to create the IIdpClient for the PS client.
   * @return The current instance of ErpClientPsBuilder, allowing for method chaining.
   */
  public ErpClientPsBuilder withIdpClientFrom(
      Function<BackendRouteConfiguration, IIdpClient> idpClientFactory) {
    this.idpClientFactory = idpClientFactory;
    return this;
  }

  /**
   * Sets the factory function to create an HttpBClient (VAU client) based on the provided
   * BackendRouteConfiguration and X509Certificate. This allows for dynamic creation of the
   * HttpBClient depending on the backend route configuration and certificate. The user can provide
   * a custom implementation of the HttpBClient if needed but does not need to because the default
   * implementation is already provided by the root factory. The default implementation is based on
   * the backend route configuration and certificate and will be used if no custom implementation is
   * provided.
   *
   * @param vauClientFactory A function that takes a BackendRouteConfiguration and X509Certificate
   *     and returns an HttpBClient. This function will be used to create the HttpBClient for the PS
   *     client.
   * @return The current instance of ErpClientPsBuilder, allowing for method chaining.
   */
  public ErpClientPsBuilder withVauClientFrom(
      BiFunction<BackendRouteConfiguration, X509Certificate, HttpBClient> vauClientFactory) {
    this.vauClientFactory = vauClientFactory;
    return this;
  }

  /**
   * Terminal method: authenticates against the IDP directly with the actor's SMC-B (the client
   * signs the IDP challenge itself using the card's AUT identity) and builds the final client.
   *
   * @return the fully configured {@link ErpClient}
   */
  public ErpClient createWithSmcBAuthentication() {
    val idpClient = idpClientFactory.apply(backendRouteConfiguration).initialize();
    val authPlugin = createIdpAuthCustomizer(idpClient, smcb);
    requestCustomizers.add(authPlugin);

    return finalizeBuild();
  }

  /**
   * Terminal method: authenticates against the IDP via <em>external authenticate</em>, i.e. the IDP
   * challenge is signed by an external component (typically a Konnektor holding the SMC-B) rather
   * than by the client itself, and builds the final client.
   *
   * @param authPubCertSupplier supplies the public authentication certificate matching the key used
   *     by {@code challenge}
   * @param challenge signs the IDP challenge externally and returns the signature
   * @return the fully configured {@link ErpClient}
   */
  public ErpClient createWithExternalAuthentication(
      Supplier<X509Certificate> authPubCertSupplier, UnaryOperator<byte[]> challenge) {
    val idpClient = idpClientFactory.apply(backendRouteConfiguration).initialize();

    val plugin =
        IdpTokenHeaderProvider.using(() -> idpClient.login(authPubCertSupplier.get(), challenge));
    requestCustomizers.add(plugin);

    return finalizeBuild();
  }

  private ErpClient finalizeBuild() {
    val vauClient = vauClientFactory.apply(backendRouteConfiguration, vauCertificate);

    return stagedPsClient.vauClient(vauClient).requestCustomizers(requestCustomizers).build();
  }
}
