/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.konnektor.soap;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.config.dto.konnektor.TLSConfiguration;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class TrustProvider {

  private final KeyManager[] keyManagers;
  private final X509TrustManager[] trustManagers;

  private TrustProvider(KeyManager[] keyManagers, X509TrustManager[] trustManagers) {
    this.keyManagers = keyManagers;
    this.trustManagers = trustManagers;
    log.trace(
        format(
            "Created {0} with {1} KeyManagers and {2} TrustManagers",
            this.getClass().getSimpleName(), keyManagers.length, trustManagers.length));
  }

  public static TrustProvider from(TLSConfiguration cfg) {
    val keyStorePassword = cfg.getKeyStorePassword();
    val keyStoreFile = cfg.getKeyStore();

    val trustStorePassword = cfg.getTrustStorePassword();
    val trustStoreFile = cfg.getTrustStore();
    return from(keyStoreFile, keyStorePassword, trustStoreFile, trustStorePassword);
  }

  /**
   * Creates a TrustStore with given parameters
   *
   * @param ksf is the filename which contains the KeyStore
   * @param ksp is the corresponding password for the KeyStore
   * @param tsf is the filename which contains the TrustStore
   * @param tsp is the corresponding password for the TrustStore
   * @return a TrustStore object
   */
  @SneakyThrows
  public static TrustProvider from(String ksf, String ksp, String tsf, String tsp) {
    log.trace(format("Create KeyManager from {0} and TrustManager from {1}", ksf, tsf));
    // build the KeyStore
    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    val keyStore = KeyStore.getInstance(keyStoreTypeFromFilename(ksf));
    val keyStoreFilename = Path.of("tls", ksf);
    val keyStoreInputStream = ClassLoader.getSystemResourceAsStream(keyStoreFilename.toString());
    keyStore.load(keyStoreInputStream, ksp.toCharArray());
    keyManagerFactory.init(keyStore, ksp.toCharArray());

    val trustStoreFilename = Path.of("tls", tsf);
    val trustStoreInputStream =
        ClassLoader.getSystemResourceAsStream(trustStoreFilename.toString());
    val trustStore = KeyStore.getInstance(keyStoreTypeFromFilename(tsf));
    trustStore.load(trustStoreInputStream, tsp.toCharArray());
    val konnektorTrustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    konnektorTrustManagerFactory.init(trustStore);
    val konnektorTrustManager =
        findX509TrustManager(konnektorTrustManagerFactory.getTrustManagers());

    val trustManagers = new X509TrustManager[] {new KonnektorTrustManager(konnektorTrustManager)};
    return new TrustProvider(keyManagerFactory.getKeyManagers(), trustManagers);
  }

  private static X509TrustManager findX509TrustManager(TrustManager[] all) {
    return Arrays.stream(all)
        .filter(X509TrustManager.class::isInstance)
        .map(X509TrustManager.class::cast)
        .findFirst()
        .orElseThrow();
  }

  private static String keyStoreTypeFromFilename(String filename) {
    var type =
        "INVALID"; // will produce a KeyStoreException if filename does not provide proper extension
    if (filename.endsWith("p12") || filename.endsWith("pfx")) {
      type = "PKCS12";
    } else if (filename.endsWith("jks")) {
      type = "JKS";
    }
    return type;
  }

  @SneakyThrows
  public SSLSocketFactory getSocketFactory() {
    val sslctx = SSLContext.getInstance("TLSv1.2");
    sslctx.init(keyManagers, trustManagers, new java.security.SecureRandom());
    return sslctx.getSocketFactory();
  }

  private static class KonnektorTrustManager implements X509TrustManager {

    private final X509TrustManager[] trustmanagers;

    public KonnektorTrustManager(X509TrustManager... m) {
      this.trustmanagers = m;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      for (X509TrustManager tm : trustmanagers) {
        tm.checkServerTrusted(chain, authType);
      }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

      for (X509TrustManager tm : trustmanagers) {
        tm.checkServerTrusted(chain, authType);
      }
    }

    public X509Certificate[] getAcceptedIssuers() {
      return trustmanagers[0].getAcceptedIssuers();
    }
  }
}
