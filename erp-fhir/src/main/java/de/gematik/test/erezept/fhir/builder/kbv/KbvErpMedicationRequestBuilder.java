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

import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef.PRESCRIBER_ID;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.ucum.builder.QuantityBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.QuantityPackungExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
public class KbvErpMedicationRequestBuilder
    extends ResourceBuilder<KbvErpMedicationRequest, KbvErpMedicationRequestBuilder> {

  private final List<Extension> extensions = new LinkedList<>();
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
  private String dosageInstruction;
  private StatusCoPayment statusCoPayment = StatusCoPayment.STATUS_0;
  private boolean bvg = true;
  private boolean ser = true;
  private boolean emergencyServiceFee = false;
  private MultiplePrescriptionExtension mvo = MultiplePrescriptionExtension.asNonMultiple();
  private Date authoredOn = new Date();
  private TemporalPrecisionEnum temporalPrecision = TemporalPrecisionEnum.DAY;
  private String prescriberId;

  private String note;

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

  public KbvErpMedicationRequestBuilder dosage(String text) {
    this.dosageInstruction = text;
    return this;
  }

  public KbvErpMedicationRequestBuilder prescriberId(String prescriberId) {
    this.prescriberId = prescriberId;
    return this;
  }

  public KbvErpMedicationRequestBuilder quantityPackages(int amount) {
    return quantity(QuantityBuilder.asUcumPackage().withValue(amount));
  }

  public KbvErpMedicationRequestBuilder quantity(Quantity quantity) {
    return quantity(
        new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(quantity));
  }

  public KbvErpMedicationRequestBuilder quantity(
      MedicationRequest.MedicationRequestDispenseRequestComponent quantity) {
    this.dispenseRequestQuantity = quantity;
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

  public KbvErpMedicationRequestBuilder coPaymentStatus(StatusCoPayment status) {
    this.statusCoPayment = status;
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

  @Override
  public KbvErpMedicationRequest build() {
    KbvErpMedicationRequest medReq;
    medReq =
        this.createResource(
            KbvErpMedicationRequest::new, KbvItaErpStructDef.PRESCRIPTION, kbvItaErpVersion);

    if (this.kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) == 0) {
      // check for 'default-able' and non-set values
      if (dispenseRequestQuantity == null) this.quantityPackages(1); // by default 1 package
      extensions.add(KbvItaErpStructDef.BVG.asBooleanExtension(bvg));

    } else {
      this.quantity(
          QuantityPackungExtension.asPackung()
              .withValue(dispenseRequestQuantity.getQuantity().getValue().intValue()));
      extensions.add(KbvItaErpStructDef.SER.asBooleanExtension(ser));
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

    if (this.medicationType == null) {
      medReq.setSubstitution(substitution);
      medReq.setDosageInstruction(List.of(createFlagedDosage()));
    } else {
      medicationTypeConfig(medReq);
    }

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
    val dosageFlag = dosageInstruction != null && !dosageInstruction.isEmpty();
    val d = new Dosage().setText(dosageInstruction);
    d.addExtension(KbvItaErpStructDef.DOSAGE_FLAG.asBooleanExtension(dosageFlag));
    return d;
  }
}
