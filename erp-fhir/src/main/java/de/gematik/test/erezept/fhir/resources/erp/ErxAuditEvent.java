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

package de.gematik.test.erezept.fhir.resources.erp;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.model.api.annotation.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

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
    PHARMACY_GET_TASK_SUCCESSFUL_WITH_CHECKSUM(
        "{agentName} hat mit Ihrer Gesundheitskarte alle Ihre einlösbaren E-Rezepte abgerufen "
            + "(Prüfziffer: {checksum}).",
        AuditEventAction.R),
    PHARMACY_GET_TASK_SUCCESSFUL_WITHOUT_CHECKSUM(
        "{agentName} hat mit Ihrer Gesundheitskarte alle Ihre einlösbaren E-Rezepte abgerufen."
            + "(Keine Prüfziffer vorhanden)",
        AuditEventAction.R),
    PHARMACY_GET_TASK_UNSUCCESSFUL(
        "{agentName} konnte aufgrund eines Fehlers Ihre E-Rezepte nicht mit Ihrer Gesundheitskarte abrufen.",
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
    val text = this.getText().getDiv().getFirstElement().getValueAsString();
    return text.replaceAll("</?div.*?>", "");
  }

  public String getAgentId() {
    val identifier = this.getAgentFirstRep().getWho().getIdentifier();
    return identifier.getValue();
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
