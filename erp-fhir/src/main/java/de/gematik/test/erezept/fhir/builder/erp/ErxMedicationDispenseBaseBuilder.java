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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public abstract class ErxMedicationDispenseBaseBuilder<
        M extends ErxMedicationDispenseBase, B extends ResourceBuilder<M, B>>
    extends ResourceBuilder<M, B> {

  protected final SimpleDateFormat dateFormat10 = new SimpleDateFormat("yyyy-MM-dd");
  protected ErpWorkflowVersion erpWorkflowVersion;

  private final KVNR kvnr;
  private String performer;
  private PrescriptionId prescriptionId;
  private MedicationDispense.MedicationDispenseStatus status =
      MedicationDispense.MedicationDispenseStatus.COMPLETED;

  protected GemErpMedication medication;

  private Date whenHandedOver;

  protected ErxMedicationDispenseBaseBuilder(KVNR kvnr) {
    this.kvnr = kvnr;
    this.version(ErpWorkflowVersion.getDefaultVersion());
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public B version(ErpWorkflowVersion version) {
    this.erpWorkflowVersion = version;
    return self();
  }

  public B medication(GemErpMedication medication) {
    this.medication = medication;
    return self();
  }

  public B performerId(String performer) {
    this.performer = performer;
    return self();
  }

  public B prescriptionId(String prescriptionId) {
    return prescriptionId(new PrescriptionId(prescriptionId));
  }

  public B prescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return self();
  }

  public B status(String statusCode) {
    return status(MedicationDispense.MedicationDispenseStatus.fromCode(statusCode));
  }

  public B status(MedicationDispense.MedicationDispenseStatus status) {
    this.status = status;
    return self();
  }

  public B whenHandedOver(Date whenHandedOver) {
    this.whenHandedOver = whenHandedOver;
    return self();
  }

  @Override
  public abstract M build();

  protected void buildBase(ErxMedicationDispenseBase medDisp) {
    checkRequiredBase();

    medDisp.setStatus(status);

    if (whenHandedOver == null) {
      whenHandedOver = new Date();
    }
    // C_10834 whenHandedOver must be of format yyyy-MM-dd
    medDisp.getWhenHandedOverElement().setValueAsString(dateFormat10.format(whenHandedOver));

    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      log.warn(
          "building {} ({}) with Version {} is deprecated!",
          ErxMedicationDispense.class.getSimpleName(),
          ErpWorkflowStructDef.MEDICATION_DISPENSE.getCanonicalUrl(),
          ErpWorkflowVersion.V1_1_1.getVersion());
      val prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
      medDisp.setIdentifier(List.of(prescriptionIdentifier));

      val performerRef = new Reference();
      performerRef
          .getIdentifier()
          .setSystem(ErpWorkflowNamingSystem.TELEMATIK_ID.getCanonicalUrl())
          .setValue(performer);
      medDisp
          .getPerformer()
          .add(new MedicationDispense.MedicationDispensePerformerComponent(performerRef));
    } else {
      val prescriptionIdentifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
      medDisp.setIdentifier(List.of(prescriptionIdentifier));

      val performerRef = new Reference();
      performerRef
          .getIdentifier()
          .setSystem(ErpWorkflowNamingSystem.TELEMATIK_ID_SID.getCanonicalUrl())
          .setValue(performer);
      medDisp
          .getPerformer()
          .add(new MedicationDispense.MedicationDispensePerformerComponent(performerRef));
    }

    // set the subject and performer properly by version
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      val subjectIdentifier = DeBasisProfilNamingSystem.KVID.asIdentifier(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    } else if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) >= 0) {
      val subjectIdentifier = DeBasisProfilNamingSystem.KVID_GKV_SID.asIdentifier(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    } else {
      val system =
          prescriptionId.getFlowType().isGkvType()
              ? DeBasisProfilNamingSystem.KVID_GKV_SID.getCanonicalUrl()
              : DeBasisProfilNamingSystem.KVID_PKV_SID.getCanonicalUrl();
      val subjectIdentifier = new Identifier().setSystem(system).setValue(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    }
  }

  private void checkRequiredBase() {
    this.checkRequired(performer, "MedicationDispense requires a Performer");
    this.checkRequired(prescriptionId, "MedicationDispense requires a Prescription ID");
    this.checkRequired(kvnr, "MedicationDispense requires a KVNR of the receiver");
  }
}
