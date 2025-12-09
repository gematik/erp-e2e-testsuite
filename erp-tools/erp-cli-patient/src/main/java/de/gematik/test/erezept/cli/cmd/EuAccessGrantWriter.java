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
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.cli.converter.StringListConverter;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.TaskPatchCommand;
import de.gematik.test.erezept.client.usecases.eu.EuGrantAccessPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuPatchTaskInputBuilder;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "grant",
    description = "grant EU Access Permission",
    mixinStandardHelpOptions = true)
public class EuAccessGrantWriter extends BaseRemoteCommand {

  @CommandLine.Option(
      names = {"--country"},
      paramLabel = "<CODE>",
      type = String.class,
      description =
          "ISO country code to grant the access permission for (default=${DEFAULT-VALUE})")
  private String countryCode = "LI";

  @CommandLine.Option(
      names = {"--pids"},
      paramLabel = "<Prescription IDs>",
      type = List.class,
      converter = StringListConverter.class,
      description = "Comma separated list of prescription ids to grant EU access for")
  private List<String> pids = List.of();

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        "Grant EU access permission status for {} in environment {}",
        egk.getKvnr(),
        this.getEnvironmentName());

    val ac = EuAccessCode.random();
    val country = IsoCountryCode.fromCode(countryCode);
    val cmd = new EuGrantAccessPostCommand(ac, country);
    val response = erpClient.request(cmd);

    if (response.isOperationOutcome()) {
      val message = OperationOutcomeExtractor.extractFrom(response.getAsOperationOutcome());
      System.out.println(
          format(
              "Error while granting EU Access permission for country {0}: {1}",
              country.getDisplay(), message));
    } else {
      System.out.println(
          format(
              "{0} ({1}) granted EU access permission for country {2} with AccessCode {3}",
              egk.getOwnerData().getOwnerName(),
              egk.getKvnr(),
              country.getDisplay(),
              ac.getValue()));
    }

    val accessPatchParams = EuPatchTaskInputBuilder.builder().build();
    pids.stream()
        .map(TaskId::from)
        .forEach(
            tid -> {
              val patch = new TaskPatchCommand(tid, accessPatchParams);
              val patchResponse = erpClient.request(patch);

              if (patchResponse.isOperationOutcome()) {
                val message =
                    OperationOutcomeExtractor.extractFrom(patchResponse.getAsOperationOutcome());
                System.out.println(
                    format(
                        "Error while granting EU Access permission for prescription {0}: {1}",
                        tid, message));
              } else {
                System.out.println(
                    format(
                        "successfully granted permission to handle prescription {0} in EU", tid));
              }
            });
  }
}
