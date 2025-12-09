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
import jakarta.xml.ws.WebServiceClient;
import java.net.URL;
import java.util.function.Function;
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
    return createAndConfigurePort(
        SignatureService.class,
        s -> ((SignatureService) s).getSignatureServicePort(),
        profile.getSignaturePath());
  }

  public final AuthSignatureServicePortType getAuthSignatureService() {
    return createAndConfigurePort(
        AuthSignatureService.class,
        s -> ((AuthSignatureService) s).getAuthSignatureServicePort(),
        profile.getAuthSignaturePath());
  }

  public final CertificateServicePortType getCertificateService() {
    return createAndConfigurePort(
        CertificateService.class,
        s -> ((CertificateService) s).getCertificateServicePort(),
        profile.getCertificatePath());
  }

  public final EventServicePortType getEventService() {
    return createAndConfigurePort(
        EventService.class, s -> ((EventService) s).getEventServicePort(), profile.getEventPath());
  }

  public final CardServicePortType getCardService() {
    return createAndConfigurePort(
        CardService.class, s -> ((CardService) s).getCardServicePort(), profile.getCardPath());
  }

  public final CardTerminalServicePortType getCardTerminalService() {
    return createAndConfigurePort(
        CardTerminalService.class,
        s -> ((CardTerminalService) s).getCardTerminalServicePort(),
        profile.getCardTerminalPath());
  }

  @Override
  public VSDServicePortType getVSDServicePortType() {
    return createAndConfigurePort(
        VSDService.class, s -> ((VSDService) s).getVSDServicePort(), profile.getVsdPath());
  }

  @Override
  public EncryptionServicePortType getEncryptionServicePortType() {
    return createAndConfigurePort(
        EncryptionService.class,
        s -> ((EncryptionService) s).getEncryptionServicePort(),
        profile.getEncryptionPath());
  }

  public <T> T createAndConfigurePort(
      Class<?> serviceClass, Function<Object, T> portGetter, String path) {
    val service = instantiateService(serviceClass);
    val port = portGetter.apply(service);
    setEndpointAddress((BindingProvider) port, path);
    return port;
  }

  @SneakyThrows
  private <T> T instantiateService(Class<T> serviceClass) {
    try {
      val wsdlLocation = resolveWsdlLocation(serviceClass);
      return serviceClass.getConstructor(URL.class).newInstance(wsdlLocation);
    } catch (Exception e) {
      return serviceClass.getDeclaredConstructor().newInstance();
    }
  }

  private URL resolveWsdlLocation(Class<?> serviceClass) {
    val wsc = serviceClass.getAnnotation(WebServiceClient.class);
    if (wsc == null) {
      throw new IllegalStateException(
          "Annotation WebServiceClient is missing in " + serviceClass.getName());
    }

    String wsdlLocation = wsc.wsdlLocation();
    if (wsdlLocation == null || wsdlLocation.isBlank()) {
      throw new IllegalStateException("wsdlLocation is missing in " + serviceClass.getName());
    }
    if (wsdlLocation.contains("gematik_schemes/")) {
      wsdlLocation = wsdlLocation.substring(wsdlLocation.indexOf("gematik_schemes/"));
    }
    return RemoteKonnektorServiceProvider.class.getClassLoader().getResource(wsdlLocation);
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
