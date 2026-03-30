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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.dgmp.RenderedDosageInstructionUtil.createGeneratorExtension;
import static de.gematik.test.erezept.fhir.builder.dgmp.RenderedDosageInstructionUtil.render;
import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef.*;
import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef.SER_EXTENSION;
import static de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem.UCUM;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.builder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.TeratogenicExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.DgMPStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.*;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class KbvErpMedicationRequestBuilder
    extends ResourceBuilder<KbvErpMedicationRequest, KbvErpMedicationRequestBuilder> {

  private final List<Extension> extensions = new LinkedList<>();
  private final List<DosageDgMP> dosageDgMPS = new ArrayList<>();
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  @Nullable private MedicationType medicationType;
  private Reference medicationReference;
  private Reference subjectReference;
  private Reference requesterReference;
  private Reference insuranceReference;
  private AccidentExtension accident;
  private MedicationRequest.MedicationRequestStatus requestStatus =
      MedicationRequest.MedicationRequestStatus.ACTIVE;
  private MedicationRequest.MedicationRequestIntent requestIntent =
      MedicationRequest.MedicationRequestIntent.ORDER;
  private MedicationRequest.MedicationRequestSubstitutionComponent substitution =
      new MedicationRequest.MedicationRequestSubstitutionComponent(new BooleanType(true));
  private MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequestQuantity;
  private Duration expectedSuplyDuration;

  private String dosageInstruction;
  private StatusCoPayment statusCoPayment = StatusCoPayment.STATUS_0;
  private boolean bvg = true;
  private boolean ser = true;
  private boolean emergencyServiceFee = false;
  private MultiplePrescriptionExtension mvo = MultiplePrescriptionExtension.asNonMultiple();
  private Date authoredOn = new Date();
  private TemporalPrecisionEnum temporalPrecision = TemporalPrecisionEnum.DAY;
  private String prescriberId;
  // becomes true if dosageInformation where given
  private Boolean dosageFlag = false;

  private String note;
  private boolean isTPrescription = false;

  public static KbvErpMedicationRequestBuilder forPatient(KbvPatient patient) {
    val mrb = new KbvErpMedicationRequestBuilder();
    mrb.subjectReference = patient.asReference();
    return mrb;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvErpMedicationRequestBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  public KbvErpMedicationRequestBuilder authoredOn(Date date) {
    this.authoredOn = date;
    return this;
  }

  /**
   * With this method the date can be set with an editable TemporalPrecisionEnum. The precision is
   * by default TemporalPrecisionEnum.DAY which corresponds to the correct behaviour profiled by the
   * KBV. <b>ATTENTION:</b> by messing around with the precision, the MedicationRequest might become
   * invalid corresponding to the KBV profiles
   *
   * @param date to be set as the authoredOn value
   * @param temporalPrecision the precision of the authoredOn date
   * @return self
   */
  public KbvErpMedicationRequestBuilder authoredOn(
      Date date, TemporalPrecisionEnum temporalPrecision) {
    this.temporalPrecision = temporalPrecision;
    return authoredOn(date);
  }

  public KbvErpMedicationRequestBuilder medication(KbvErpMedication medication) {
    this.medicationType = medication.getMedicationType().orElse(null);
    this.medicationReference = medication.asReference();
    this.isTPrescription = medication.getCategoryFirstRep().equals(MedicationCategory.C_02);
    return this;
  }

  public KbvErpMedicationRequestBuilder requester(KbvPractitioner practitioner) {
    this.requesterReference = practitioner.asReference();
    return this;
  }

  public KbvErpMedicationRequestBuilder insurance(KbvCoverage coverage) {
    this.insuranceReference = coverage.asReference();
    return this;
  }

  public KbvErpMedicationRequestBuilder status(String code) {
    var status = MedicationRequest.MedicationRequestStatus.fromCode(code);
    if (status == null) {
      log.warn(
          "Given code {} cannot be converted to a MedicationRequestStatus: using UNKNOWN as"
              + " default",
          code);
      status = MedicationRequest.MedicationRequestStatus.UNKNOWN;
    }
    return status(status);
  }

  public KbvErpMedicationRequestBuilder expectedSupplyDurationInWeeks(float value) {
    this.expectedSuplyDuration = new Duration();
    expectedSuplyDuration.setValue(value);
    expectedSuplyDuration.setUnit("Woche(n)");
    return this;
  }

  public KbvErpMedicationRequestBuilder status(MedicationRequest.MedicationRequestStatus status) {
    this.requestStatus = status;
    return this;
  }

  public KbvErpMedicationRequestBuilder intent(String code) {
    var intent = MedicationRequest.MedicationRequestIntent.fromCode(code);
    if (intent == null) {
      log.warn(
          "Given code {} cannot be converted to a MedicationRequestIntent: using NULL as"
              + " default",
          code);
      intent = MedicationRequest.MedicationRequestIntent.NULL;
    }
    return intent(intent);
  }

  public KbvErpMedicationRequestBuilder intent(MedicationRequest.MedicationRequestIntent intent) {
    this.requestIntent = intent;
    return this;
  }

  public KbvErpMedicationRequestBuilder dgmp(DosageDgMP dosageDgMP) {
    this.dosageFlag = true;
    this.dosageDgMPS.add(dosageDgMP);
    return this;
  }

  public KbvErpMedicationRequestBuilder dgmp(List<DosageDgMP> dosageDgMPs) {
    this.dosageFlag = true;
    this.dosageDgMPS.addAll(dosageDgMPs);
    return this;
  }

  public KbvErpMedicationRequestBuilder dosage(String text) {
    this.dosageInstruction = text;
    return this;
  }

  public KbvErpMedicationRequestBuilder prescriberId(String prescriberId) {
    this.prescriberId = prescriberId;
    return this;
  }

  public KbvErpMedicationRequestBuilder dispenseRequestQuantity(int amount) {
    val q = new Quantity();
    q.setUnit("Packung");
    q.setValue(amount);
    this.dispenseRequestQuantity =
        new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(q);
    return this;
  }

  public KbvErpMedicationRequestBuilder substitution(boolean allowed) {
    return substitution(
        new MedicationRequest.MedicationRequestSubstitutionComponent(new BooleanType(allowed)));
  }

  public KbvErpMedicationRequestBuilder substitution(
      MedicationRequest.MedicationRequestSubstitutionComponent substitution) {
    this.substitution = substitution;
    return this;
  }

  public KbvErpMedicationRequestBuilder isBVG(final boolean bvg) {
    this.bvg = bvg;
    return this;
  }

  public KbvErpMedicationRequestBuilder isSER(final boolean ser) {
    this.ser = ser;
    return this;
  }

  public KbvErpMedicationRequestBuilder mvo(MultiplePrescriptionExtension mvo) {
    this.mvo = mvo;
    return this;
  }

  public KbvErpMedicationRequestBuilder hasEmergencyServiceFee(final boolean emergencyServiceFee) {
    this.emergencyServiceFee = emergencyServiceFee;
    return this;
  }

  /**
   * from KbvItaErpVersion.V1_4_0 StatusCoPayment has to be set to [01] if SER has been set
   *
   * @param statusCoPayment
   * @return KbvErpMedicationRequestBuilder
   */
  public KbvErpMedicationRequestBuilder coPaymentStatus(StatusCoPayment statusCoPayment) {
    this.statusCoPayment = statusCoPayment;
    return this;
  }

  public KbvErpMedicationRequestBuilder accident(AccidentExtension accident) {
    this.accident = accident;
    return this;
  }

  public KbvErpMedicationRequestBuilder note(String note) {
    this.note = note;
    return this;
  }

  @SuppressWarnings({"java:S3776"})
  @Override
  public KbvErpMedicationRequest build() {
    KbvErpMedicationRequest medReq;
    checkRequired();
    medReq =
        this.createResource(
            KbvErpMedicationRequest::new, KbvItaErpStructDef.PRESCRIPTION, kbvItaErpVersion);
    // from Version 1.6 a VersionId is mandatory
    if (kbvItaErpVersion.isBiggerThan(KbvItaErpVersion.V1_3_0)) {
      medReq.getMeta().setVersionId("1");
    }

    if (isTPrescription) extensions.add(new TeratogenicExtension().asExtension());

    if (this.kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) == 0) {
      // check for 'default-able' and non-set values
      if (dispenseRequestQuantity == null) {
        this.dispenseRequestQuantity =
            new MedicationRequest.MedicationRequestDispenseRequestComponent();
        val quantity =
            new Quantity(1)
                .setSystem(UCUM.getCanonicalUrl())
                .setCode("{Package}"); // {Package} is a fixed Value
        this.dispenseRequestQuantity.setQuantity(quantity); // by default 1 package
      } else {
        if (dispenseRequestQuantity.getQuantity().getSystem() == null) {
          dispenseRequestQuantity.getQuantity().setSystem(UCUM.getCanonicalUrl());
        }
        if (dispenseRequestQuantity.getQuantity().getCode() == null) {
          dispenseRequestQuantity.getQuantity().setCode("{Package}"); // {Package} is a fixed Value
          dispenseRequestQuantity.getQuantity().setUnit(null);
        }
      }
      extensions.add(KbvItaErpStructDef.BVG.asBooleanExtension(bvg));

    } else {

      extensions.add(SER_EXTENSION.asBooleanExtension(ser));
      // (defined in https://simplifier.net/erezept/kbv_pr_erp_bundle)
      if (ser && kbvItaErpVersion.isBiggerThan(KbvItaErpVersion.V1_3_0))
        // -erp-angabeZuzahlungsbefreiungSER: 'Wenn die SER Kennzeichnung auf true gesetzt ist, dann
        // muss der Versicherte von der Zuzahlungspflicht befreit sein (Zuzahlungsstatus = 1)'
        // (defined in https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle)
        this.statusCoPayment = StatusCoPayment.STATUS_1;
    }

    extensions.add(
        KbvItaErpStructDef.EMERGENCY_SERVICES_FEE.asBooleanExtension(emergencyServiceFee));
    extensions.add(mvo.asExtension());
    extensions.add(statusCoPayment.asExtension());

    Optional.ofNullable(prescriberId)
        .map(id -> PRESCRIBER_ID.asExtension(new Identifier().setValue(id)))
        .ifPresent(extensions::add);

    Optional.ofNullable(accident).ifPresent(a -> extensions.add(a.asExtension()));
    Optional.ofNullable(note).ifPresent(n -> medReq.addNote().setText(n));
    Optional.ofNullable(expectedSuplyDuration)
        .ifPresent(eSD -> dispenseRequestQuantity.setExpectedSupplyDuration(eSD));
    medReq
        .setMedication(medicationReference)
        .setSubject(subjectReference)
        .setRequester(requesterReference)
        .setInsurance(List.of(insuranceReference))
        .setStatus(requestStatus)
        .setIntent(requestIntent)
        .setAuthoredOnElement(new DateTimeType(authoredOn, temporalPrecision))
        .setDispenseRequest(dispenseRequestQuantity)
        .setExtension(extensions);

    if (kbvItaErpVersion.isBiggerThan(KbvItaErpVersion.V1_3_0)) {
      if (dosageInstruction != null && !dosageInstruction.isEmpty()) {
        dosageFlag = true;
        dosageDgMPS.add(DosageDgMPBuilder.dosageBuilder().text(dosageInstruction).build());
      }
      if (!dosageDgMPS.isEmpty()) {
        // ext setzen
        medReq.addExtension(createGeneratorExtension());
        // ext setzen
        medReq.addExtension(
            new Extension(
                DgMPStructDef.MR_RENDERED_DOSAGE_INSTRUCTION.getCanonicalUrl(),
                new MarkdownType(render(dosageDgMPS))));
        // dgmp setzten
        dosageDgMPS.forEach(medReq::addDosageInstruction);
        // set DosageFlag
        medReq.addExtension(KbvItaErpStructDef.DOSAGE_FLAG.asBooleanExtension(dosageFlag));
      }

      medReq.setSubstitution(substitution);

    } else {
      if (this.medicationType == null) {
        medReq.setSubstitution(substitution);
        medReq.setDosageInstruction(List.of(createFlagedDosage()));
      } else {
        medicationTypeConfig(medReq);
      }
    }
    if (medicationType != null && medicationType.equals(MedicationType.INGREDIENT)) {
      // KBV constraint since KbvItaErp 1.4.1 -> if MedicationType is Ingredient ; Substitution
      // (Auditem) is not allowed // -erp-angabeSubstitutionVerbot:
      // .resource.substitution.exists().not()
      medReq.setSubstitution(null);
    }
    return medReq;
  }

  @SuppressWarnings("java:S6205")
  private void medicationTypeConfig(KbvErpMedicationRequest medReq) {

    Optional.ofNullable(this.medicationType)
        .ifPresentOrElse(
            mt -> {
              switch (mt) {
                case INGREDIENT -> {
                  setForIngredient(medReq);
                }
                case FREETEXT -> {
                  medReq
                      .setDosageInstruction(List.of(createFlagedDosage()))
                      .setSubstitution(substitution);
                }
                case COMPOUNDING -> {
                  val adaptedDosage = new Dosage();
                  adaptedDosage.setPatientInstruction(dosageInstruction);
                  medReq.setDosageInstruction(List.of(adaptedDosage)).setSubstitution(substitution);
                }
              }
            },
            () -> medReq.setDosageInstruction(List.of(createFlagedDosage())));
  }

  private void setForIngredient(KbvErpMedicationRequest medReq) {
    medReq.setDosageInstruction(List.of(createFlagedDosage())); // tobe adapted
  }

  private Dosage createFlagedDosage() {
    val hasDosageFlag = dosageInstruction != null && !dosageInstruction.isEmpty();
    val d = new Dosage().setText(dosageInstruction);
    d.addExtension(KbvItaErpStructDef.DOSAGE_FLAG.asBooleanExtension(hasDosageFlag));
    return d;
  }

  private void checkRequired() {
    if (isTPrescription)
      this.checkRequired(
          expectedSuplyDuration, "T-Prescription needs the expectedSuplyDuration in Woche(n)");
  }
}
