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
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "delete",
    description = "delete PKV Consent",
    mixinStandardHelpOptions = true)
public class ConsentDeleter extends BaseRemoteCommand {

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        format("Delete PKV consent for {0} from {1}", egk.getKvnr(), this.getEnvironmentName()));

    val cmd = new ConsentDeleteCommand();
    val response = erpClient.request(cmd);

    if (response.isOperationOutcome()) {
      val message = OperationOutcomeWrapper.extractFrom(response.getAsOperationOutcome());
      System.out.println(format("Consent could not be deleted: {0}", message));
    } else {
      System.out.println(
          format(
              "Consent deleted for {0} ({1})", egk.getOwnerData().getOwnerName(), egk.getKvnr()));
    }
  }
}
