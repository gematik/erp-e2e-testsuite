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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
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

  private boolean corresponds(ErxAuditEvent event, Representation representation) {
    return unifyText(event.getFirstText()).equals(unifyText(representation.getText()))
        && representation.getAction().equals(event.getAction());
  }

  private String unifyText(String input) {
    return replacePlaceholder(input).replace(" ", "").replace("\n", "").replace("\t", "");
  }

  private String replacePlaceholder(String input) {
    AtomicReference<String> text = new AtomicReference<>(input);
    replacements.forEach(e -> text.set(e.apply(text.get())));
    return text.get();
  }

  public VerificationStep<ErxAuditEventBundle> firstElementCorrespondsTo(
      Representation representation) {
    Predicate<ErxAuditEventBundle> predicate =
        bundle -> {
          val auditEvents = bundle.getAuditEvents();
          if (auditEvents.isEmpty()) {
            return false;
          }
          val firstAuditEvent = auditEvents.get(0);
          log.info(
              format(
                  "Der erste Eintrag im Versichertenprotokoll entspricht \"{0}\"",
                  firstAuditEvent.getFirstText()));
          return corresponds(firstAuditEvent, representation);
        };
    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_19284.getRequirement(),
            format(
                "Der erste Eintrag im Versichertenprotokoll entspricht nicht  \"{0}\"",
                replacePlaceholder(representation.getText())))
        .predicate(predicate)
        .accept();
  }

  public VerificationStep<ErxAuditEventBundle> contains(Representation representation) {
    Predicate<ErxAuditEventBundle> predicate =
        bundle -> {
          val auditEvents = bundle.getAuditEvents();
          return auditEvents.stream().anyMatch(ae -> corresponds(ae, representation));
        };

    return new VerificationStep.StepBuilder<ErxAuditEventBundle>(
            ErpAfos.A_19284.getRequirement(),
            format(
                "Im Versichertenprotokoll wurde folgender Eintrag nicht gefunden: {0}",
                replacePlaceholder(representation.getText())))
        .predicate(predicate)
        .accept();
  }

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

  public static @NonNull Builder forPharmacy(PharmacyActor pharmacy) {
    val builder = new Builder();
    builder.getReplacements().add(text -> text.replace("{agentName}", pharmacy.getCommonName()));
    return builder;
  }
}
