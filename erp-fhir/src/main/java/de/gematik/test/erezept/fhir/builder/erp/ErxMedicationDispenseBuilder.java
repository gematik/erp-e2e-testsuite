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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFutureExpirationDate;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public class ErxMedicationDispenseBuilder
    extends ErxMedicationDispenseBaseBuilder<ErxMedicationDispense, ErxMedicationDispenseBuilder> {

  private Date whenPrepared;
  private Boolean wasSubstituted;
  private final List<String> dosageInstructions = new LinkedList<>();
  private final List<String> notes = new LinkedList<>();

  // required because here we need to support KbvMedication and GemErpMedication at the same time
  private Medication baseMedication;
  private Medication.MedicationBatchComponent batch;

  protected ErxMedicationDispenseBuilder(KVNR kvnr) {
    super(kvnr);
  }

  @Deprecated(forRemoval = true)
  public static ErxMedicationDispenseBuilder faker(
      KVNR kvnr, String performerId, PrescriptionId prescriptionId) {
    return faker(kvnr, performerId, PZN.random().getValue(), prescriptionId);
  }

  @Deprecated(forRemoval = true)
  public static ErxMedicationDispenseBuilder faker(
      KVNR kvnr, String performerId, String pzn, PrescriptionId prescriptionId) {
    return faker(
        kvnr,
        performerId,
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake(),
        prescriptionId);
  }

  @Deprecated(forRemoval = true)
  public static ErxMedicationDispenseBuilder faker(
      @NonNull KVNR kvnr,
      String performerId,
      KbvErpMedication medication,
      PrescriptionId prescriptionId) {
    val builder = forKvnr(kvnr);
    builder
        .medication(medication)
        .performerId(performerId)
        .prescriptionId(prescriptionId)
        .batch(fakerLotNumber(), fakerFutureExpirationDate());
    return builder;
  }

  public static ErxMedicationDispenseBuilder forKvnr(KVNR kvnr) {
    return new ErxMedicationDispenseBuilder(kvnr);
  }

  public ErxMedicationDispenseBuilder whenPrepared(Date whenPrepared) {
    this.whenPrepared = whenPrepared;
    return self();
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
    return self();
  }

  public ErxMedicationDispenseBuilder wasSubstituted(Boolean wasSubstituted) {
    this.wasSubstituted = wasSubstituted;
    return self();
  }

  public ErxMedicationDispenseBuilder dosageInstruction(String instruction) {
    this.dosageInstructions.add(instruction);
    return self();
  }

  public ErxMedicationDispenseBuilder note(String note) {
    this.notes.add(note);
    return self();
  }

  @Override
  public ErxMedicationDispense build() {
    checkRequired();
    val medDisp = new ErxMedicationDispense();
    buildBase(medDisp);

    CanonicalType profile;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      log.warn(
          format(
              "building {0} ({1}) with Version {2} is deprecated!",
              ErxMedicationDispense.class.getSimpleName(),
              ErpWorkflowStructDef.MEDICATION_DISPENSE.getCanonicalUrl(),
              ErpWorkflowVersion.V1_1_1.getVersion()));
      profile = ErpWorkflowStructDef.MEDICATION_DISPENSE.asCanonicalType(erpWorkflowVersion);
    } else {
      profile =
          ErpWorkflowStructDef.MEDICATION_DISPENSE_12.asCanonicalType(erpWorkflowVersion, true);
    }

    val meta = new Meta().setProfile(List.of(profile));
    // set FHIR-specific values provided by HAPI
    medDisp.setId(this.getResourceId()).setMeta(meta);

    // set medication properly by version
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) < 0) {
      medDisp.getContained().add(this.baseMedication);
      medDisp.setMedication(new Reference("#" + this.baseMedication.getIdElement().getIdPart()));
    } else {
      medDisp.setMedication(new Reference(this.baseMedication.getIdPart()));
    }

    if (whenPrepared != null) {
      medDisp.getWhenPreparedElement().setValueAsString(dateFormat10.format(whenPrepared));
    }

    if (batch != null) {
      this.baseMedication.setBatch(batch);
    }

    if (wasSubstituted != null) {
      medDisp.getSubstitution().setWasSubstituted(wasSubstituted);
    }

    this.dosageInstructions.stream()
        .map(instruction -> new Dosage().setText(instruction))
        .forEach(medDisp::addDosageInstruction);

    this.notes.stream().map(note -> new Annotation().setText(note)).forEach(medDisp::addNote);

    return medDisp;
  }

  private void checkRequired() {
    this.checkRequired(baseMedication, "MedicationDispense requires a Medication");
  }
}
