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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import de.gematik.test.erezept.fhir.valuesets.eu.EuConsentType;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Reference;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErxEuConsentVerifier {

  /** Prüft, ob die Kategorie des Consents dem erwarteten EU-Consent-Typ entspricht. */
  public static VerificationStep<EuConsent> consentCategoryIs(EuConsentType expectedType) {
    Predicate<EuConsent> predicate =
        consent ->
            !consent.getCategory().isEmpty()
                && expectedType
                    .getCode()
                    .equals(consent.getCategoryFirstRep().getCodingFirstRep().getCode());

    val step =
        new VerificationStep.StepBuilder<EuConsent>(
            ErpAfos.A_22162_01,
            format("Der Consent muss den Kategorie-Code {0} enthalten", expectedType.getCode()));

    return step.predicate(predicate).accept();
  }

  /** Prüft, ob der Consent die erwartete Patienten-KVNR enthält. */
  public static VerificationStep<EuConsent> patientIdentifierIs(KVNR expectedKvnr) {
    Predicate<EuConsent> predicate =
        consent ->
            consent.getPatient().hasIdentifier()
                && expectedKvnr.getValue().equals(consent.getPatient().getIdentifier().getValue());

    val step =
        new VerificationStep.StepBuilder<EuConsent>(
            ErpAfos.A_22162_01,
            format("Der Consent muss die Patienten-KVNR {0} enthalten", expectedKvnr.getValue()));

    return step.predicate(predicate).accept();
  }

  /** Prüft, ob der ConsentBundle die erwartete Patienten-KVNR enthält. */
  public static VerificationStep<EuConsentBundle> verifyPatientIdentifierInBundle(
      KVNR expectedKvnr) {
    Predicate<EuConsentBundle> predicate =
        bundle ->
            bundle
                .getConsent()
                .map(Consent::getPatient)
                .filter(Reference::hasIdentifier)
                .map(it -> it.getIdentifier().getValue())
                .map(it -> expectedKvnr.getValue().equals(it))
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<EuConsentBundle>(
            ErpAfos.A_22162_01,
            format("Der Consent muss die Patienten-KVNR {0} enthalten", expectedKvnr.getValue()));

    return step.predicate(predicate).accept();
  }

  /** Prüft, ob der Consent einen gültigen UTC-Zeitstempel hat (±1 Minute Differenz) */
  public static VerificationStep<EuConsent> hasValidTimestamp() {
    Predicate<EuConsent> predicate =
        consent -> {
          if (consent.getDateTime() == null) return false;

          Instant consentDate = consent.getDateTime().toInstant();
          Instant now = Instant.now();

          long diffMillis = Math.abs(Duration.between(consentDate, now).toMillis());
          return diffMillis < Duration.ofMinutes(1).toMillis();
        };

    return new VerificationStep.StepBuilder<EuConsent>(
            Requirement.custom(""), "dateTime entspricht dem Timestamp der Erstellung (UTC)")
        .predicate(predicate)
        .accept();
  }

  /** Prüft, ob der Consent überhaupt einen DateTime-Wert besitzt (nicht null) */
  public static VerificationStep<EuConsent> hasDateTime() {
    Predicate<EuConsent> predicate = consent -> consent.getDateTime() != null;

    return new VerificationStep.StepBuilder<EuConsent>(
            Requirement.custom(ErpAfos.A_27143.getDescription()), "dateTime ist vorhanden")
        .predicate(predicate)
        .accept();
  }

  /** Prüft, dass der Consent datetime nicht einem verbotenen Wert entspricht */
  public static VerificationStep<EuConsent> datetimeIsNot(Date forbiddenDate) {
    Predicate<EuConsent> predicate =
        consent -> consent.getDateTime() != null && !consent.getDateTime().equals(forbiddenDate);

    return new VerificationStep.StepBuilder<EuConsent>(
            Requirement.custom(ErpAfos.A_27143.getDescription()),
            "dateTime darf nicht dem vom Client gesetzten Wert entsprechen")
        .predicate(predicate)
        .accept();
  }

  /** Prüft, dass kein Consent im Bundle vorhanden ist. */
  public static VerificationStep<EuConsentBundle> hasNoConsentEntry() {
    Predicate<EuConsentBundle> predicate = bundle -> !bundle.hasEntry();

    return new VerificationStep.StepBuilder<EuConsentBundle>(
            Requirement.custom("ConsentBundle enthält keinen Consent"),
            "Der ConsentBundle darf keinen Consent enthalten")
        .predicate(predicate)
        .accept();
  }
}
