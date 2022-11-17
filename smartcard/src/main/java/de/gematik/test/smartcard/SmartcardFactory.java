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

package de.gematik.test.smartcard;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.smartcard.cfg.EgkConfigDto;
import de.gematik.test.smartcard.cfg.HbaConfigDto;
import de.gematik.test.smartcard.cfg.LdapReader;
import de.gematik.test.smartcard.cfg.SmartcardIndex;
import de.gematik.test.smartcard.cfg.SmcbConfigDto;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@SuppressWarnings({"java:S1854"}) // assignments are not useless, producing false positives in sonar
public class SmartcardFactory {

  private static SmartcardArchive smartcardArchive;

  private SmartcardFactory() {
    throw new UnsupportedOperationException(
        "Do not instantiate utility class " + SmartcardFactory.class);
  }

  /**
   * Erstelle default Smartcard Archiv aus dem mitgelieferten Kartenmaterial
   *
   * @return {@link SmartcardArchive} Objekt welches den Zugriff auf die default Karten ermöglicht
   */
  @SneakyThrows
  public static SmartcardArchive getArchive() {
    if (smartcardArchive == null) {
      val is =
          Objects.requireNonNull(
              SmartcardFactory.class.getClassLoader().getResourceAsStream("cardimages/images.json"),
              format("Cannot read card images from resources"));
      smartcardArchive = getArchive(is);
    }
    return smartcardArchive;
  }

  @SneakyThrows
  private static SmartcardArchive getArchive(final InputStream is) {
    val index = new ObjectMapper().readValue(is, SmartcardIndex.class);
    // fix the relative paths of the DTOs
    index
        .getHbaCards()
        .forEach(
            hba -> {
              hba.setQes(normalizePath(hba.getQes()));
              hba.setAuth(normalizePath(hba.getAuth()));
            });

    index.getSmcbCards().forEach(smcb -> smcb.setAuth(normalizePath(smcb.getAuth())));
    index.getSmcbCards().stream()
        .filter(smcb -> smcb.getEnc() != null)
        .forEach(smcb -> smcb.setEnc(normalizePath(smcb.getEnc())));
    index.getEgkCards().forEach(egk -> egk.setAuth(normalizePath(egk.getAuth())));

    val hbaCards = loadHbaCards(index.getHbaCards());
    val smcbCards = loadSmcbCards(index.getSmcbCards());
    val egkCards = loadEgkCards(index.getEgkCards());

    return new SmartcardArchive(smcbCards, hbaCards, egkCards);
  }

  private static String normalizePath(final String input) {
    val baseDir = "cardimages";
    return Path.of(baseDir, input).normalize().toString().replace("\\", "/");
  }

  private static List<Hba> loadHbaCards(final List<HbaConfigDto> configs) {
    val ret = new ArrayList<Hba>();
    configs.forEach(cfg -> ret.add(loadHba(cfg)));
    return ret;
  }

  private static List<SmcB> loadSmcbCards(final List<SmcbConfigDto> configs) {
    val ret = new ArrayList<SmcB>();
    configs.forEach(cfg -> ret.add(loadSmcb(cfg)));
    return ret;
  }

  private static List<Egk> loadEgkCards(final List<EgkConfigDto> configs) {
    val ret = new ArrayList<Egk>();
    configs.forEach(cfg -> ret.add(loadEgk(cfg)));
    return ret;
  }

  private static Hba loadHba(final HbaConfigDto cfg) {
    log.trace(format("Loading HBA with ICSSN {0}", cfg.getIccsn()));
    val hbaBuilder =
        Hba.builder().type(SmartcardType.HBA).iccsn(cfg.getIccsn()).algorithm(cfg.getAlgorithm());
    loadHbaAuth(cfg, hbaBuilder);
    loadHbaQes(cfg, hbaBuilder);

    return hbaBuilder.build();
  }

