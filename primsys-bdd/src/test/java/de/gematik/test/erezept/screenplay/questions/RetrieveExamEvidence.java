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
 */

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.utils.XmlEncoder;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.ws.fa.vsds.UCAllgemeineVersicherungsdatenXML;
import de.gematik.ws.fa.vsds.UCPersoenlicheVersichertendatenXML;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class RetrieveExamEvidence implements Question<RetrieveExamEvidence> {

  private final Egk egk;
  @Getter private String examEvidence;
  @Getter private LocalDate insuranceStartDate;
  @Getter private String street;

  public static RetrieveExamEvidence with(Egk egk) {
    return new RetrieveExamEvidence(egk);
  }

  @Override
  public RetrieveExamEvidence answeredBy(Actor pharmacy) {
    val konnektor = SafeAbility.getAbility(pharmacy, UseTheKonnektor.class);

    val response = konnektor.requestEvidenceForEgk(egk).getPayload();

    examEvidence = getExamEvidence(response.getPruefungsnachweis());
    insuranceStartDate = getInsuranceStartDate(response.getAllgemeineVersicherungsdaten());
    street = getStreet(response.getPersoenlicheVersichertendaten());

    return this;
  }

  private String getExamEvidence(byte[] pruefungsnachweis) {
    val examEvidenceAsBase64 = new String(pruefungsnachweis, StandardCharsets.UTF_8);
    val evidence = VsdmExamEvidence.parse(examEvidenceAsBase64);

    Serenity.recordReportData()
        .withTitle("Prüfungsnachweis")
        .andContents(format("Base64: {0}\nXML: {1}", examEvidenceAsBase64, evidence.asXml()));

    evidence
        .getCheckDigit()
        .ifPresent(
            checksumAsBase64 -> {
              val version = VsdmCheckDigitVersion.fromData(checksumAsBase64);
              Serenity.recordReportData()
                  .withTitle("Prüfziffer")
                  .andContents(
                      format("Base64: {0}\nVersion: {1}", checksumAsBase64, version.name()));
            });
    return examEvidenceAsBase64;
  }

  @SneakyThrows
  private LocalDate getInsuranceStartDate(byte[] compressedCommonInsuranceData) {
    val commonInsuranceData =
        XmlEncoder.parse(UCAllgemeineVersicherungsdatenXML.class, compressedCommonInsuranceData);
    val date = commonInsuranceData.getVersicherter().getVersicherungsschutz().getBeginn();
    return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
  }

  @SneakyThrows
  private String getStreet(byte[] persoenlicheVersichertendaten) {
    val personalInsuranceData =
        XmlEncoder.parse(UCPersoenlicheVersichertendatenXML.class, persoenlicheVersichertendaten);
    val street =
        personalInsuranceData.getVersicherter().getPerson().getStrassenAdresse().getStrasse();
    return street == null ? "" : street;
  }
}
