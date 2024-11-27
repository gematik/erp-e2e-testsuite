/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.smartcards;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.InstituteSmartcard;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardCertificate;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class SmartcardExpiryForecastMain {

  public static void main(String[] args) throws IOException {
    val today = new Date();
    val expiryForecast = 2;
    val expirationCalendar = Calendar.getInstance();
    expirationCalendar.setTime(today);
    expirationCalendar.add(Calendar.MONTH, expiryForecast);
    val expiryWarnDate = expirationCalendar.getTime();

    val sca = SmartcardArchive.fromResources();
    val summaries =
        sca.getConfigs().stream()
            .sorted(Comparator.comparing(SmartcardConfigDto::getType))
            .map(
                cardInfo -> {
                  val summary =
                      new SmartcardSummary(
                          cardInfo.getType(),
                          cardInfo.getIccsn(),
                          cardInfo.getOwnerName(),
                          cardInfo.getNote());

                  if (!cardInfo.getStores().isEmpty()) {
                    val card = sca.getSmartcardByICCSN(cardInfo.getType(), cardInfo.getIccsn());

                    Stream.of(CryptoSystem.values())
                        .forEach(
                            cryptoSystem ->
                                card.getAutCertificate(cryptoSystem)
                                    .ifPresent(
                                        cert -> {
                                          val expiry = cert.getX509Certificate().getNotAfter();
                                          val warn =
                                              !cert.getX509CertificateHolder()
                                                  .isValidOn(expiryWarnDate);
                                          summary.addCertificate(cryptoSystem, "AUT", expiry, warn);
                                        }));

                    if (card instanceof InstituteSmartcard instituteSmartcard) {
                      // extract enc certificates
                      Stream.of(CryptoSystem.values())
                          .forEach(
                              cryptoSystem -> {
                                extractCertificate(
                                        () -> instituteSmartcard.getEncCertificate(cryptoSystem))
                                    .ifPresent(
                                        cert -> {
                                          val expiry = cert.getX509Certificate().getNotAfter();
                                          val warn =
                                              !cert.getX509CertificateHolder()
                                                  .isValidOn(expiryWarnDate);
                                          summary.addCertificate(cryptoSystem, "ENC", expiry, warn);
                                        });
                              });
                    }

                    if (card instanceof Hba hba) {
                      // extract qes certificates
                      Stream.of(CryptoSystem.values())
                          .forEach(
                              cryptoSystem -> {
                                extractCertificate(() -> hba.getQesCertificate(cryptoSystem))
                                    .ifPresent(
                                        cert -> {
                                          val expiry = cert.getX509Certificate().getNotAfter();
                                          val warn =
                                              !cert.getX509CertificateHolder()
                                                  .isValidOn(expiryWarnDate);
                                          summary.addCertificate(cryptoSystem, "QES", expiry, warn);
                                        });
                              });
                    }
                  }

                  return summary;
                })
            .toList();

    val hasExpiringCards = summaries.stream().anyMatch(SmartcardSummary::warn);

    val sb = new StringBuilder();
    sb.append("<html><head><title>Smartcard Summary</title><meta charset=\"UTF-8\"></head><body>");

    sb.append("<h1>Smartcard Summary</h1>");
    sb.append("<p>Collected on ")
        .append(today)
        .append("smartcard forecast for ")
        .append(expiryForecast)
        .append(" Months")
        .append("</p>");

    summaries.forEach(
        summary -> {
          sb.append("<h3>")
              .append(summary.type)
              .append(" ")
              .append(summary.iccsn)
              .append(" (")
              .append(summary.owner)
              .append(")</h3>");

          Optional.ofNullable(summary.note)
              .ifPresent(note -> sb.append("<i>").append(note).append("</i>"));

          sb.append(
              "<table border='1'><tr><th>CryptoSystem</th><th>Usage</th><th>Expiry</th></tr>");

          val dateColor = summary.warn() ? "red" : "green";

          summary.certificates.forEach(
              cert -> {
                sb.append("<tr><td>")
                    .append(cert.cryptoSystem)
                    .append("</td><td>")
                    .append(cert.usage)
                    .append("</td><td bgcolor=\"")
                    .append(dateColor)
                    .append("\">")
                    .append(cert.expiry)
                    .append("</td></tr>");
              });
          sb.append("</table>");
        });

    sb.append("</body></html>");
    Files.writeString(Path.of("./target/smartcard-summary.html"), sb.toString());

    if (hasExpiringCards) {
      System.exit(1);
    } else {
      System.exit(0);
    }
  }

  private static Optional<SmartcardCertificate> extractCertificate(
      Supplier<SmartcardCertificate> certificateSupplier) {
    try {
      return Optional.of(certificateSupplier.get());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static class SmartcardSummary {

    private final SmartcardType type;
    private final String iccsn;
    private final String owner;
    private final String note;
    private final List<CertificateSummary> certificates = new LinkedList<>();
    private boolean warn;

    public void addCertificate(CryptoSystem cryptoSystem, String usage, Date expiry, boolean warn) {
      certificates.add(new CertificateSummary(cryptoSystem, usage, expiry));
      if (!this.warn) {
        this.warn = warn;
      }
    }

    public boolean warn() {
      return warn;
    }
  }

  private record CertificateSummary(CryptoSystem cryptoSystem, String usage, Date expiry) {}
}
