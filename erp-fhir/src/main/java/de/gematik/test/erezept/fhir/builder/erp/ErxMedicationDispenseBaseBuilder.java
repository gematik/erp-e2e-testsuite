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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.toggle.FeatureConfiguration;
import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
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
        M extends ErxMedicationDispenseBase, B extends AbstractResourceBuilder<B>>
    extends AbstractResourceBuilder<B> {

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
    val isOldProfile = version == ErpWorkflowVersion.V1_1_1;
    val shouldOverwriteOldProfileVersion =
        new FeatureConfiguration().getBooleanToggle("erp.fhir.medicationdispense.overwrite_111");
    // this check is required because on old profiles we have 2 options here
    if (isOldProfile && shouldOverwriteOldProfileVersion) {
      this.erpWorkflowVersion = ErpWorkflowVersion.V1_2_0;
    } else {
      this.erpWorkflowVersion = version;
    }
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
          format(
              "building {0} ({1}) with Version {2} is deprecated!",
              ErxMedicationDispense.class.getSimpleName(),
              ErpWorkflowStructDef.MEDICATION_DISPENSE.getCanonicalUrl(),
              ErpWorkflowVersion.V1_1_1.getVersion()));
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
      val subjectIdentifier =
          new Identifier()
              .setSystem(DeBasisNamingSystem.KVID.getCanonicalUrl())
              .setValue(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    } else if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) >= 0) {
      val subjectIdentifier =
          new Identifier()
              .setSystem(DeBasisNamingSystem.KVID_GKV.getCanonicalUrl())
              .setValue(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    } else {
      val system =
          prescriptionId.getFlowType().isGkvType()
              ? DeBasisNamingSystem.KVID_GKV.getCanonicalUrl()
              : DeBasisNamingSystem.KVID_PKV.getCanonicalUrl();
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
