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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationPZNBuilderORIGINAL_BUILDER;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfClosePrescriptionOperation extends FhirResponseQuestion<ErxReceipt> {

  private final List<Map<String, String>> replacementMedications;
  private final PrescriptionToDispenseStrategy.Builder strategyBuilder;

  private final Boolean alreadyDispensedFlag;
  @Getter private PrescriptionToDispenseStrategy executedStrategy;

  private ResponseOfClosePrescriptionOperation(
      PrescriptionToDispenseStrategy.Builder strategyBuilder,
      List<Map<String, String>> replacementMedications,
      boolean alreadyDispensedFlag) {
    this.replacementMedications = replacementMedications;
    this.strategyBuilder = strategyBuilder;
    this.alreadyDispensedFlag = alreadyDispensedFlag;
  }

  public static ResponseOfDispenseMedicationOperationBuilder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfDispenseMedicationOperationBuilder fromStack(
      DequeStrategy dequeStrategy) {
    return new ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.withDequeue(dequeStrategy));
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

  @Override
  public ErpResponse<ErxReceipt> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    executedStrategy = strategyBuilder.initialize(prescriptionManager);

    CloseTaskCommand executedCommand;
    if (replacementMedications.isEmpty()) {
      if (alreadyDispensedFlag) {
        executedCommand = this.closeAlreadyDispensed(executedStrategy);

      } else {
        val kbvAsString = executedStrategy.getKbvBundleAsString();
        val kbvBundle = erpClientAbility.decode(KbvErpBundle.class, kbvAsString);

        executedCommand =
            this.dispensePrescribedMedication(
                executedStrategy, kbvBundle.getMedication(), smcb.getTelematikID());
      }
    } else {
      // if replacements were given, dispense the alternative medication(s)
      executedCommand =
          this.dispenseAlternativeMedications(executedStrategy, smcb.getTelematikID());
    }

    log.info(
        "Actor {} is asking for the response of {}",
        actor.getName(),
        executedCommand.getRequestLocator());
    return erpClientAbility.request(executedCommand);
  }

  private CloseTaskCommand dispensePrescribedMedication(
      PrescriptionToDispenseStrategy strategy, KbvErpMedication medication, String telematikId) {
    val taskId = strategy.getTaskId();
    val secret = strategy.getSecret();
    val prescriptionId = strategy.getPrescriptionId();
    val kvnr = strategy.getKvnr();

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3) <= 0) {
      val medicationDispense =
          ErxMedicationDispenseBuilder.forKvnr(kvnr)
              .prescriptionId(prescriptionId)
              .performerId(telematikId)
              .medication(medication)
              .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
              .whenPrepared(new Date())
              .whenHandedOver(new Date())
              .wasSubstituted(false)
              .build();
      return new CloseTaskCommand(taskId, secret, medicationDispense);

    } else {

      val lotNr = GemFaker.fakerLotNumber();
      val expDate = GemFaker.fakerFutureExpirationDate();
      val gemMedication =
          GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(medication).lotNumber(lotNr).build();

      val medicationDisp =
          ErxMedicationDispenseBuilder.forKvnr(strategy.getKvnr())
              .prescriptionId(strategy.getPrescriptionId())
              .performerId(telematikId)
              .batch(lotNr, expDate)
              .whenPrepared(new Date())
              .whenHandedOver(new Date())
              .wasSubstituted(false)
              .medication(gemMedication)
              .build();

      val closeParams =
          GemOperationInputParameterBuilder.forClosingPharmaceuticals()
              .with(medicationDisp, gemMedication)
              .build();
      return new CloseTaskCommand(taskId, secret, closeParams);
    }
  }

  private CloseTaskCommand closeAlreadyDispensed(PrescriptionToDispenseStrategy strategy) {
    val taskId = strategy.getTaskId();
    val secret = strategy.getSecret();
    return new CloseTaskCommand(taskId, secret);
  }

  private CloseTaskCommand dispenseAlternativeMedications(
      PrescriptionToDispenseStrategy strategy, String performerId) {
    val taskId = strategy.getTaskId();
    val secret = strategy.getSecret();

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3) <= 0) {
      val medicationDispenses = getAlternativeMedicationDispenses(strategy, performerId);
      return new CloseTaskCommand(taskId, secret, medicationDispenses);
    } else {
      val closeParameters = getAlternativeCloseParameterStructure(strategy, performerId);
      return new CloseTaskCommand(taskId, secret, closeParameters);
    }
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
          val sizeCode = medMap.getOrDefault("Normgröße", StandardSize.random().getCode());

          val medication =
              KbvErpMedicationPZNBuilder.builder()
                  .pzn(pzn, name)
                  .amount(amount, unit)
                  .category(MedicationCategory.fromCode(categoryCode))
                  .isVaccine(isVaccine)
                  .darreichungsform(Darreichungsform.fromCode(darreichungsCode))
                  .normgroesse(StandardSize.fromCode(sizeCode))
                  .build();

          val medicationDispense =
              ErxMedicationDispenseBuilder.forKvnr(strategy.getKvnr())
                  .prescriptionId(strategy.getPrescriptionId())
                  .performerId(performerId)
                  .medication(medication)
                  .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                  .wasSubstituted(true)
                  .build();

          medicationDispenses.add(medicationDispense);
        });

    return medicationDispenses;
  }

  private GemCloseOperationParameters getAlternativeCloseParameterStructure(
      PrescriptionToDispenseStrategy strategy, String performerId) {
    val paramsBuilder = GemOperationInputParameterBuilder.forClosingPharmaceuticals();

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
          val sizeCode = medMap.getOrDefault("Normgröße", StandardSize.random().getCode());

          val medication =
              GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder()
                  .pzn(pzn, name)
                  .amount(amount, unit)
                  .category(EpaDrugCategory.fromCode(categoryCode))
                  .isVaccine(isVaccine)
                  .darreichungsform(Darreichungsform.fromCode(darreichungsCode))
                  .normgroesse(StandardSize.fromCode(sizeCode))
                  .build();

          val medicationDisp =
              ErxMedicationDispenseBuilder.forKvnr(strategy.getKvnr())
                  .prescriptionId(strategy.getPrescriptionId())
                  .performerId(performerId)
                  .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                  .whenPrepared(new Date())
                  .whenHandedOver(new Date())
                  .wasSubstituted(true)
                  .medication(medication)
                  .build();

          paramsBuilder.with(medicationDisp, medication);
        });

    return paramsBuilder.build();
  }

  public static class ResponseOfDispenseMedicationOperationBuilder {
    private final PrescriptionToDispenseStrategy.Builder dequeStrategyBuilder;

    private ResponseOfDispenseMedicationOperationBuilder(
        PrescriptionToDispenseStrategy.Builder dequeStrategyBuilder) {
      this.dequeStrategyBuilder = dequeStrategyBuilder;
    }

    public ResponseOfClosePrescriptionOperation forPrescribedMedications() {
      return new ResponseOfClosePrescriptionOperation(dequeStrategyBuilder, List.of(), false);
    }

    public ResponseOfClosePrescriptionOperation forAlreadyDispensedMedication() {
      return new ResponseOfClosePrescriptionOperation(dequeStrategyBuilder, List.of(), true);
    }

    public ResponseOfClosePrescriptionOperation forAlternativeMedications(DataTable medications) {
      return forAlternativeMedications(medications.asMaps());
    }

    public ResponseOfClosePrescriptionOperation forAlternativeMedications(
        List<Map<String, String>> medications) {
      return new ResponseOfClosePrescriptionOperation(dequeStrategyBuilder, medications, false);
    }
  }
}
