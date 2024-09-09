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
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.cli.cfg.ConfigurationFactory;
import de.gematik.test.erezept.cli.param.EgkParameter;
import de.gematik.test.erezept.cli.param.EnvironmentParameter;
import de.gematik.test.erezept.cli.param.TaskStatusWrapper;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.search.TaskSearch;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "delete",
    description = "delete prescriptions",
    mixinStandardHelpOptions = true)
public class PrescriptionsDeleter implements Callable<Integer> {

  @CommandLine.Mixin private EgkParameter egkParameter;

  @CommandLine.Mixin private EnvironmentParameter environmentParameter;

  @CommandLine.Option(
      names = {"--sort"},
      paramLabel = "<SORT>",
      type = SortOrder.class,
      description =
          "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query"
              + " (default=${DEFAULT-VALUE})")
  private SortOrder sortOrder = SortOrder.DESCENDING;

  @CommandLine.Option(
      names = {"--status"},
      paramLabel = "<STATUS>",
      type = TaskStatusWrapper.class,
      description =
          "Task-Status from ${COMPLETION-CANDIDATES} for the Query (default=${DEFAULT-VALUE})")
  private TaskStatusWrapper taskStatus = TaskStatusWrapper.READY;

  @Override
  public Integer call() throws Exception {
    if (taskStatus == TaskStatusWrapper.INPROGRESS) {
      System.out.println(
          format(
              "ERROR: TaskStatus not allowed - delete prescriptions from status {0} is impossible",
              taskStatus));
      return -1;
    }

    val sca = SmartcardArchive.fromResources();
    val egks = egkParameter.getEgks(sca);
    val env = environmentParameter.getEnvironment();

    egks.forEach(egk -> this.performFor(env, egk));
    return 0;
  }

  private void performFor(EnvironmentConfiguration env, Egk egk) {
    val patientConfig = ConfigurationFactory.createPatientConfigurationFor(egk);
    val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
    erpClient.authenticateWith(egk);
    log.info(format("Delete prescriptions for {0} from {1}", egk.getKvnr(), env.getName()));

    val cmd =
        TaskSearch.builder()
            .sortedByAuthoredOn(sortOrder)
            .withStatus(taskStatus.getStatus())
            .createCommand();
    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();

    val tasks =
        bundle.getTasks().stream()
            .filter(
                task ->
                    task.getStatus() != Task.TaskStatus.INPROGRESS
                        && task.getStatus() != Task.TaskStatus.CANCELLED)
            .filter(
                task -> !PrescriptionId.from(task.getTaskId()).getFlowType().isDirectAssignment())
            .toList();
    val size = tasks.size();
    val ownerName = egk.getOwnerData().getOwnerName();

    System.out.println(
        format(
            "Found {0} deletable Prescriptions(s) for {1} ({2}) in {3}\n",
            size, ownerName, egk.getKvnr(), env.getName()));
    tasks.forEach(task -> this.deletePrescription(erpClient, task));
  }

  private void deletePrescription(ErpClient erpClient, ErxTask task) {
    System.out.println(
        format(
            "Delete {0} prescription {1}", task.getStatus(), task.getPrescriptionId().getValue()));
    val cmd = new TaskAbortCommand(task.getTaskId());
    val response = erpClient.request(cmd);

    if (response.isOperationOutcome()) {
      System.out.println(
          format(
              "\tError ({0}) while deleting Prescription {1}: {2}",
              response.getStatusCode(),
              task.getTaskId().getValue(),
              OperationOutcomeWrapper.extractFrom(response.getAsOperationOutcome())));
    } else {
      System.out.println(
          format(
              "\tSuccessfully ({0}) deleted Prescription {1}",
              response.getStatusCode(), task.getTaskId().getValue()));
    }
  }
}
