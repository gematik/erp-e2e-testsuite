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

package de.gematik.test.erezept.fhir.r4.erp;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.PrimitiveType;

/**
 * AuditEvent for logging of ePrescription Access
 *
 * @see <a href="https://simplifier.net/erezept-workflow/gemerxauditevent">Gem_erxAuditEvent</a>
 */
@Slf4j
@ResourceDef(name = "AuditEvent")
@SuppressWarnings({"java:S110"})
public class ErxAuditEvent extends AuditEvent {

  @AllArgsConstructor
  @Getter
  public enum Representation {
    PHARMACY_GET_TASK_SUCCESSFUL(
        "{agentName} hat mit Ihrer eGK die Liste der offenen E-Rezepte abgerufen.",
        AuditEventAction.R),
    PHARMACY_GET_TASK_SUCCESSFUL_PN3(
        "{agentName} hat mit Ihrer eGK die Liste der offenen E-Rezepte abgerufen. (Offline-Check"
            + " wurde akzeptiert)",
        AuditEventAction.R),
    PHARMACY_GET_TASK_UNSUCCESSFUL_PN3(
        "{agentName} konnte aufgrund eines Fehlerfalls nicht die Liste der offenen E-Rezepte mit"
            + " Ihrer eGK abrufen. (Offline-Check wurde nicht akzeptiert)",
        AuditEventAction.R),
    PHARMACY_GET_TASK_UNSUCCESSFUL(
        "{agentName} konnte aufgrund eines Fehlerfalls nicht die Liste der offenen E-Rezepte mit"
            + " Ihrer eGK abrufen.",
        AuditEventAction.R);

    private final String text;
    private final AuditEventAction action;
  }

  @Override
  public String toString() {
    val profile =
        this.getMeta().getProfile().stream()
            .map(PrimitiveType::asStringValue)
            .collect(Collectors.joining(", "));
    return format(
        "{0} from Profile {1} with Text {2}",
        this.getClass().getSimpleName(), profile, getFirstText());
  }

  public String getFirstText() {
    return this.getText().getDiv().getChildNodes().get(0).getContent();
  }

  public String getAgentId() {
    val identifier = this.getAgentFirstRep().getWho().getIdentifier();
    return identifier.getValue();
  }

  public Optional<PrescriptionId> getPrescriptionId() {
    return this.getEntity().stream()
        .map(entity -> entity.getWhat().getIdentifier())
        .filter(PrescriptionId::isPrescriptionId)
        .map(PrescriptionId::from)
        .findFirst();
  }

  public String getAgentName() {
    return this.getAgentFirstRep().getName();
  }

  public static ErxAuditEvent fromAuditEvent(AuditEvent other) {
    val erxAuditEvent = new ErxAuditEvent();
    other.copyValues(erxAuditEvent);
    return erxAuditEvent;
  }
}
