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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.toggle.FeatureConfiguration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class ErxMedicationDispenseBuilder
    extends AbstractResourceBuilder<ErxMedicationDispenseBuilder> {

  private final SimpleDateFormat dateFormat10 = new SimpleDateFormat("yyyy-MM-dd");
  private ErpWorkflowVersion erpWorkflowVersion;
  private KVNR kvnr;
  private String performer;
  private PrescriptionId prescriptionId;
  private MedicationDispense.MedicationDispenseStatus status =
      MedicationDispense.MedicationDispenseStatus.COMPLETED;
  private KbvErpMedication medication;
  private Date whenHandedOver;
  private Date whenPrepared;
  private Boolean wasSubstituted;
  private final List<String> dosageInstructions = new LinkedList<>();
  private final List<String> notes = new LinkedList<>();

  private Medication.MedicationBatchComponent batch;

  private ErxMedicationDispenseBuilder() {
    this.version(ErpWorkflowVersion.getDefaultVersion());
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

  public static ErxMedicationDispenseBuilder forKvnr(@NonNull KVNR kvnr) {
    val b = new ErxMedicationDispenseBuilder();
    b.kvnr = kvnr;
    return b;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public ErxMedicationDispenseBuilder version(ErpWorkflowVersion version) {
    val isOldProfile = version == ErpWorkflowVersion.V1_1_1;
    val shouldOverwriteOldProfileVersion =
        new FeatureConfiguration().getBooleanToggle("erp.fhir.medicationdispense.overwrite_111");
    // this check is required because on old profiles we have 2 options here
    if (isOldProfile && shouldOverwriteOldProfileVersion) {
      this.erpWorkflowVersion = ErpWorkflowVersion.V1_2_0;
    } else {
      this.erpWorkflowVersion = version;
    }
    return this;
  }

  public ErxMedicationDispenseBuilder performerId(@NonNull String performer) {
    this.performer = performer;
    return self();
  }

  public ErxMedicationDispenseBuilder prescriptionId(@NonNull String prescriptionId) {
    return prescriptionId(new PrescriptionId(prescriptionId));
  }

  public ErxMedicationDispenseBuilder prescriptionId(@NonNull PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return self();
  }

  public ErxMedicationDispenseBuilder status(@NonNull String statusCode) {
    return status(MedicationDispense.MedicationDispenseStatus.fromCode(statusCode));
  }

  public ErxMedicationDispenseBuilder status(
      @NonNull MedicationDispense.MedicationDispenseStatus status) {
    this.status = status;
    return self();
  }

  public ErxMedicationDispenseBuilder medication(@NonNull KbvErpMedication medication) {
    this.medication = medication;
    return self();
  }

  public ErxMedicationDispenseBuilder whenHandedOver(Date whenHandedOver) {
    this.whenHandedOver = whenHandedOver;
    return self();
  }

  public ErxMedicationDispenseBuilder whenPrepared(Date whenPrepared) {
    this.whenPrepared = whenPrepared;
    return self();
  }

  public ErxMedicationDispenseBuilder batch(
      @NonNull String lotNumber, @NonNull Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return batch(newBatch);
  }

  public ErxMedicationDispenseBuilder batch(@NonNull Medication.MedicationBatchComponent batch) {
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

  public ErxMedicationDispense build() {
    checkRequired();
    val medDisp = new ErxMedicationDispense();

    CanonicalType profile;
    Identifier prescriptionIdentifier;
    Identifier subjectIdentifier;
    val performerRef = new Reference();
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      log.warn(
          format(
              "building {0} ({1}) with Version {2} is deprecated!",
              ErxMedicationDispense.class.getSimpleName(),
              ErpWorkflowStructDef.MEDICATION_DISPENSE.getCanonicalUrl(),
              ErpWorkflowVersion.V1_1_1.getVersion()));
      profile = ErpWorkflowStructDef.MEDICATION_DISPENSE.asCanonicalType(erpWorkflowVersion);
      prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
      subjectIdentifier =
          new Identifier()
              .setSystem(DeBasisNamingSystem.KVID.getCanonicalUrl())
              .setValue(kvnr.getValue());
      performerRef
          .getIdentifier()
          .setSystem(ErpWorkflowNamingSystem.TELEMATIK_ID.getCanonicalUrl())
          .setValue(performer);
    } else {
      prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
      profile =
          ErpWorkflowStructDef.MEDICATION_DISPENSE_12.asCanonicalType(erpWorkflowVersion, true);
      subjectIdentifier = new Identifier().setValue(kvnr.getValue());

      if (prescriptionId.getFlowType().isGkvType()) {
        subjectIdentifier.setSystem(DeBasisNamingSystem.KVID_GKV.getCanonicalUrl());
      } else {
        subjectIdentifier.setSystem(DeBasisNamingSystem.KVID_PKV.getCanonicalUrl());
      }

      performerRef
          .getIdentifier()
          .setSystem(ErpWorkflowNamingSystem.TELEMATIK_ID_SID.getCanonicalUrl())
          .setValue(performer);
    }

    val meta = new Meta().setProfile(List.of(profile));
    // set FHIR-specific values provided by HAPI
    medDisp.setId(this.getResourceId()).setMeta(meta);

    medDisp.setIdentifier(List.of(prescriptionIdentifier));
    medDisp.getSubject().setIdentifier(subjectIdentifier);

    medDisp
        .getPerformer()
        .add(new MedicationDispense.MedicationDispensePerformerComponent(performerRef));
    medDisp.setStatus(status);
    medDisp.getContained().add(medication);
    medDisp.setMedication(new Reference("#" + medication.getIdElement().getIdPart()));

    if (whenHandedOver == null) {
      whenHandedOver = new Date();
    }
    // C_10834 whenHandedOver must be of format yyyy-MM-dd
    medDisp.getWhenHandedOverElement().setValueAsString(dateFormat10.format(whenHandedOver));

    if (whenPrepared != null) {
      medDisp.getWhenPreparedElement().setValueAsString(dateFormat10.format(whenPrepared));
    }

    if (batch != null) {
      medication.setBatch(batch);
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
    this.checkRequired(medication, "MedicationDispense requires a Medication");
    this.checkRequired(performer, "MedicationDispense requires a Performer");
    this.checkRequired(prescriptionId, "MedicationDispense requires a Prescription ID");
    this.checkRequired(kvnr, "MedicationDispense requires a KVNR of the receiver");
  }
}
