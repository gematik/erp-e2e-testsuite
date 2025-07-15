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

package de.gematik.test.konnektor.soap;

import static java.text.MessageFormat.format;

import com.sun.xml.ws.developer.JAXWSProperties;
import de.gematik.test.konnektor.profile.KonnektorProfile;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureService;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardService;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalService;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateService;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionService;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventService;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureService;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDService;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import jakarta.xml.ws.BindingProvider;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RemoteKonnektorServiceProvider extends ServicePortProvider {

  private static final String SSL_SOCKET_FACTORY =
      "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

  @Getter private final URL baseUrl;
  private TrustProvider trustProvider;
  @Getter @Setter private String username;
  @Getter @Setter private String password;

  public RemoteKonnektorServiceProvider(@NonNull URL baseUrl, @NonNull KonnektorProfile profile) {
    super(profile);
    this.baseUrl = baseUrl;
  }

  public static Builder of(URL baseUrl, KonnektorProfile profile) {
    return new Builder(baseUrl, profile);
  }

  public final SignatureServicePortType getSignatureService() {
    val service = new SignatureService();
    val servicePort = service.getSignatureServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getSignaturePath());
    return servicePort;
  }

  public final AuthSignatureServicePortType getAuthSignatureService() {
    val service = new AuthSignatureService();
    val servicePort = service.getAuthSignatureServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getAuthSignaturePath());
    return servicePort;
  }

  public final CertificateServicePortType getCertificateService() {
    val service = new CertificateService();
    val servicePort = service.getCertificateServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getCertificatePath());
    return servicePort;
  }

  @SneakyThrows
  public final EventServicePortType getEventService() {
    val service = new EventService();
    val servicePort = service.getEventServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getEventPath());
    return servicePort;
  }

  public final CardServicePortType getCardService() {
    val service = new CardService();
    val servicePort = service.getCardServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getCardPath());
    return servicePort;
  }

  public final CardTerminalServicePortType getCardTerminalService() {
    val service = new CardTerminalService();
    val servicePort = service.getCardTerminalServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getEventPath());
    return servicePort;
  }

  @Override
  public VSDServicePortType getVSDServicePortType() {
    val service = new VSDService();
    val servicePort = service.getVSDServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getVsdPath());
    return servicePort;
  }

  @Override
  public EncryptionServicePortType getEncryptionServicePortType() {
    val service = new EncryptionService();
    val servicePort = service.getEncryptionServicePort();
    setEndpointAddress((BindingProvider) servicePort, profile.getEncryptionPath());
    return servicePort;
  }

  @SuppressWarnings("java:S1874")
  private void setEndpointAddress(BindingProvider servicePort, String path) {
    val endpoint = createEndpoint(path);
    servicePort.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

    if (this.username != null) {
      servicePort.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, this.username);
    }
    if (this.password != null) {
      servicePort.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, this.password);
    }

    if (trustProvider != null) {
      // set SSL: see also
      // https://stackoverflow.com/questions/11001102/how-to-programmatically-set-the-sslcontext-of-a-jax-ws-client
      val sslSocketFactory = trustProvider.getSocketFactory();
      servicePort.getRequestContext().put(SSL_SOCKET_FACTORY, sslSocketFactory);

      // TODO: find a better way!
      // the given truststore usually contains a different domain: e.g. konsim running on localhost
      // is
      // still using RU cert
      HostnameVerifier hostnameVerifier =
          (hostname, session) -> true; // NOSONAR already covered by comment above
      servicePort.getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, hostnameVerifier);
    }
  }

  private String createEndpoint(String path) {
    val baseString = baseUrl.toString();
    val pathString =
        path.startsWith("/") ? path : "/" + path; // NOSONAR not dealing with paths here!
    return format("{0}{1}", baseString, pathString);
  }

  @Override
  public String toString() {
    return format("{0} at {1}", this.profile.getType(), this.getBaseUrl());
  }

  public static class Builder {
    private final RemoteKonnektorServiceProvider serviceProvider;

    private Builder(URL baseUrl, KonnektorProfile profile) {
      serviceProvider = new RemoteKonnektorServiceProvider(baseUrl, profile);
    }

    public Builder username(String username) {
      this.serviceProvider.setUsername(username);
      return this;
    }

    public Builder password(String password) {
      this.serviceProvider.setPassword(password);
      return this;
    }

    public Builder trustProvider(TrustProvider trustProvider) {
      this.serviceProvider.trustProvider = trustProvider;
      return this;
    }

    public RemoteKonnektorServiceProvider build() {
      return serviceProvider;
    }
  }
}
