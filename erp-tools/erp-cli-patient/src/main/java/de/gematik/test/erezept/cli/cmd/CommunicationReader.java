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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "show",
    description = "show prescriptions",
    mixinStandardHelpOptions = true)
public class CommunicationReader extends BaseRemoteCommand {

  @CommandLine.Option(
      names = {"--sender"},
      paramLabel = "<ID>",
      type = String.class,
      description = "Filter for specific Sender-ID for the Query")
  private String sender;

  @CommandLine.Option(
      names = {"--receiver"},
      paramLabel = "<ID>",
      type = String.class,
      description = "Filter for specific Receiver-ID for the Query")
  private String receiver;

  @CommandLine.Option(
      names = {"--sort"},
      paramLabel = "<SORT>",
      type = SortOrder.class,
      description =
          "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query"
              + " (default=${DEFAULT-VALUE})")
  private SortOrder sortOrder = SortOrder.DESCENDING;

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        format(
            "Show Communications for {0} ({1}) from {2}",
            egk.getOwnerData().getOwnerName(), egk.getKvnr(), this.getEnvironmentName()));

    val searchBuilder = CommunicationSearch.searchFor();
    Optional.ofNullable(sender).ifPresent(searchBuilder::sender);
    Optional.ofNullable(receiver).ifPresent(searchBuilder::recipient);

    val cmd = searchBuilder.sortedBySendDate(sortOrder);
    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();
    val communications = bundle.getCommunications();
    val ownerName = egk.getOwnerData().getOwnerName();

    System.out.println(
        format(
            "Received {0} Communications(s) for {1} ({2}) in {3}\n",
            communications.size(), ownerName, egk.getKvnr(), this.getEnvironmentName()));
    communications.forEach(this::printCommunication);
  }

  private void printCommunication(ErxCommunication com) {
    System.out.println(format("=> {0} with ID: {1}", com.getType().name(), com.getUnqualifiedId()));
    System.out.println(format("\tBased on: {0}", com.getBasedOnReferenceId().getValue()));
    com.getBasedOnAccessCodeString()
        .ifPresent(ac -> System.out.println(format("\tAccessCode: {0}", ac)));
    System.out.println(format("\t{0} -> {1}", com.getSenderId(), com.getRecipientId()));
    System.out.println(format("\tsent: {0} -> received: {1}", com.getSent(), com.getReceived()));
    System.out.println(format("\t{0}", com.getMessage()));
  }
}
