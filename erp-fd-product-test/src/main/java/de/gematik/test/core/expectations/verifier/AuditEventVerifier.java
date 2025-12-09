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

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEventVerifier {
  private final List<Function<String, String>> replacements;

  public static VerificationStep<ErxAuditEventBundle> hasAuditEventAtPosition(
      ErxAuditEvent auditEvent, int position) {
    Predicate<ErxAuditEventBundle> predicate =
        bundle -> bundle.getAuditEvents().get(position).getId().equals(auditEvent.getId());
    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_24441.getRequirement(),
            format(
                "Die übergebenen AuditEventBundle muss an position {0} ein AuditEvent mit Id"
                    + " {1} enthalten",
                position, auditEvent.getId()))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxAuditEventBundle> bundleContainsLog(String logContent) {
    Predicate<ErxAuditEventBundle> predicate =
        auditEventBundle ->
            auditEventBundle.getAuditEvents().stream()
                .anyMatch(ae -> ae.getFirstText().contains(logContent));

    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_19284_11.getRequirement(),
            format("Ein Audit Event enthält die Information: {0}", logContent))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxAuditEventBundle> bundleContainsLogFor(
      PrescriptionId prescriptionId, String logContent) {

    Predicate<ErxAuditEventBundle> predicate =
        auditEventBundle ->
            auditEventBundle.getAuditEvents().stream()
                .filter(
                    ae -> ae.getEntity().get(0).getDescription().equals(prescriptionId.getValue()))
                .toList()
                .stream()
                .anyMatch(ae -> ae.getFirstText().contains(logContent));

    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_25962.getRequirement(),
            format(
                "Das Audit Event für die Prescription {0} enthält die Information: {1}",
                prescriptionId.getValue(), logContent))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxAuditEventBundle> bundleDoesNotContainLogFor(
      PrescriptionId prescriptionId, String logContent) {

    Predicate<ErxAuditEventBundle> predicate =
        auditEventBundle ->
            auditEventBundle.getAuditEvents().stream()
                .filter(
                    ae -> ae.getEntity().get(0).getDescription().equals(prescriptionId.getValue()))
                .toList()
                .stream()
                .noneMatch(ae -> ae.getFirstText().contains(logContent));

    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_25962.getRequirement(),
            format(
                "Das Audit Event für die Prescription {0} enthält nicht die Information: {1}",
                prescriptionId.getValue(), logContent))
        .predicate(predicate)
        .accept();
  }

  public static @NonNull Builder forPharmacy(PharmacyActor pharmacy) {
    val builder = new Builder();
    builder.getReplacements().add(text -> text.replace("{agentName}", pharmacy.getCommonName()));
    return builder;
  }

  private boolean corresponds(ErxAuditEvent event, Representation representation) {
    return unifyText(event.getFirstText()).equals(unifyText(representation.getText()))
        && representation.getAction().equals(event.getAction());
  }

  private String unifyText(String input) {
    return replacePlaceholder(input)
        .replace(" ", "")
        .replace("\n", "")
        .replace("\t", "")
        .replace("TEST-ONLY", "");
  }

  private String replacePlaceholder(String input) {
    AtomicReference<String> text = new AtomicReference<>(input);
    replacements.forEach(e -> text.set(e.apply(text.get())));
    return text.get();
  }

  /**
   * Verifies that the given `ErxAuditEventBundle` contains an `ErxAuditEvent` that matches the
   * specified representation and the recorded timestamp is after the given timestamp.
   *
   * @param representation the representation to match against the `ErxAuditEvent`
   * @param timestamp the timestamp to compare against the recorded time of the `ErxAuditEvent`
   * @return a `VerificationStep`
   */
  public VerificationStep<ErxAuditEventBundle> contains(
      Representation representation, Instant timestamp) {
    Predicate<ErxAuditEventBundle> predicate =
        bundle ->
            bundle.getAuditEvents().stream()
                .filter(ae -> ae.getRecorded().toInstant().isAfter(timestamp.minusSeconds(5)))
                .map(ae -> corresponds(ae, representation))
                .findAny()
                .orElse(false);
    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_19284.getRequirement(),
            format(
                "Die Liste der letzten Versichertenprotokolle (> {1}) enthält den Eintrag \"{0}\""
                    + " nicht  \"{0}\"",
                replacePlaceholder(representation.getText()),
                timestamp.truncatedTo(ChronoUnit.SECONDS)))
        .predicate(predicate)
        .accept();
  }

  @Getter
  public static class Builder {
    private final List<Function<String, String>> replacements = new ArrayList<>();

    public Builder withChecksum(String checksum) {
      replacements.add(text -> text.replace("{checksum}", checksum));
      return this;
    }

    public AuditEventVerifier build() {
      return new AuditEventVerifier(replacements);
    }
  }
}
