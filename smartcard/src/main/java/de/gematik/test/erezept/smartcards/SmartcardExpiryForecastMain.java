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

package de.gematik.test.erezept.smartcards;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bouncycastle.cert.X509CertificateHolder;

public class SmartcardExpiryForecastMain {

  private static final DateFormat dateFormatter =
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
  private static final Date today = new Date();

  public static void main(String[] args) throws IOException {
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
                                          val forecast =
                                              createExpiryColor(
                                                  cert.getX509CertificateHolder(), expiryWarnDate);
                                          summary.addCertificate(
                                              cryptoSystem, "AUT", expiry, forecast);
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
                                          val forecast =
                                              createExpiryColor(
                                                  cert.getX509CertificateHolder(), expiryWarnDate);
                                          summary.addCertificate(
                                              cryptoSystem, "ENC", expiry, forecast);
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
                                          val forecast =
                                              createExpiryColor(
                                                  cert.getX509CertificateHolder(), expiryWarnDate);
                                          summary.addCertificate(
                                              cryptoSystem, "QES", expiry, forecast);
                                        });
                              });
                    }
                  }

                  return summary;
                })
            .sorted(
                Comparator.comparing(ss -> ss.totalForecast.ordinal(), Comparator.reverseOrder()))
            .toList();

    val hasExpiringCards = summaries.stream().anyMatch(SmartcardSummary::warn);

    val sb = new StringBuilder();
    sb.append("<html><head><title>Smartcard Summary</title><meta charset=\"UTF-8\"></head><body>");

    sb.append("<h1>Smartcard Summary</h1>");
    sb.append("<p>Collected on ")
        .append(dateFormatter.format(today))
        .append(" smartcard forecast for ")
        .append(expiryForecast)
        .append(" Months")
        .append("</p><hr />");

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

          summary.certificates.forEach(
              cert -> {
                sb.append("<tr><td>")
                    .append(cert.cryptoSystem)
                    .append("</td><td>")
                    .append(cert.usage)
                    .append("</td><td bgcolor=\"")
                    .append(cert.forecast.color)
                    .append("\">")
                    .append(dateFormatter.format(cert.expiry))
                    .append("</td></tr>");
              });
          sb.append("</table><hr />");
        });

    sb.append("</body></html>");
    Files.writeString(Path.of("./target/smartcard-summary.html"), sb.toString());

    if (hasExpiringCards) {
      System.exit(1);
    } else {
      System.exit(0);
    }
  }

  private static ExpiryForecast createExpiryColor(
      X509CertificateHolder certificateHolder, Date forecastDate) {
    val alreadyExpired = !certificateHolder.isValidOn(today);
    if (alreadyExpired) {
      return ExpiryForecast.EXPIRED;
    }
    val expiresSoon = !certificateHolder.isValidOn(forecastDate);
    if (expiresSoon) {
      return ExpiryForecast.EXPIRES_SOON;
    } else {
      return ExpiryForecast.VALID;
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
    private ExpiryForecast totalForecast = ExpiryForecast.VALID;

    public void addCertificate(
        CryptoSystem cryptoSystem, String usage, Date expiry, ExpiryForecast forecast) {
      certificates.add(new CertificateSummary(cryptoSystem, usage, expiry, forecast));
      this.totalForecast = totalForecast.adjust(forecast);
    }

    public boolean warn() {
      return !this.totalForecast.equals(ExpiryForecast.VALID);
    }
  }

  private record CertificateSummary(
      CryptoSystem cryptoSystem, String usage, Date expiry, ExpiryForecast forecast) {}

  @RequiredArgsConstructor
  private enum ExpiryForecast {
    VALID("#00AA00"),
    EXPIRES_SOON("#FFAA00"),
    EXPIRED("#CC0000");

    private final String color;

    public ExpiryForecast adjust(ExpiryForecast forecast) {
      if (forecast.ordinal() > this.ordinal()) {
        return forecast;
      } else {
        return this;
      }
    }
  }
}
