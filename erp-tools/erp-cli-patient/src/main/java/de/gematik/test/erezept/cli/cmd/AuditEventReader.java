/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.param.EgkParameter;
import de.gematik.test.erezept.cli.param.EnvironmentParameter;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.search.AuditEventSearch;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.util.concurrent.Callable;
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
public class AuditEventReader implements Callable<Integer> {

  @CommandLine.Mixin private EgkParameter egkParameter;

  @CommandLine.Mixin private EnvironmentParameter environmentParameter;

  @CommandLine.Option(
          names = {"--sort"},
          paramLabel = "<SORT>",
          type = SortOrder.class,
          description = "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query (default=${DEFAULT-VALUE})")
  private SortOrder sortOrder = SortOrder.DESCENDING;
  
  @Override
  public Integer call() throws Exception {
    val sca = SmartcardFactory.getArchive();
    val egks = egkParameter.getEgks(sca);
    val env = environmentParameter.getEnvironment();

    egks.forEach(egk -> this.performFor(env, egk));
    return 0;
  }

  private void performFor(EnvironmentConfiguration env, Egk egk) {
    val patientConfig = createPatientConfig(egk);
    val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
    erpClient.authenticateWith(egk);
    log.info(format("Read AuditEvents for {0} from {1}", egk.getKvnr(), env.getName()));

    val cmd = AuditEventSearch.getAuditEvents(sortOrder);
    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();
    
    val auditEvents = bundle.getAuditEvents();
    val size = auditEvents.size();
    val ownerName = egk.getOwner().getOwnerName();
    
    System.out.println(format("Received {0} AuditEvent(s) for {1} ({2}) in {3}\n", size, ownerName, egk.getKvnr(), env.getName()));
    auditEvents.forEach(this::printAuditEvent);
  }

  private void printAuditEvent(ErxAuditEvent auditEvent) {
    val profile =
            auditEvent.getMeta().getProfile().stream()
                    .map(PrimitiveType::asStringValue)
                    .collect(Collectors.joining(", "));

    val typeCode = auditEvent.getType().getCode().toUpperCase();
    val subTypeCode = auditEvent.getSubtypeFirstRep().getCode().toUpperCase();
    val description = auditEvent.getEntityFirstRep().getDescription();
    System.out.println(format("{0} {1} on {2} for {3}", typeCode, subTypeCode, auditEvent.getRecorded(), description));
    System.out.println(format("ID {0} of type {1}", auditEvent.getIdPart(), profile));
    System.out.println(format("From: {0} ({1})", auditEvent.getAgentName(), auditEvent.getAgentId()));
    System.out.println(format("\t{0}", auditEvent.getFirstText()));
    System.out.println("----------");
  }
  
  private PatientConfiguration createPatientConfig(Egk egk) {
    val pcfg = new PatientConfiguration();
    pcfg.setName(egk.getOwner().getGivenName());
    return pcfg;
  }
}
