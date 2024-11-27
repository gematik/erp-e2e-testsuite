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

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.cli.converter.PrescriptionIdConverter;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "show",
    description = "show medication dispenses",
    mixinStandardHelpOptions = true)
public class MedicationDispenseReader extends BaseRemoteCommand {

  @CommandLine.Option(
      names = {"--prescriptionid", "--pid"},
      paramLabel = "<PRESCRIPTION-ID>",
      type = PrescriptionId.class,
      converter = PrescriptionIdConverter.class,
      description = "The id of the corresponding prescription")
  private PrescriptionId prescriptionId;

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        "Show prescriptions for {} ({}) from {}",
        egk.getOwnerData().getOwnerName(),
        egk.getKvnr(),
        this.getEnvironmentName());

    val cmd = new MedicationDispenseSearchByIdCommand(prescriptionId);

    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();
    resourcePrinter.printMedicationDispenses(bundle.getMedicationDispenses());
  }
}
