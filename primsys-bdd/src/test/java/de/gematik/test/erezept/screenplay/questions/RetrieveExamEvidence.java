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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Egk;
import java.util.Base64;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor
public class RetrieveExamEvidence implements Question<Optional<String>> {
  private final Egk egk;

  @Override
  public Optional<String> answeredBy(Actor pharmacy) {
    val konnektor = SafeAbility.getAbility(pharmacy, UseTheKonnektor.class);

    val examEvidence = konnektor.requestEvidenceForEgk(egk);

    if (examEvidence.isEmpty()) {
      log.warn(format("Exam Evidence could not be determined for {0}", egk.getKvnr()));
    }

    return examEvidence.map(e -> Base64.getEncoder().encodeToString(e));
  }

  public static RetrieveExamEvidence with(Egk egk) {
    return new RetrieveExamEvidence(egk);
  }
}
