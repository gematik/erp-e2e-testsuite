/*
 * Copyright (c) 2022 gematik GmbH
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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class ErxMedicationDispenseBuilder
    extends AbstractResourceBuilder<ErxMedicationDispenseBuilder> {

  private final SimpleDateFormat dateFormat10 = new SimpleDateFormat("yyyy-MM-dd");

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

    val profile = ErpStructureDefinition.GEM_MEDICATION_DISPENSE.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    medDisp.setId(this.getResourceId()).setMeta(meta);

    val pidIdentifier =
        new Identifier()
            .setSystem(prescriptionId.getSystemAsString())
            .setValue(prescriptionId.getValue());
    medDisp.setIdentifier(List.of(pidIdentifier));
    medDisp
        .getSubject()
        .setIdentifier(
            new Identifier().setSystem(ErpNamingSystem.KVID.getCanonicalUrl()).setValue(kvid));

    val performerRef = new Reference();
    performerRef
        .getIdentifier()
        .setSystem(ErpNamingSystem.TELEMATIK_ID.getCanonicalUrl())
        .setValue(performer);
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
