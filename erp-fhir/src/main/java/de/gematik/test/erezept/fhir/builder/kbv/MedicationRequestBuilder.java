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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.BuilderUtil;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.references.kbv.CoverageReference;
import de.gematik.test.erezept.fhir.references.kbv.MedicationReference;
import de.gematik.test.erezept.fhir.references.kbv.RequesterReference;
import de.gematik.test.erezept.fhir.references.kbv.SubjectReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class MedicationRequestBuilder extends AbstractResourceBuilder<MedicationRequestBuilder> {

  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private Reference medicationReference;
  private Reference subjectReference;
  private VersicherungsArtDeBasis patientInsuranceKind;
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

  private Dosage dosage;

  private StatusCoPayment statusCoPayment = StatusCoPayment.STATUS_0;

  private final List<Extension> extensions = new LinkedList<>();
  private boolean bvg = true;
  private boolean emergencyServiceFee = false;
  private MultiplePrescriptionExtension mvo = MultiplePrescriptionExtension.asNonMultiple();
  private Date authoredOn;
  private TemporalPrecisionEnum temporalPrecision = TemporalPrecisionEnum.DAY;

  public static MedicationRequestBuilder forPatient(@NonNull KbvPatient patient) {
    val mrb = new MedicationRequestBuilder();
    mrb.subjectReference = new SubjectReference(patient.getId());
    mrb.patientInsuranceKind = patient.getInsuranceKind();
    return mrb;
  }

  public static MedicationRequestBuilder faker() {
    return faker(PatientBuilder.faker().build());
  }

  public static MedicationRequestBuilder faker(@NonNull KbvPatient patient) {
    return faker(patient, fakerBool());
  }

  public static MedicationRequestBuilder faker(@NonNull KbvPatient patient, Date authoredOn) {
    return faker(patient, authoredOn, fakerBool());
  }

  public static MedicationRequestBuilder faker(@NonNull KbvPatient patient, boolean substitution) {
    return faker(patient, new Date(), substitution);
  }

  @SuppressWarnings({"java:S1764"}) // not an issue fakerBool returns random values
  public static MedicationRequestBuilder faker(
      @NonNull KbvPatient patient, Date authoredOn, boolean substitution) {
    val builder =
        forPatient(patient)
            .dosage(fakerDosage())
            .quantityPackages(fakerAmount())
            //            .status(fakerValueSet(MedicationRequest.MedicationRequestStatus.class))
            //            .intent(fakerValueSet(MedicationRequest.MedicationRequestIntent.class))
            .isBVG(fakerBool())
            .hasEmergencyServiceFee(fakerBool())
            .insurance(CoverageBuilder.faker(patient.getInsuranceKind()).build())
            .requester(PractitionerBuilder.faker().build())
            .medication(KbvErpMedicationBuilder.faker().build())
            .substitution(substitution)
            .authoredOn(authoredOn)
            .coPaymentStatus(fakerValueSet(StatusCoPayment.class));

    // randomly add some accident type
    val randomlyAccident =
        fakerBool() && fakerBool(); // two booleans to have less probability for accident types
    if (randomlyAccident) {
      builder.accident(AccidentExtension.faker());
    }

    return builder;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public MedicationRequestBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  public MedicationRequestBuilder authoredOn(Date date) {
    this.authoredOn = date;
    return self();
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
  public MedicationRequestBuilder authoredOn(Date date, TemporalPrecisionEnum temporalPrecision) {
    this.temporalPrecision = temporalPrecision;
    return authoredOn(date);
  }

  public MedicationRequestBuilder medication(@NonNull KbvErpMedication medication) {
    this.medicationReference = new MedicationReference(medication.getId());
    return self();
  }

  public MedicationRequestBuilder requester(@NonNull Practitioner practitioner) {
    this.requesterReference = new RequesterReference(practitioner.getId());
    return self();
  }

  public MedicationRequestBuilder insurance(@NonNull Coverage coverage) {
    this.insuranceReference = new CoverageReference(coverage.getId());
    return self();
  }

  public MedicationRequestBuilder status(@NonNull String code) {
    var status = MedicationRequest.MedicationRequestStatus.fromCode(code);
    if (status == null) {
      log.warn(
          format(
              "Given code {0} cannot be converted to a MedicationRequestStatus: using UNKNOWN as default",
              code));
      status = MedicationRequest.MedicationRequestStatus.UNKNOWN;
    }
    return status(status);
  }

  public MedicationRequestBuilder status(
      @NonNull MedicationRequest.MedicationRequestStatus status) {
    this.requestStatus = status;
    return self();
  }

  public MedicationRequestBuilder intent(@NonNull String code) {
    var intent = MedicationRequest.MedicationRequestIntent.fromCode(code);
    if (intent == null) {
      log.warn(
          format(
              "Given code {0} cannot be converted to a MedicationRequestIntent: using NULL as default",
              code));
      intent = MedicationRequest.MedicationRequestIntent.NULL;
    }
    return intent(intent);
  }

  public MedicationRequestBuilder intent(
      @NonNull MedicationRequest.MedicationRequestIntent intent) {
    this.requestIntent = intent;
    return self();
  }

  public MedicationRequestBuilder dosage(@NonNull String text) {
    // TODO: check if possible to have dosageFlag = false + text  // NOSONAR
    // TODO: check if multiple dosage instructions are required   // NOSONAR
    val d = new Dosage().setText(text);
    d.addExtension(BuilderUtil.dosageFlag(true));
    return this.dosage(d);
  }

  public MedicationRequestBuilder dosage(@NonNull Dosage dosage) {
    this.dosage = dosage;
    return self();
  }

  public MedicationRequestBuilder quantityPackages(int amount) {
    return quantity(BuilderUtil.packageQuantity(amount));
  }

  public MedicationRequestBuilder quantity(@NonNull Quantity quantity) {
    return quantity(
        new MedicationRequest.MedicationRequestDispenseRequestComponent().setQuantity(quantity));
  }

  public MedicationRequestBuilder quantity(
      @NonNull MedicationRequest.MedicationRequestDispenseRequestComponent quantity) {
    this.dispenseRequestQuantity = quantity;
    return self();
  }

  public MedicationRequestBuilder substitution(boolean allowed) {
    return substitution(
        new MedicationRequest.MedicationRequestSubstitutionComponent(new BooleanType(allowed)));
  }

  public MedicationRequestBuilder substitution(
      @NonNull MedicationRequest.MedicationRequestSubstitutionComponent substitution) {
    this.substitution = substitution;
    return self();
  }

  public MedicationRequestBuilder isBVG(final boolean bvg) {
    this.bvg = bvg;
    return self();
  }

  public MedicationRequestBuilder mvo(MultiplePrescriptionExtension mvo) {
    this.mvo = mvo;
    return self();
  }

  public MedicationRequestBuilder hasEmergencyServiceFee(final boolean emergencyServiceFee) {
    this.emergencyServiceFee = emergencyServiceFee;
    return self();
  }

  public MedicationRequestBuilder coPaymentStatus(@NonNull StatusCoPayment status) {
    this.statusCoPayment = status;
    return self();
  }

  public MedicationRequestBuilder accident(AccidentExtension accident) {
    this.accident = accident;
    return self();
  }

  public KbvErpMedicationRequest build() {
    val medReq = new KbvErpMedicationRequest();

    val profile = KbvItaErpStructDef.PRESCRIPTION.asCanonicalType(kbvItaErpVersion);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    medReq.setId(this.getResourceId()).setMeta(meta);

    // check for 'default-able' and non set values
    if (dispenseRequestQuantity == null) this.quantityPackages(1); // by default 1 package

    extensions.add(BuilderUtil.isBVG(bvg));
    extensions.add(BuilderUtil.hasEmergencyServiceFee(emergencyServiceFee));
    extensions.add(mvo.asExtension(kbvItaErpVersion));

    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      extensions.add(statusCoPayment.asExtension());

      if (accident != null) {
        // accident allowed for any in kbv.ita.erp < 1.1.0
        extensions.add(accident.asExtension(kbvItaErpVersion));
      }
    } else {
      extensions.add(
          statusCoPayment.asExtension(
              KbvItaForStructDef.STATUS_CO_PAYMENT, KbvCodeSystem.STATUS_CO_PAYMENT_FOR));

      val isValidPayerType = patientInsuranceKind.equals(VersicherungsArtDeBasis.BG);
      if (accident != null && isValidPayerType) {
        // accident only allowed for BG and UK in kbv.ita.erp >= 1.1.0
        extensions.add(accident.asExtension(kbvItaErpVersion));
      }
    }

    if (authoredOn == null) {
      authoredOn = new Date();
    }

    medReq
        .setMedication(medicationReference)
        .setSubject(subjectReference)
        .setRequester(requesterReference)
        .setInsurance(List.of(insuranceReference))
        .setStatus(requestStatus)
        .setIntent(requestIntent)
        .setDosageInstruction(List.of(dosage))
        .setAuthoredOnElement(new DateTimeType(authoredOn, temporalPrecision))
        .setDispenseRequest(dispenseRequestQuantity)
        .setSubstitution(substitution)
        .setExtension(extensions);

    return medReq;
  }
}
