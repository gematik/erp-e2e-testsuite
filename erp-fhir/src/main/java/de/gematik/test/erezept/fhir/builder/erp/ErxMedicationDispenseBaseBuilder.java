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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public abstract class ErxMedicationDispenseBaseBuilder<
        M extends ErxMedicationDispenseBase,
        V extends ProfileVersion,
        B extends ResourceBuilder<M, B>>
    extends ResourceBuilder<M, B> {

  protected final SimpleDateFormat dateFormat10 = new SimpleDateFormat("yyyy-MM-dd");
  protected V erpWorkflowVersion;
  protected GemErpMedication medication;
  private KVNR kvnr;
  private String performer;
  private Reference practitionerRoleReference;
  private PrescriptionId prescriptionId;
  private MedicationDispense.MedicationDispenseStatus status =
      MedicationDispense.MedicationDispenseStatus.COMPLETED;
  private Date whenHandedOver = new Date();

  protected ErxMedicationDispenseBaseBuilder(KVNR kvnr) {
    this.kvnr = kvnr;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public B version(V version) {
    this.erpWorkflowVersion = version;
    return self();
  }

  public B medication(GemErpMedication medication) {
    this.medication = medication;
    return self();
  }

  public B performerId(TelematikID telematikID) {
    return performerId(telematikID.getValue());
  }

  public B performerId(String performer) {
    this.performer = performer;
    return self();
  }

  public B performerContains(PractitionerRole practitionerRoleForReferenz) {
    this.practitionerRoleReference = new Reference(practitionerRoleForReferenz.getId());
    return self();
  }

  public B prescriptionId(String prescriptionId) {
    return prescriptionId(PrescriptionId.from(prescriptionId));
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

    // C_10834 whenHandedOver must be of format yyyy-MM-dd
    medDisp.setWhenHandedOverElement(new DateTimeType(whenHandedOver, TemporalPrecisionEnum.DAY));

    medDisp.setIdentifier(List.of(this.prescriptionId.asIdentifier()));

    var performerRef = new Reference();
    Optional.ofNullable(this.performer)
        .ifPresent(
            p -> {
              performerRef.setIdentifier(
                  DeBasisProfilNamingSystem.TELEMATIK_ID_SID.asIdentifier(p));
              medDisp
                  .getPerformer()
                  .add(new MedicationDispense.MedicationDispensePerformerComponent(performerRef));
            });
    Optional.ofNullable(this.practitionerRoleReference)
        .ifPresent(pRR -> medDisp.addPerformer().setActor(pRR));

    // this kind of comparison is happened, caused by different .compare() implementations in ENUM
    // and ProfilVersion
    if (ErpWorkflowVersion.V1_4.compareTo(erpWorkflowVersion) >= 0
        || Stream.of(ErpWorkflowVersion.V1_3, ErpWorkflowVersion.V1_2)
            .anyMatch(wfV -> wfV.equals(erpWorkflowVersion))) {
      // older Versions
      val subjectIdentifier = DeBasisProfilNamingSystem.KVID_GKV_SID.asIdentifier(kvnr.getValue());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    } else {
      // since WorkflowProfile 1.5.2 KVNR-TYPE ist mandatory
      kvnr = kvnr.as(InsuranceTypeDe.GKV);
      val subjectIdentifier =
          kvnr.asIdentifier().setType(IdentifierTypeDe.KVZ10.asCodeableConcept());
      medDisp.getSubject().setIdentifier(subjectIdentifier);
    }
  }

  private void checkRequiredBase() {
    if (performer == null && practitionerRoleReference == null) {
      throw new BuilderException("MedicationDispense requires a Performer or a Reference");
    }
    this.checkRequired(prescriptionId, "MedicationDispense requires a Prescription ID");
    this.checkRequired(kvnr, "MedicationDispense requires a KVNR of the receiver");
  }
}
