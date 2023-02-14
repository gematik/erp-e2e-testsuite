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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFutureExpirationDate;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPzn;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class ErxMedicationDispenseBuilder
    extends AbstractResourceBuilder<ErxMedicationDispenseBuilder> {

  private final SimpleDateFormat dateFormat10 = new SimpleDateFormat("yyyy-MM-dd");
  private ErpWorkflowVersion erpWorkflowVersion = ErpWorkflowVersion.getDefaultVersion();
  private String kvid;
  private String performer;
  private PrescriptionId prescriptionId;
  private MedicationDispense.MedicationDispenseStatus status =
      MedicationDispense.MedicationDispenseStatus.COMPLETED;
  private KbvErpMedication medication;
  private Date whenHandedOver;
  private Date whenPrepared;

  private Medication.MedicationBatchComponent batch;

  public static ErxMedicationDispenseBuilder faker(
      String kvid, String performerId, PrescriptionId prescriptionId) {
    return faker(kvid, performerId, fakerPzn(), prescriptionId);
  }

  public static ErxMedicationDispenseBuilder faker(
      String kvid, String performerId, String pzn, PrescriptionId prescriptionId) {
    return faker(kvid, performerId, KbvErpMedicationBuilder.faker(pzn).build(), prescriptionId);
  }

  public static ErxMedicationDispenseBuilder faker(
      @NonNull String kvid,
      String performerId,
      KbvErpMedication medication,
      PrescriptionId prescriptionId) {
    val builder = forKvid(kvid);
    builder
        .medication(medication)
        .performerId(performerId)
        .prescriptionId(prescriptionId)
        .batch(fakerLotNumber(), fakerFutureExpirationDate());
    return builder;
  }

  public static ErxMedicationDispenseBuilder forKvid(@NonNull String kvid) {
    val b = new ErxMedicationDispenseBuilder();
    b.kvid = kvid;
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
    this.erpWorkflowVersion = version;
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

  public ErxMedicationDispenseBuilder whenHandedOver(@NonNull Date whenHandedOver) {
    this.whenHandedOver = whenHandedOver;
    return self();
  }

  public ErxMedicationDispenseBuilder whenPrepared(@NonNull Date whenPrepared) {
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

  public ErxMedicationDispense build() {
    checkRequired();
    val medDisp = new ErxMedicationDispense();

    CanonicalType profile;
    Identifier prescriptionIdentifier;
    Identifier subjectIdentifier;
    val performerRef = new Reference();
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      profile = ErpWorkflowStructDef.MEDICATION_DISPENSE.asCanonicalType(erpWorkflowVersion);
      prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
      subjectIdentifier =
          new Identifier().setSystem(DeBasisNamingSystem.KVID.getCanonicalUrl()).setValue(kvid);
      performerRef
          .getIdentifier()
          .setSystem(ErpWorkflowNamingSystem.TELEMATIK_ID.getCanonicalUrl())
          .setValue(performer);
    } else {
      prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
      profile =
          ErpWorkflowStructDef.MEDICATION_DISPENSE_12.asCanonicalType(erpWorkflowVersion, true);
      subjectIdentifier = new Identifier().setValue(kvid);

      // TODO: temporary hack
      if (prescriptionId.getValue().startsWith("16")) {
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

    return medDisp;
  }

  private void checkRequired() {
    this.checkRequired(medication, "MedicationDispense requires a Medication");
    this.checkRequired(performer, "MedicationDispense requires a Performer");
    this.checkRequired(prescriptionId, "MedicationDispense requires a Prescription ID");
    this.checkRequired(kvid, "MedicationDispense requires a KVID of the receiver");
  }
}
