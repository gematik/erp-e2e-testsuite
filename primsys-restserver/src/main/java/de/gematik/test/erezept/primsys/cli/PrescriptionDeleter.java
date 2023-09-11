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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.apimeasure.ApiCallStopwatch;
import de.gematik.test.erezept.apimeasure.DumpingStopwatch;
import de.gematik.test.erezept.apimeasure.LoggingStopwatch;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.search.TaskSearch;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;

@Slf4j
public class PrescriptionDeleter implements Callable<Integer> {

  @CommandLine.Option(
      names = "--conf",
      paramLabel = "CONFIG",
      type = Path.class,
      required = true,
      description = "Path to a Configuration File")
  private Path config;

  @CommandLine.Option(
      names = "--env",
      paramLabel = "ENV",
      type = String.class,
      required = true,
      description = "Environment to delete prescriptions from")
  private String environment;

  @CommandLine.Option(
      names = "--kvnrs",
      paramLabel = "KVNRS",
      converter = StringListConverter.class,
      type = List.class,
      description = "List of KVNRs to delete")
  private List<String> kvnrs;

  @CommandLine.Option(
      names = "--dry-run",
      type = Boolean.class,
      description = "Dry-run will print existing prescriptions, no prescriptions will be deleted")
  private Boolean dryRun = false;

  @CommandLine.Option(
      names = "--measure-calls",
      type = Boolean.class,
      description = "Perform measurements of API-Calls")
  private Boolean measureCalls = false;

  private ApiCallStopwatch stopwatch;

  @Override
  public Integer call() throws Exception {
    stopwatch =
        measureCalls ? new DumpingStopwatch("prescription_deleter") : new LoggingStopwatch();
    int returnCode = 0;
    val configFile = config.toFile();
    log.info("Initialize Erp-Client with Config from " + configFile.getAbsolutePath());
    val cfg = TestsuiteConfiguration.getInstance(configFile);
    val smartcards = SmartcardFactory.getArchive();

    val env =
        cfg.getEnvironments().stream()
            .filter(envCfg -> envCfg.getName().equalsIgnoreCase(environment))
            .findFirst()
            .orElseThrow(
                () ->
                    new ConfigurationMappingException(
                        environment,
                        cfg.getEnvironments().stream()
                            .map(EnvironmentConfiguration::getName)
                            .toList()));

    val dummyConfig = cfg.getActors().getPatients().get(0);
    final List<Egk> egks;
    if (kvnrs == null || kvnrs.isEmpty()) {
      egks =
          cfg.getActors().getPatients().stream()
              .map(actor -> smartcards.getEgkByICCSN(actor.getEgkIccsn()))
              .toList();
    } else {
      egks = kvnrs.stream().map(smartcards::getEgkByKvnr).toList();
    }

    egks.forEach(
        egk -> {
          val erpClient = ErpClientFactory.createErpClient(env, dummyConfig);
          erpClient.authenticateWith(egk);

          log.info(
              format(
                  "Delete all Prescriptions for {0} with KVNR {1}",
                  egk.getOwner().getOwnerName(), egk.getKvnr()));
          if (dryRun) {
            showAllFor(erpClient, egk);
          } else {
            deleteAllFor(erpClient);
          }
        });

    stopwatch.close();
    return returnCode;
  }

  private void showAllFor(ErpClient client, Egk egk) {
    System.out.println(format("---- {0} / {1} ----", egk.getOwner().getOwnerName(), egk.getKvnr()));
    val getCmd = TaskSearch.getSortedByAuthoredOn(SortOrder.DESCENDING);
    val getResponse = this.request(client, getCmd);

    val taskBundle = getResponse.getExpectedResource();
    taskBundle
        .getTasks()
        .forEach(
            task -> {
              System.out.println(task);
              System.out.println(
                  format(
                      "\t{0} for {1} in status {2} authored-on {3}",
                      task.getPrescriptionId().getValue(),
                      task.getForKvnr().orElse(null),
                      task.getStatus(),
                      task.getAuthoredOn()));
            });
  }

  private void deleteAllFor(ErpClient client) {
    var hasPrescriptions = true;

    Comparator<ErxTask> authoredOnComparator = Comparator.comparing(ErxTask::getAuthoredOn);

    var counter = 1000;
    while (hasPrescriptions && counter > 0) {
      counter--;
      val getCmd = TaskSearch.getSortedByAuthoredOn(SortOrder.DESCENDING);
      val getResponse = this.request(client, getCmd);

      if (getResponse.isOperationOutcome()) {
        log.info(
            format(
                "Unexpected Response while getting all tasks: {0}", getResponse.getStatusCode()));
        break;
      }
      val taskBundle = getResponse.getExpectedResource();
      hasPrescriptions =
          taskBundle.getTasks().stream()
              .anyMatch(
                  task ->
                      task.getStatus() != TaskStatus.INPROGRESS
                          && task.getStatus() != TaskStatus.CANCELLED);

      taskBundle.getTasks().stream()
          .filter(task -> task.getStatus() != TaskStatus.INPROGRESS)
          .sorted(authoredOnComparator)
          .forEach(
              task -> {
                val id = task.getTaskId();
                val abortCmd = new TaskAbortCommand(id);
                val abortResponse = this.request(client, abortCmd);

                if (abortResponse.isOperationOutcome()) {
                  val oo = abortResponse.getAsOperationOutcome();
                  val issue = oo.getIssueFirstRep();
                  val issueText = issue.getDetails().getText();
                  val issueDiagnostics = issue.getDiagnostics();
                  log.warn(
                      format(
                          "Unable to delete {0} with status {1}: return code {2} / issue ''{3}'' / diagnostics ''{4}''",
                          id,
                          task.getStatus(),
                          abortResponse.getStatusCode(),
                          issueText,
                          issueDiagnostics));
                } else {
                  log.info(
                      format("Successfully deleted {0} from status {1}", id, task.getStatus()));
                }
              });
    }
  }

  private <R extends Resource> ErpResponse<R> request(ErpClient client, ICommand<R> command) {
    val response = client.request(command);
    stopwatch.measurement(client.getClientType(), command, response);
    return response;
  }

  public static class StringListConverter implements ITypeConverter<List<String>> {

    @Override
    public List<String> convert(String s) throws Exception {
      return List.of(s.split(","));
    }
  }
}
