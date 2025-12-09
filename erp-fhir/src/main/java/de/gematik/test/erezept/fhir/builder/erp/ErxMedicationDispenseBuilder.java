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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public class ErxMedicationDispenseBuilder
    extends ErxMedicationDispenseBaseBuilder<
        ErxMedicationDispense, ErpWorkflowVersion, ErxMedicationDispenseBuilder> {

  private Date whenPrepared;
  private Boolean wasSubstituted;
  private final List<String> dosageInstructions = new LinkedList<>();
  private final List<String> notes = new LinkedList<>();

  // required because here we need to support KbvMedication and GemErpMedication at the same time
  private Medication baseMedication;
  private Medication.MedicationBatchComponent batch;

  protected ErxMedicationDispenseBuilder(KVNR kvnr) {
    super(kvnr);
    this.version(ErpWorkflowVersion.getDefaultVersion());
  }

  public static ErxMedicationDispenseBuilder forKvnr(KVNR kvnr) {
    return new ErxMedicationDispenseBuilder(kvnr);
  }

  public ErxMedicationDispenseBuilder whenPrepared(Date whenPrepared) {
    this.whenPrepared = whenPrepared;
    return this;
  }

  // this is required for older medication dispenses
  public ErxMedicationDispenseBuilder medication(KbvErpMedication medication) {
    this.baseMedication = medication;
    return this;
  }

  // required because here we need to support KbvMedication and GemErpMedication at the same time
  @Override
  public ErxMedicationDispenseBuilder medication(GemErpMedication medication) {
    this.baseMedication = medication;
    return this;
  }

  public ErxMedicationDispenseBuilder batch(String lotNumber, Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return batch(newBatch);
  }

  public ErxMedicationDispenseBuilder batch(Medication.MedicationBatchComponent batch) {
    this.batch = batch;
    return this;
  }

  public ErxMedicationDispenseBuilder wasSubstituted(Boolean wasSubstituted) {
    this.wasSubstituted = wasSubstituted;
    return this;
  }

  public ErxMedicationDispenseBuilder dosageInstruction(String instruction) {
    this.dosageInstructions.add(instruction);
    return this;
  }

  public ErxMedicationDispenseBuilder note(String note) {
    this.notes.add(note);
    return this;
  }

  @Override
  public ErxMedicationDispense build() {
    checkRequired();

    val medDisp =
        this.createResource(
            ErxMedicationDispense::new,
            ErpWorkflowStructDef.MEDICATION_DISPENSE_12,
            erpWorkflowVersion);
    buildBase(medDisp);

    // set medication properly by version
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_3) <= 0) {
      medDisp.getContained().add(this.baseMedication);
      medDisp.setMedication(new Reference("#" + this.baseMedication.getIdElement().getIdPart()));
    } else {
      medDisp.setMedication(new Reference(this.baseMedication.getIdPart()));
    }

    Optional.ofNullable(whenPrepared)
        .ifPresent(
            date ->
                medDisp.setWhenPreparedElement(new DateTimeType(date, TemporalPrecisionEnum.DAY)));
    Optional.ofNullable(batch).ifPresent(baseMedication::setBatch);
    Optional.ofNullable(wasSubstituted)
        .ifPresent(s -> medDisp.getSubstitution().setWasSubstituted(s));

    this.dosageInstructions.stream()
        .map(instruction -> new Dosage().setText(instruction))
        .forEach(medDisp::addDosageInstruction);

    this.notes.stream().map(note -> new Annotation().setText(note)).forEach(medDisp::addNote);

    return medDisp;
  }

  private void checkRequired() {
    this.checkRequired(baseMedication, "MedicationDispense requires a Medication");
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_3) <= 0) {
      if (this.baseMedication instanceof GemErpMedication) {
        throw new BuilderException(
            format(
                "in {0} is no {1} allowed",
                erpWorkflowVersion, GemErpMedication.class.getSimpleName()));
      }
    } else {
      if (this.baseMedication instanceof KbvErpMedication) {
        throw new BuilderException(
            format(
                "in {0} is no {1} allowed",
                erpWorkflowVersion, KbvErpMedication.class.getSimpleName()));
      }
    }
  }
}