  private static SmcB loadSmcb(final SmcbConfigDto cfg) {
    log.trace(format("Loading SMC-B with ICSSN {0} ({1})", cfg.getIccsn(), cfg.getAlgorithm()));
    val smcbBuilder =
        SmcB.builder()
            .type(SmartcardType.SMC_B)
            .iccsn(cfg.getIccsn())
            .algorithm(cfg.getAlgorithm());

    val authIs =
        Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(cfg.getAuth()),
            format("SMC-B Card image {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));

    val authEntry = loadEntryFromKeystore(authIs, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(authEntry, smcbBuilder);

    if (cfg.getEnc() != null && !cfg.getEnc().isEmpty()) {
      val encIs =
          Objects.requireNonNull(
              ClassLoader.getSystemResourceAsStream(cfg.getEnc()),
              format(
                  "SMC-B Card image {0} for {1} cannot be loaded", cfg.getEnc(), cfg.getIccsn()));
      log.info(format("Load Enc {0} with password {1}", cfg.getEnc(), cfg.getPassword()));
      val encEntry = loadEntryFromKeystore(encIs, cfg.getPassword(), cfg.getKeystoreType());
      smcbBuilder
          .encCertificateChain(encEntry.getCertificateChain())
          .encCertificate((X509Certificate) encEntry.getCertificate())
          .encPrivateKey(encEntry.getPrivateKey());
    }

    return smcbBuilder.build();
  }

  @SneakyThrows
  private static Egk loadEgk(final EgkConfigDto cfg) {
    log.trace(format("Loading EGK with ICCSN {0} for KVNR {1}", cfg.getIccsn(), cfg.getKvnr()));
    val egkBuilder = Egk.builder();
    egkBuilder
        .type(SmartcardType.EGK)
        .iccsn(cfg.getIccsn())
        .algorithm(cfg.getAlgorithm())
        .kvnr(cfg.getKvnr());

    val authIs =
        Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(cfg.getAuth()),
            format("eGK Card image {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));
    val entry = loadEntryFromKeystore(authIs, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(entry, egkBuilder);
    return egkBuilder.build();
  }

  @SneakyThrows
  private static <C extends Hba, B extends Hba.HbaBuilder<C, B>> void loadHbaAuth(
      final HbaConfigDto cfg, final Hba.HbaBuilder<C, B> hbaBuilder) {
    val authIs =
        Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(cfg.getAuth()),
            format("HBA Card image {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));
    val entry = loadEntryFromKeystore(authIs, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(entry, hbaBuilder);
  }

  @SneakyThrows
  private static <C extends Hba, B extends Hba.HbaBuilder<C, B>> void loadHbaQes(
      final HbaConfigDto cfg, final Hba.HbaBuilder<C, B> hbaBuilder) {
    val qesIs =
        Objects.requireNonNull(
            ClassLoader.getSystemResourceAsStream(cfg.getQes()),
            format("HBA Card image {0} for {1} cannot be loaded", cfg.getQes(), cfg.getIccsn()));

    val entry = loadEntryFromKeystore(qesIs, cfg.getPassword(), cfg.getKeystoreType());

    hbaBuilder
        .qesCertificate((X509Certificate) entry.getCertificate())
        .qesCertificateChain(entry.getCertificateChain())
        .qesPrivateKey(entry.getPrivateKey());

    val qesResourceUrl =
        Objects.requireNonNull(
            ClassLoader.getSystemResource(cfg.getQes()),
            format("Could not read QES certificate of HBA from {0}", cfg.getQes()));
    hbaBuilder
        .qesP12File(Paths.get(qesResourceUrl.toURI()).toFile())
        .qesP12Password(cfg.getPassword());
  }

  private static <C extends Smartcard, B extends Smartcard.SmartcardBuilder<C, B>> void setAuth(
      final KeyStore.PrivateKeyEntry entry, final Smartcard.SmartcardBuilder<C, B> smartcard) {
    val x509Cert = (X509Certificate) entry.getCertificate();

    smartcard
        .serialnumber(x509Cert.getSerialNumber())
        .authCertificateChain(entry.getCertificateChain())
        .authCertificate(x509Cert)
        .authPrivateKey(entry.getPrivateKey())
        .owner(LdapReader.getOwnerData(x509Cert.getSubjectDN()));
  }

  @SneakyThrows
  private static KeyStore.PrivateKeyEntry loadEntryFromKeystore(
      final InputStream is, final String password, final KeystoreType keystoreType) {
    val ks = KeyStore.getInstance(keystoreType.getName());
    ks.load(is, password.toCharArray());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    return (KeyStore.PrivateKeyEntry)
        ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
  }
}
