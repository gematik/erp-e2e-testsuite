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

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.cli.param.TaskStatusWrapper;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.client.usecases.search.TaskSearch;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvBaseBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.text.SimpleDateFormat;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "show",
    description = "show prescriptions",
    mixinStandardHelpOptions = true)
public class PrescriptionsReader extends BaseRemoteCommand {

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

  @CommandLine.Option(
      names = {"--sort"},
      paramLabel = "<SORT>",
      type = SortOrder.class,
      description =
          "Sort-Order by Date from ${COMPLETION-CANDIDATES} for the Query"
              + " (default=${DEFAULT-VALUE})")
  private final SortOrder sortOrder = SortOrder.DESCENDING;

  @CommandLine.Option(
      names = {"--status"},
      paramLabel = "<STATUS>",
      type = TaskStatusWrapper.class,
      description =
          "Task-Status from ${COMPLETION-CANDIDATES} for the Query (default=${DEFAULT-VALUE})")
  private final TaskStatusWrapper taskStatus = TaskStatusWrapper.ANY;

  @CommandLine.Option(
      names = {"--max"},
      paramLabel = "MAX_COUNT",
      description = "Max number of prescriptions to fetch (default=${DEFAULT-VALUE})")
  private final Integer maxCounter = 50;

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        "Show prescriptions for {} ({}) from {}",
        egk.getOwnerData().getOwnerName(),
        egk.getKvnr(),
        this.getEnvironmentName());

    val cmd =
        TaskSearch.builder()
            .sortedByModified(sortOrder)
            .withStatus(taskStatus.getStatus())
            .withMaxCount(maxCounter)
            .createCommand();

    val response = erpClient.request(cmd);
    val bundle = response.getExpectedResource();

    val tasks = bundle.getTasks().stream().toList();
    val size = tasks.size();
    val ownerName = egk.getOwnerData().getOwnerName();

    System.out.println(
        format(
            "Received {0} Prescriptions(s) for {1} ({2}) in {3}\n",
            size, ownerName, egk.getKvnr(), this.getEnvironmentName()));
    tasks.forEach(task -> this.printPrescription(erpClient, task));
  }

  private void printPrescription(ErpClient erpClient, ErxTask task) {
    // print the common parts first
    val prescriptionId = task.getPrescriptionId();

    val headline = format("======> {0} {1} <======", task.getStatus(), prescriptionId.getValue());
    System.out.println(headline);
    System.out.println(
        format(
            "Workflow {0}: {1} ausgestellt am {2} (zuletzt modifiziert am {3})",
            prescriptionId.getFlowType().getCode(),
            prescriptionId.getFlowType().getDisplay(),
            DATE_FORMATTER.format(task.getAuthoredOn()),
            DATE_FORMATTER.format(task.getLastModified())));

    // print full detail only for prescriptions which are NOT cancelled
    if (task.getStatus() != Task.TaskStatus.CANCELLED) {
      printPrescriptionAvailable(erpClient, task);
    }
    System.out.println("------------------------------");
  }

  private void printPrescriptionAvailable(ErpClient erpClient, ErxTask task) {
    val cmd = new TaskGetByIdCommand(task.getTaskId());
    val prescription = erpClient.request(cmd).getExpectedResource();

    val prescriptionId = prescription.getTask().getPrescriptionId();

    if (prescriptionId.getFlowType().equals(PrescriptionFlowType.FLOW_TYPE_162)) {
      printEvdgaBundle(erpClient, task, prescription);
    } else {
      printKbvBundle(erpClient, task, prescription);
    }
  }

  private void printEvdgaBundle(
      ErpClient erpClient, ErxTask task, ErxPrescriptionBundle prescription) {
    val evdgaBundle =
        prescription
            .getEvdgaBundle()
            .orElseThrow(
                () -> new MissingFieldException(KbvErpBundle.class, KbvItvEvdgaStructDef.BUNDLE));

    printKbvBaseBundle(evdgaBundle);
    resourcePrinter.print(evdgaBundle.getHealthAppRequest());
    printMedicationDispenses(erpClient, task);
  }

  private void printKbvBundle(
      ErpClient erpClient, ErxTask task, ErxPrescriptionBundle prescription) {
    val kbvBundle =
        prescription
            .getKbvBundle()
            .orElseThrow(
                () -> new MissingFieldException(KbvErpBundle.class, KbvItaErpStructDef.BUNDLE));

    printKbvBaseBundle(kbvBundle);

    val medicationRequest = kbvBundle.getMedicationRequest();

    System.out.println(format("{0}", medicationRequest.getDescription()));
    resourcePrinter.print(kbvBundle.getMedication());
    printMedicationDispenses(erpClient, task);
  }

  private void printKbvBaseBundle(KbvBaseBundle bundle) {
    val coverage = bundle.getCoverage();
    val practitioner = bundle.getPractitioner();
    val practitionerOrg = bundle.getMedicalOrganization();

    System.out.println(
        format(
            "Practitioner: {0} / {1}",
            practitioner.getDescription(), practitionerOrg.getDescription()));
    System.out.println(format("Coverage: {0}", coverage.getDescription()));
  }

  private void printMedicationDispenses(ErpClient erpClient, ErxTask task) {
    val pid = task.getPrescriptionId();
    if (task.getStatus() == Task.TaskStatus.COMPLETED) {
      val medDispCmd = new MedicationDispenseSearchByIdCommand(task.getPrescriptionId());
      val medDispBundle = erpClient.request(medDispCmd).getExpectedResource();
      resourcePrinter.print(medDispBundle);
    }
  }
}
