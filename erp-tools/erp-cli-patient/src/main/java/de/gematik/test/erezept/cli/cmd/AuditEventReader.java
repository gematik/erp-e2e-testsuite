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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.search.AuditEventSearch;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.PrimitiveType;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "audit",
    aliases = {"auditevent"},
    description = "Read Audit-Events from E-Rezept Backend",
    mixinStandardHelpOptions = true)
public class AuditEventReader extends BaseRemoteCommand {

  @CommandLine.Option(
      names = {"--sort"},
      paramLabel = "<SORT>",
      type = SortOrder.class,
      description =
          "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query"
              + " (default=${DEFAULT-VALUE})")
  private SortOrder sortOrder = SortOrder.DESCENDING;

  @CommandLine.Option(
      names = {"--id", "--prescription"},
      paramLabel = "<PRESCRIPTION ID>",
      type = String.class,
      description = "Prescription for which to get the AuditEvents")
  private String prescriptionId;

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(format("Read AuditEvents for {0} from {1}", egk.getKvnr(), this.getEnvironmentName()));

    val bundle = getAuditEvents(erpClient);

    val auditEvents = bundle.getAuditEvents();
    val size = auditEvents.size();
    val ownerName = egk.getOwnerData().getOwnerName();

    System.out.println(
        format(
            "Received {0} AuditEvent(s) for {1} ({2}) in {3}\n",
            size, ownerName, egk.getKvnr(), this.getEnvironmentName()));
    auditEvents.forEach(this::printAuditEvent);
  }

  private ErxAuditEventBundle getAuditEvents(ErpClient erpClient) {

    if (prescriptionId != null) {
      return erpClient
          .request(AuditEventSearch.getAuditEventsFor(PrescriptionId.from(prescriptionId)))
          .getExpectedResource();
    } else {
      return erpClient.request(AuditEventSearch.getAuditEvents(sortOrder)).getExpectedResource();
    }
  }

  private void printAuditEvent(ErxAuditEvent auditEvent) {
    val profile =
        auditEvent.getMeta().getProfile().stream()
            .map(PrimitiveType::asStringValue)
            .collect(Collectors.joining(", "));

    val typeCode = auditEvent.getType().getCode().toUpperCase();
    val subTypeCode = auditEvent.getSubtypeFirstRep().getCode().toUpperCase();
    val what =
        auditEvent
            .getPrescriptionId()
            .map(
                prescriptionId ->
                    format(
                        "{0} ({1})",
                        prescriptionId.getValue(), prescriptionId.getSystem().getCanonicalUrl()))
            .orElse(auditEvent.getEntityFirstRep().getDescription());

    System.out.println(
        format("{0} {1} on {2} for {3}", typeCode, subTypeCode, auditEvent.getRecorded(), what));
    System.out.println(format("ID {0} of type {1}", auditEvent.getIdPart(), profile));
    System.out.println(
        format("From: {0} ({1})", auditEvent.getAgentName(), auditEvent.getAgentId()));
    System.out.println(format("\t{0}", auditEvent.getFirstText()));
    System.out.println("----------");
  }
}
