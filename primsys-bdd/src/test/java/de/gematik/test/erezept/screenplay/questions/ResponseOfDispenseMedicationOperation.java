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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispenseMedicationCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.PrescriptionToDispenseStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.datatable.DataTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfDispenseMedicationOperation extends FhirResponseQuestion<ErxReceipt> {

  private final List<Map<String, String>> replacementMedications;
  private final PrescriptionToDispenseStrategy.Builder strategyBuilder;

  @Getter private PrescriptionToDispenseStrategy executedStrategy;

  private ResponseOfDispenseMedicationOperation(
      PrescriptionToDispenseStrategy.Builder strategyBuilder,
      List<Map<String, String>> replacementMedications) {
    super("Task/$close");
    this.replacementMedications = replacementMedications;
    this.strategyBuilder = strategyBuilder;
  }

  @Override
  public ErpResponse<ErxReceipt> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    executedStrategy = strategyBuilder.initialize(prescriptionManager);

    DispenseMedicationCommand executedCommand;
    if (replacementMedications.isEmpty()) {
      // if no replacements given, dispense the original medication
      val kbvAsString = executedStrategy.getKbvBundleAsString();
      val kbvBundle = erpClientAbility.decode(KbvErpBundle.class, kbvAsString);

      executedCommand =
          this.dispensePrescribedMedication(
              executedStrategy, kbvBundle.getMedication(), smcb.getTelematikID());
    } else {
      // if replacements were given, dispense the alternative medication(s)
      executedCommand =
          this.dispenseAlternativeMedications(executedStrategy, smcb.getTelematikID());
    }

    log.info(
        format(
            "Actor {0} is asking for the response of {1}",
            actor.getName(), executedCommand.getRequestLocator()));
    return erpClientAbility.request(executedCommand);
  }

  private DispenseMedicationCommand dispensePrescribedMedication(
      PrescriptionToDispenseStrategy strategy, KbvErpMedication medication, String telematikId) {
    val taskId = strategy.getTaskId();
    val secret = strategy.getSecret();
    val prescriptionId = strategy.getPrescriptionId();
    val kvnr = strategy.getKvnr();

    if (medication.getVersion().compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      log.info(
          format(
              "Creating a MedicationDispense containing an old Medication with version {0}",
              medication.getVersion().getVersion()));
    }

    val medicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(kvnr)
            .prescriptionId(prescriptionId)
            .performerId(telematikId)
            .medication(medication)
            .build();
    return new DispenseMedicationCommand(taskId, secret, medicationDispense);
  }

  private DispenseMedicationCommand dispenseAlternativeMedications(
      PrescriptionToDispenseStrategy strategy, String performerId) {
    val taskId = strategy.getTaskId();
    val secret = strategy.getSecret();

    val medicationDispenses = getAlternativeMedicationDispenses(strategy, performerId);
    return new DispenseMedicationCommand(taskId, secret, medicationDispenses);
  }

  private List<ErxMedicationDispense> getAlternativeMedicationDispenses(
      PrescriptionToDispenseStrategy strategy, String performerId) {
    val medicationDispenses = new ArrayList<ErxMedicationDispense>();

    replacementMedications.forEach(
        medMap -> {
          val pzn = medMap.getOrDefault("PZN", PZN.random().getValue());
          val name = medMap.getOrDefault("Name", GemFaker.fakerDrugName());
          val amount =
              Long.valueOf(medMap.getOrDefault("Menge", String.valueOf(GemFaker.fakerAmount())));
          val unit = medMap.getOrDefault("Einheit", "Stk");
          val categoryCode =
              medMap.getOrDefault(
                  "Kategorie", GemFaker.fakerValueSet(MedicationCategory.class).getCode());
          val isVaccine = Boolean.getBoolean(medMap.getOrDefault("Impfung", "false"));
          val darreichungsCode =
              medMap.getOrDefault(
                  "Darreichungsform", GemFaker.fakerValueSet(Darreichungsform.class).getCode());
          val sizeCode =
              medMap.getOrDefault(
                  "Normgröße", GemFaker.fakerValueSet(StandardSize.class).getCode());

          val medication =
              KbvErpMedicationPZNBuilder.builder()
                  .pzn(pzn, name)
                  .amount(amount, unit)
                  .category(MedicationCategory.fromCode(categoryCode))
                  .isVaccine(isVaccine)
                  .darreichungsform(Darreichungsform.fromCode(darreichungsCode))
                  .normgroesse(StandardSize.fromCode(sizeCode))
                  .build();

          if (medication.getVersion().compareTo(KbvItaErpVersion.V1_1_0) < 0) {
            log.info(
                format(
                    "Creating a MedicationDispense containing an old Medication with version {0}",
                    medication.getVersion().getVersion()));
          }

          val medicationDispense =
              ErxMedicationDispenseBuilder.forKvnr(strategy.getKvnr())
                  .prescriptionId(strategy.getPrescriptionId())
                  .performerId(performerId)
                  .medication(medication)
                  .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                  .build();

          medicationDispenses.add(medicationDispense);
        });

    return medicationDispenses;
  }

  public static ResponseOfDispenseMedicationOperationBuilder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfDispenseMedicationOperationBuilder fromStack(
      DequeStrategy dequeStrategy) {
    return new ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.withDequeue(dequeStrategy));
  }

  public static class ResponseOfDispenseMedicationOperationBuilder {
    private final PrescriptionToDispenseStrategy.Builder dequeStrategyBuilder;

    private ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.Builder dequeStrategyBuilder) {
      this.dequeStrategyBuilder = dequeStrategyBuilder;
    }

    public ResponseOfDispenseMedicationOperation forPrescribedMedications() {
      val replacementMedications =
          new ArrayList<Map<String, String>>(); // empty list signals no replacements!
      return new ResponseOfDispenseMedicationOperation(
          dequeStrategyBuilder, replacementMedications);
    }

    public ResponseOfDispenseMedicationOperation forAlternativeMedications(DataTable medications) {
      return forAlternativeMedications(medications.asMaps());
    }

    public ResponseOfDispenseMedicationOperation forAlternativeMedications(
        List<Map<String, String>> medications) {
      return new ResponseOfDispenseMedicationOperation(dequeStrategyBuilder, medications);
    }
  }

  public static ResponseOfDispenseMedicationOperationBuilder withSecret(
      String dequeue, String wrongSecret) {
    return withSecret(DequeStrategy.fromString(dequeue), wrongSecret);
  }

  public static ResponseOfDispenseMedicationOperationBuilder withSecret(
      DequeStrategy dequeue, String wrongSecret) {
    return new ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.withDequeue(dequeue).secret(wrongSecret));
  }

  public static ResponseOfDispenseMedicationOperationBuilder toKvnr(String dequeue, String kvnr) {
    return toKvnr(DequeStrategy.fromString(dequeue), KVNR.from(kvnr));
  }

  public static ResponseOfDispenseMedicationOperationBuilder toKvnr(
      DequeStrategy dequeue, KVNR kvnr) {
    return new ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.withDequeue(dequeue).kvnr(kvnr));
  }

  public static ResponseOfDispenseMedicationOperationBuilder toPatient(
      String dequeue, Actor patient) {
    return toPatient(DequeStrategy.fromString(dequeue), patient);
  }

  public static ResponseOfDispenseMedicationOperationBuilder toPatient(
      DequeStrategy dequeue, Actor patient) {
    return new ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.withDequeue(dequeue).patient(patient));
  }
}
