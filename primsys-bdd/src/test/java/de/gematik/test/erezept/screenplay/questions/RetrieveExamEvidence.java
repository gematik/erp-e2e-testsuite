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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmChecksum;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
@RequiredArgsConstructor
public class RetrieveExamEvidence implements Question<String> {

  private final Egk egk;

  public static RetrieveExamEvidence with(Egk egk) {
    return new RetrieveExamEvidence(egk);
  }

  private StringBuilder examEvidenceInfo;

  @Override
  public String answeredBy(Actor pharmacy) {
    val konnektor = SafeAbility.getAbility(pharmacy, UseTheKonnektor.class);

    val response = konnektor.requestEvidenceForEgk(egk).getPayload();

    val examEvidenceAsBase64 = Base64.getEncoder().encodeToString(response.getPruefungsnachweis());
    examEvidenceInfo = new StringBuilder(format("Prüfungsnachweis: {0} \n", examEvidenceAsBase64));
    val examEvidence = VsdmExamEvidence.parse(examEvidenceAsBase64);
    examEvidenceInfo.append(format("Prüfungsnachweis dekodiert: {0}\n\n", examEvidence));

    examEvidence
        .getChecksum()
        .ifPresentOrElse(
            it -> verifyChecksum(pharmacy, it),
            () -> examEvidenceInfo.append("Prüfziffer nicht vorhanden"));

    Serenity.recordReportData()
        .withTitle("Prüfungsnachweis")
        .andContents(examEvidenceInfo.toString());

    return examEvidenceAsBase64;
  }

  @SneakyThrows
  private void verifyChecksum(Actor pharmacy, String checksumAsBase64) {
    val checksum = VsdmChecksum.parse(checksumAsBase64);

    examEvidenceInfo.append(format("Prüfziffer: {0}\n", checksum));

    pharmacy.attemptsTo(Ensure.that(checksum.getKvnr()).isEqualTo(egk.getKvnr()));
  }
}
