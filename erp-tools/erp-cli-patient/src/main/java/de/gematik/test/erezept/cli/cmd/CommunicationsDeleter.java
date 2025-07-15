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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "delete",
    description = "delete communications",
    mixinStandardHelpOptions = true)
public class CommunicationsDeleter extends BaseRemoteCommand {

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
    val searchBuilder = CommunicationSearch.searchFor();
    searchBuilder.sender(egk.getKvnr());

    Optional.ofNullable(receiver).ifPresent(searchBuilder::recipient);

    val cmd = searchBuilder.sortedBySendDate(sortOrder);
    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();
    val communications = bundle.getCommunications();

    communications.forEach(com -> this.deleteCommunication(erpClient, com));
  }

  private void deleteCommunication(ErpClient erpClient, ErxCommunication com) {
    System.out.println(
        format("Delete {0} for {1}", com.getType().name(), com.getBasedOnReferenceId().getValue()));
    val cmd = new CommunicationDeleteCommand(com.getUnqualifiedId());
    val response = erpClient.request(cmd);
    System.out.println(format("\t Status Code: {0}", response.getStatusCode()));
  }
}
