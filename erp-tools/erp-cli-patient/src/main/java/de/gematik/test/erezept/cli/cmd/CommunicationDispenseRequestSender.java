/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.cli.converter.PrescriptionIdConverter;
import de.gematik.test.erezept.cli.exceptions.CliException;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.io.File;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@CommandLine.Command(
    name = "request",
    description = "send a communication dispense request for given prescription",
    mixinStandardHelpOptions = true)
public class CommunicationDispenseRequestSender extends BaseRemoteCommand {

  @CommandLine.Option(
      names = {"--receiver", "--to"},
      required = true,
      paramLabel = "<TELEMATIK-ID>",
      type = String.class,
      description = "Telematik-ID of the receiver")
  private String receiver;

  @CommandLine.Option(
      names = {"--prescriptionid", "--pid"},
      required = true,
      paramLabel = "<PRESCRIPTION-ID>",
      type = PrescriptionId.class,
      converter = PrescriptionIdConverter.class,
      description = "The id of the corresponding prescription")
  private PrescriptionId prescriptionId;

  @CommandLine.Parameters(
      paramLabel = "<COMMUNICATION-PAYLOAD-FILE>",
      type = File.class,
      description = "Communication payload file to send (JSON)")
  private File communicationPayloadFile;

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    val taskId = prescriptionId.toTaskId();
    val prescription = erpClient.request(new TaskGetByIdCommand(taskId)).getExpectedResource();

    val dispReq = createCommunicationFor(prescription);
    val sendDispReq = new CommunicationPostCommand(dispReq);

    val response = erpClient.request(sendDispReq).getExpectedResource();
    System.out.println(format("Sent DispenseRequest for {0}", taskId.getValue()));
    System.out.println(format("\tSender {0}", egk.getKvnr()));
    System.out.println(format("\tReceiver {0}", receiver));
  }

  private ErxCommunication createCommunicationFor(ErxPrescriptionBundle prescription) {
    val accessCode = prescription.getTask().getAccessCode();
    val taskId = prescription.getTask().getTaskId();
    val flowType = taskId.getFlowType();
    val kvnr = prescription.getTask().getForKvnr().map(KVNR::getValue).orElse("");

    ErxCommunicationBuilder<?> builder;

    if (flowType.equals(PrescriptionFlowType.FLOW_TYPE_162)) {
      builder =
          ErxCommunicationBuilder.forDiGADispenseRequest()
              .basedOn(taskId, accessCode)
              .flowType(flowType);
    } else {
      val payloadMessage = createDispenseRequestMessage();
      builder =
          ErxCommunicationBuilder.forDispenseRequest(payloadMessage)
              .basedOn(taskId, accessCode)
              .sender(kvnr)
              .flowType(flowType);
    }

    return builder.receiver(receiver).sender(kvnr).build();
  }

  private CommunicationDisReqMessage createDispenseRequestMessage() {
    // Note: Optional for future extension, for now the communicationPayloadFile is required
    // because we don't have a faker for the CommunicationDisReqMessage yet
    return Optional.ofNullable(communicationPayloadFile)
        .map(
            it -> {
              try {
                return new JsonMapper().readValue(it, CommunicationDisReqMessage.class);
              } catch (JacksonException e) {
                throw new CliException(
                    format(
                        "Unable to read communication payload file {0}: {1}", it, e.getMessage()));
              }
            })
        .orElseGet(() -> CommunicationDisReqMessage.forV3().build());
  }
}
