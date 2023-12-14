/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.cli.cfg.ConfigurationFactory;
import de.gematik.test.erezept.cli.param.EgkParameter;
import de.gematik.test.erezept.cli.param.EnvironmentParameter;
import de.gematik.test.erezept.cli.param.TaskStatusWrapper;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.client.usecases.search.TaskSearch;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "show",
    description = "show prescriptions",
    mixinStandardHelpOptions = true)
public class PrescriptionsViewer implements Callable<Integer> {

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

  @CommandLine.Mixin private EgkParameter egkParameter;

  @CommandLine.Mixin private EnvironmentParameter environmentParameter;

  @CommandLine.Option(
      names = {"--sort"},
      paramLabel = "<SORT>",
      type = SortOrder.class,
      description =
          "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query (default=${DEFAULT-VALUE})")
  private SortOrder sortOrder = SortOrder.DESCENDING;

  @CommandLine.Option(
      names = {"--status"},
      paramLabel = "<STATUS>",
      type = TaskStatusWrapper.class,
      description =
          "Task-Status from ${COMPLETION-CANDIDATES} for the Query (default=${DEFAULT-VALUE})")
  private TaskStatusWrapper taskStatus = TaskStatusWrapper.ANY;

  @Override
  public Integer call() throws Exception {
    val sca = SmartcardFactory.getArchive();
    val egks = egkParameter.getEgks(sca);
    val env = environmentParameter.getEnvironment();

    egks.forEach(egk -> this.performFor(env, egk));
    return 0;
  }

  private void performFor(EnvironmentConfiguration env, Egk egk) {
    val patientConfig = ConfigurationFactory.createPatientConfigurationFor(egk);
    val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
    erpClient.authenticateWith(egk);
    log.info(format("Show prescriptions for {0} from {1}", egk.getKvnr(), env.getName()));

    val cmd =
        TaskSearch.builder()
            .sortedByAuthoredOn(sortOrder)
            .withStatus(taskStatus.getStatus())
            .createCommand();
    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();

    val tasks =
        bundle.getTasks().stream()
            .filter(task -> task.getStatus() != Task.TaskStatus.CANCELLED)
            .toList();
    val size = tasks.size();
    val ownerName = egk.getOwner().getOwnerName();

    System.out.println(
        format(
            "Received {0} Prescriptions(s) for {1} ({2}) in {3}\n",
            size, ownerName, egk.getKvnr(), env.getName()));
    tasks.forEach(task -> this.printPrescription(erpClient, task));
  }

  private void printPrescription(ErpClient erpClient, ErxTask task) {
    val cmd = new TaskGetByIdCommand(task.getTaskId());
    val prescription = erpClient.request(cmd).getExpectedResource();

    val prescriptionId = prescription.getTask().getPrescriptionId();
    val kbvBundle = prescription.getKbvBundle();
    val medication = kbvBundle.getMedication();
    val medicationRequest = kbvBundle.getMedicationRequest();
    val coverage = kbvBundle.getCoverage();
    val practitioner = kbvBundle.getPractitioner();
    val practitionerOrg = kbvBundle.getMedicalOrganization();

    System.out.println(
        format(
            "=> {0} Prescription: {1} ({2})",
            prescription.getTask().getStatus(),
            prescriptionId.getValue(),
            prescriptionId.getSystem().getCanonicalUrl()));
    System.out.println(format("{0}", medicationRequest.getDescription()));
    System.out.println(
        format(
            "Kategorie: {0} / {1}",
            medication.getCategoryFirstRep().getCode(),
            medication.getCategoryFirstRep().getDisplay()));
    System.out.println(
        format(
            "Workflow {0}: {1} ausgestellt am {2} (zuletzt modifiziert am {3})",
            kbvBundle.getFlowType().getCode(),
            kbvBundle.getFlowType().getDisplay(),
            DATE_FORMATTER.format(task.getAuthoredOn()),
            DATE_FORMATTER.format(task.getLastModified())));
    printSubLine(
        format(
            "{0} {1} (PZN: {2})",
            medication.getMedicationName(),
            medication.getStandardSize().getCode(),
            medication.getPznFirstRep()));
    medication.getMedicationType().ifPresent(type -> printSubLine(format("Type: {0}", type)));
    medication.getIngredientText().ifPresent(text -> printSubLine(format("Ingredient: {0}", text)));
    medication
        .getIngredientStrengthString()
        .ifPresent(text -> printSubLine(format("Ingredient strength: {0}", text)));
    medication
        .getDarreichungsformFirstRep()
        .ifPresent(
            darreichungsform ->
                printSubLine(format("Darreichungsform: {0}", darreichungsform.getDisplay())));
    printSubLine(format("Impfung: {0}", medication.isVaccine()));
    System.out.println(coverage.getDescription());
    System.out.println(
        format("{0} / {1}", practitioner.getDescription(), practitionerOrg.getDescription()));

    System.out.println("-------------");
  }

  private void printSubLine(String line) {
    System.out.println(format("\t{0}", line));
  }
}
