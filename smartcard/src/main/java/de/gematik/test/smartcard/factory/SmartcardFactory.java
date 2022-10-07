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

package de.gematik.test.smartcard.factory;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.smartcard.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SmartcardFactory {

  private SmartcardFactory() {
    throw new AssertionError("Do not instantiate utility class " + SmartcardFactory.class);
  }

  /**
   * Erstelle default Smartcard Archiv aus dem mitgelieferten Kartenmaterial
   *
   * @return {@link SmartcardArchive} Objekt welches den Zugriff auf die default Karten ermöglicht
   */
  @SneakyThrows
  public static SmartcardArchive readArchive() {
    val is = SmartcardFactory.class.getClassLoader().getResourceAsStream("cardimages/images.json");
    return readArchive(is);
  }

  @SneakyThrows
  public static SmartcardArchive readArchive(InputStream is) {
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

  private static String normalizePath(String input) {
    val baseDir = "cardimages";
    return Path.of(baseDir, input).normalize().toString().replace("\\", "/");
  }

  private static List<Hba> loadHbaCards(List<HbaConfigDto> configs) {
    val ret = new ArrayList<Hba>();
    configs.forEach(cfg -> ret.add(loadHba(cfg)));
    return ret;
  }

  private static List<SmcB> loadSmcbCards(List<SmcbConfigDto> configs) {
    val ret = new ArrayList<SmcB>();
    configs.forEach(cfg -> ret.add(loadSmcb(cfg)));
    return ret;
  }

  private static List<Egk> loadEgkCards(List<EgkConfigDto> configs) {
    val ret = new ArrayList<Egk>();
    configs.forEach(cfg -> ret.add(loadEgk(cfg)));
    return ret;
  }

  private static Hba loadHba(HbaConfigDto cfg) {
    log.trace(format("Loading HBA with ICSSN {0}", cfg.getIccsn()));
    val hba = new Hba(cfg.getIccsn());
    hba.setAlgorithm(cfg.getAlgorithm());
    loadHbaAuth(cfg, hba);
    loadHbaQes(cfg, hba);

    return hba;
  }

  @SneakyThrows
  private static SmcB loadSmcb(SmcbConfigDto cfg) {
    log.trace(format("Loading SMC-B with ICSSN {0} ({1})", cfg.getIccsn(), cfg.getAlgorithm()));
    val smcb = new SmcB(cfg.getIccsn());
    smcb.setAlgorithm(cfg.getAlgorithm());

    val authIs = ClassLoader.getSystemResourceAsStream(cfg.getAuth());
    Objects.requireNonNull(
        authIs,
        format("SMC-B Cardimage {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));
    val authEntry = loadEntryFromKeystore(authIs, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(authEntry, smcb);

    if (cfg.getEnc() != null && !cfg.getEnc().isEmpty()) {
      val encIs = ClassLoader.getSystemResourceAsStream(cfg.getEnc());
      Objects.requireNonNull(
          encIs,
          format("SMC-B Cardimage {0} for {1} cannot be loaded", cfg.getEnc(), cfg.getIccsn()));
      log.info(format("Load Enc {0} with password {1}", cfg.getEnc(), cfg.getPassword()));
      val encEntry = loadEntryFromKeystore(encIs, cfg.getPassword(), cfg.getKeystoreType());
      smcb.setEncCertificateChain(encEntry.getCertificateChain());
      smcb.setEncCertificate((X509Certificate) encEntry.getCertificate());
      smcb.setEncPrivateKey(encEntry.getPrivateKey());
    }

    return smcb;
  }

  @SneakyThrows
  private static Egk loadEgk(EgkConfigDto cfg) {
    log.trace(format("Loading EGK with ICCSN {0} for KVNR {1}", cfg.getIccsn(), cfg.getKvnr()));
    val egk = new Egk(cfg.getIccsn());
    egk.setAlgorithm(cfg.getAlgorithm());
    egk.setKvnr(cfg.getKvnr());

    val is = ClassLoader.getSystemResourceAsStream(cfg.getAuth());
    Objects.requireNonNull(
        is, format("eGK Cardimage {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));
    val entry = loadEntryFromKeystore(is, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(entry, egk);
    return egk;
  }

  @SneakyThrows
  private static void loadHbaAuth(HbaConfigDto cfg, Hba hba) {
    val is = ClassLoader.getSystemResourceAsStream(cfg.getAuth());
    Objects.requireNonNull(
        is, format("HBA Cardimage {0} for {1} cannot be loaded", cfg.getAuth(), cfg.getIccsn()));
    val entry = loadEntryFromKeystore(is, cfg.getPassword(), cfg.getKeystoreType());
    setAuth(entry, hba);
  }

  @SneakyThrows
  private static void loadHbaQes(HbaConfigDto cfg, Hba hba) {
    val is = ClassLoader.getSystemResourceAsStream(cfg.getQes());
    Objects.requireNonNull(
        is, format("HBA Cardimage {0} for {1} cannot be loaded", cfg.getQes(), cfg.getIccsn()));

    /* Note: later on the LocalSigner will require the QES p12 file for signing
        However, later on the signer won't be able to access the file from jar -> write to a tmp-File as a workaround
    */
    val tmpQesFile = Files.createTempFile(null, null);
    try (val writer = new FileOutputStream(tmpQesFile.toFile())) {
      log.info(
          format(
              "Extracting p12 of HBA {0} ({1}) to temporary File {2}",
              cfg.getIccsn(), cfg.getAlgorithm(), tmpQesFile));
      is.transferTo(writer);
    }

    val entry = loadEntryFromKeystore(tmpQesFile, cfg.getPassword(), cfg.getKeystoreType());
    val x509Cert = (X509Certificate) entry.getCertificate();

    hba.setQesCertificate(x509Cert);
    hba.setQesCertificateChain(entry.getCertificateChain());
    hba.setQesPrivateKey(entry.getPrivateKey());

    hba.setQesP12File(tmpQesFile.toFile());
    hba.setQesP12Password(cfg.getPassword());
  }

  private static void setAuth(KeyStore.PrivateKeyEntry entry, Smartcard smartcard) {
    val x509Cert = (X509Certificate) entry.getCertificate();
    val subject = x509Cert.getSubjectDN();

    smartcard.setSerialnumber(x509Cert.getSerialNumber());
    smartcard.setAuthCertificateChain(entry.getCertificateChain());
    smartcard.setAuthCertificate(x509Cert);
    smartcard.setAuthPrivateKey(entry.getPrivateKey());
    smartcard.setOwner(LdapReader.getOwnerData(subject));
  }

  private static KeyStore.PrivateKeyEntry loadEntryFromKeystore(
      Path path, String password, KeystoreType keystoreType) {
    return loadEntryFromKeystore(path.toFile(), password, keystoreType);
  }

  @SneakyThrows
  private static KeyStore.PrivateKeyEntry loadEntryFromKeystore(
      File input, String password, KeystoreType keystoreType) {
    return loadEntryFromKeystore(new FileInputStream(input), password, keystoreType);
  }

  @SneakyThrows
  private static KeyStore.PrivateKeyEntry loadEntryFromKeystore(
      InputStream is, String password, KeystoreType keystoreType) {
    val ks = KeyStore.getInstance(keystoreType.getName());
    ks.load(is, password.toCharArray());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    return (KeyStore.PrivateKeyEntry)
        ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
  }
}
