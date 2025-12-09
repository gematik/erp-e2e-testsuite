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

import de.gematik.bbriccs.fhir.codec.OperationOutcomeExtractor;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "set", description = "set EU Consent", mixinStandardHelpOptions = true)
public class EuConsentWriter extends BaseRemoteCommand {

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info("Set EU consent status for {} from {}", egk.getKvnr(), this.getEnvironmentName());

    val kvnr = KVNR.from(egk.getKvnr());
    val cmd = new EuConsentPostCommand(kvnr);
    val response = erpClient.request(cmd);

    if (response.isOperationOutcome()) {
      val message = OperationOutcomeExtractor.extractFrom(response.getAsOperationOutcome());
      System.out.println(format("Consent could not be set: {0}", message));
    } else {
      System.out.println(
          format("Consent set for {0} ({1})", egk.getOwnerData().getOwnerName(), egk.getKvnr()));
    }
  }
}
