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

import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErxEuAccessPermissionVerifier {
  private static final String A_10395 = "A_10395";

  /** Prüft, ob valid_until innerhalb von 1 Stunde (+10s Toleranz) liegt (inklusive). */
  public static VerificationStep<EuAccessPermission> validUntilWithinOneHour() {
    Predicate<EuAccessPermission> predicate =
        perm -> {
          Optional<Instant> validUntilOpt = perm.getValidUntil();
          if (validUntilOpt.isEmpty()) return false;

          Instant validUntil = validUntilOpt.get();
          Instant now = Instant.now();
          Instant oneHourInFuture = now.plus(1, ChronoUnit.HOURS).plusSeconds(10);

          return !validUntil.isBefore(now) && !validUntil.isAfter(oneHourInFuture);
        };

    return new VerificationStep.StepBuilder<EuAccessPermission>(
            Requirement.custom(A_10395), "muss innerhalb einer Stunde nach Erzeugungszeit liegen")
        .predicate(predicate)
        .accept();
  }

  /** Prüft, ob das Response-Profil korrekt gesetzt ist. */
  public static VerificationStep<EuAccessPermission> hasCorrectProfile() {
    String expectedProfile =
        GemErpEuStructDef.ACCESS_AUTHORIZATION_RESPONSE.getCanonicalUrl()
            + "|"
            + VersionUtil.omitPatch(EuVersion.getDefaultVersion().getVersion());

    Predicate<EuAccessPermission> predicate =
        perm ->
            perm.getMeta() != null
                && perm.getMeta().getProfile() != null
                && perm.getMeta().getProfile().stream().anyMatch(p -> p.equals(expectedProfile));

    return new VerificationStep.StepBuilder<EuAccessPermission>(
            Requirement.custom(A_10395),
            format("Response muss Profil {0} enthalten", expectedProfile))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EuAccessPermission> hasIsoCountry(IsoCountryCode country) {
    Predicate<EuAccessPermission> predicate = perm -> perm.getIsoCountryCode().equals(country);

    return new VerificationStep.StepBuilder<EuAccessPermission>(
            Requirement.custom(A_10395),
            format("Zugriffsberechtigung muss für Land {0} ausgestellt sein", country.getCode()))
        .predicate(predicate)
        .accept();
  }

  /** Prüft, dass genau eine AccessPermission vorhanden ist. */
  public static VerificationStep<EuAccessPermission> hasExactlyOnePermission() {
    Predicate<EuAccessPermission> predicate =
        perm ->
            perm != null
                && perm.getParameter().stream()
                        .filter(p -> "accessCode".equals(p.getName()))
                        .count()
                    == 1;

    return new VerificationStep.StepBuilder<EuAccessPermission>(
            Requirement.custom("A_10406"), "Zugriffsberechtigung muss genau einmal vorhanden sein")
        .predicate(predicate)
        .accept();
  }

  /** Prüft, ob die AccessPermission den erwarteten AccessCode enthält. */
  public static VerificationStep<EuAccessPermission> hasPermissionWithAccessCode(
      EuAccessCode expected) {
    Predicate<EuAccessPermission> predicate =
        perm -> expected.getValue().equals(perm.getAccessCode().getValue());

    return new VerificationStep.StepBuilder<EuAccessPermission>(
            Requirement.custom(A_10395),
            format("Zugriffsberechtigung muss AccessCode {0} enthalten", expected.getValue()))
        .predicate(predicate)
        .accept();
  }
}
