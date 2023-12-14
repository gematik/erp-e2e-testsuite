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

package de.gematik.test.smartcard;

import de.gematik.test.erezept.crypto.certificate.Oid;
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper;
import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import de.gematik.test.smartcard.exceptions.InvalidCertificateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;

@EqualsAndHashCode
@Slf4j
public class SmartcardCertificate {

  @Getter private final X509CertificateWrapper certWrapper;

  @Getter private final PrivateKey privateKey;
  @Getter private final Algorithm algorithm;

  private File file;

  @SneakyThrows
  private SmartcardCertificate(InputStream inputStream) {
    val privateKeyEntry = loadEntryFromKeystore(inputStream);
    inputStream.close();

    privateKey = privateKeyEntry.getPrivateKey();
    certWrapper = new X509CertificateWrapper((X509Certificate) privateKeyEntry.getCertificate());
    algorithm =
        Algorithm.fromOid(certWrapper.toCertificateHolder().getSignatureAlgorithm().getAlgorithm());
  }

  @SneakyThrows
  public SmartcardCertificate(String path) {
    this(
        Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(path),
            format("Smartcard Certificate {0} cannot be loaded", path)));

    val resourceUrl = ClassLoader.getSystemResource(path);
    if (resourceUrl.getProtocol().equals("jar")) {
      // if loading from jar externalForm is required
      this.file = Paths.get(resourceUrl.toExternalForm()).toFile();
    } else {
      // otherwise the .p12 file is within classes and can be loaded via the URI
      this.file = Paths.get(resourceUrl.toURI()).toFile();
    }
  }

  public X509Certificate getX509Certificate() {
    return this.certWrapper.toCertificate();
  }

  public X509CertificateHolder getX509CertificateHolder() {
    return certWrapper.toCertificateHolder();
  }

  @Override
  public String toString() {
    return format("SmartcardKey with algorithm={0}", algorithm);
  }

  public Oid getOid() {
    val oid = certWrapper.getCertificateTypeOid();
    return oid.orElseThrow(() -> new InvalidCertificateException(certWrapper));
  }

  /**
   * This method will decide if the LocalSigner is currently running from a jar or from classes This
   * decision is required because getting file from a jar or from plain file path differs.
   *
   * @return a supplier for an InputStream of the certificate file
   */
  public Supplier<InputStream> getInputStreamSupplier() {
    Objects.requireNonNull(file);

    if (this.file.getAbsolutePath().contains("jar:file:")) {
      log.info(format("Use {0} from resources for signing XML Document", this));
      val fileResourcePath = this.file.getAbsolutePath().split("jar!")[1];
      return () -> this.getClass().getResourceAsStream(fileResourcePath);
    } else {
      log.info(format("Use {0} from directory for signing XML Document", this));
      return () -> {
        try {
          return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
          throw new CardNotFoundException(
              format("Could not open Certificate of Smartcard {0}", this), e);
        }
      };
    }
  }

  @SneakyThrows
  private KeyStore.PrivateKeyEntry loadEntryFromKeystore(final InputStream is) {
    val ks = KeyStore.getInstance(KeystoreType.P12.getName());
    ks.load(is, getP12KeyStorePassword().toCharArray());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    return (KeyStore.PrivateKeyEntry)
        ks.getEntry(alias, new KeyStore.PasswordProtection(getP12KeyStorePassword().toCharArray()));
  }

  public String getP12KeyStorePassword() {
    return "00";
  }
}
