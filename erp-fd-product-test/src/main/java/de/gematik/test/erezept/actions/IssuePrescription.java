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

package de.gematik.test.erezept.actions;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasValidPrescriptionId;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInDraftStatus;

import com.ibm.icu.impl.Pair;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.ByteArrayMutator;
import de.gematik.test.fuzzing.core.StringMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;

@RequiredArgsConstructor
public class IssuePrescription extends ErpAction<ErxTask> {

  private final PatientActor patient;
  private final Pair<KbvPatient, KbvCoverage> patientCoverage;
  @Nullable private final DoctorActor responsibleDoctor;
  private final PrescriptionAssignmentKind assignmentKind;
  private final KbvErpBundleBuilder bundleBuilder;
  private final List<Consumer<KbvErpBundle>> manipulator;
  private final List<StringMutator> stringMutators;
  private final List<ByteArrayMutator> signedBundleMutators;
  @Nullable private final FhirResourceFuzz<Bundle> smartFuzzer;

  @Nullable private ByteArrayOutputStream signatureObserver;
  @Nullable private Function<String, byte[]> signingFunc;

  public static Builder forPatient(PatientActor patient) {
    return new Builder(patient);
  }

  public IssuePrescription setSignatureObserver(ByteArrayOutputStream signatureObserver) {
    this.signatureObserver = signatureObserver;
    return this;
  }

  public IssuePrescription setCustomSigningFunction(Function<String, byte[]> signingFunc) {
    this.signingFunc = signingFunc;
    return this;
  }

  @Override
  public ErpInteraction<ErxTask> answeredBy(Actor actor) {
    val creation = actor.asksFor(TaskCreate.forPatient(patient).ofAssignmentKind(assignmentKind));
    actor.attemptsTo(
        Verify.that(creation)
            .withExpectedType(ErpAfos.A_19018)
            .hasResponseWith(returnCode(201))
            .and(isInDraftStatus())
            .and(hasValidPrescriptionId())
            .isCorrect());

    val docBaseData = SafeAbility.getAbility(actor, ProvideDoctorBaseData.class);
    val kbvPractitioner = docBaseData.getPractitioner();
    val draftTask = creation.getExpectedResponse();
    val prescriptionId = draftTask.getPrescriptionId();

    bundleBuilder
        .prescriptionId(prescriptionId)
        .practitioner(kbvPractitioner)
        .medicalOrganization(docBaseData.getMedicalOrganization())
        .patient(patientCoverage.first)
        .insurance(patientCoverage.second);

    if (this.responsibleDoctor != null) {
      bundleBuilder.attester(responsibleDoctor.getPractitioner());
    }
    val kbvBundle = bundleBuilder.build();

    if (smartFuzzer != null) {
      smartFuzzer.fuzzTilInvalid(
          kbvBundle, SafeAbility.getAbility(actor, UseTheErpClient.class).getFhir());
    }

    // now add optional extensions
    manipulator.forEach(extConsumer -> extConsumer.accept(kbvBundle));

    val activationResponse =
        actor.asksFor(
            ActivatePrescription.forGiven(draftTask)
                .withStringMutator(stringMutators)
                .withByteArrayMutator(signedBundleMutators)
                .withKbvBundle(kbvBundle)
                .setSignatureObserver(signatureObserver)
                .setCustomSigningFunction(signingFunc));

    // store prescription in patient ability for automatic teardown
    Optional.ofNullable(patient.abilityTo(ManageDataMatrixCodes.class))
        .ifPresent(
            ability ->
                activationResponse
                    .getResponse()
                    .getResourceOptional()
                    .ifPresent(
                        task ->
                            ability.appendDmc(
                                DmcPrescription.ownerDmc(task.getTaskId(), task.getAccessCode()))));

    // store prescription in doctor ability for automatic teardown
    // this is required because the patient cannot delete all (e.g. 169/209) prescriptions
    Optional.ofNullable(actor.abilityTo(ManageDoctorsPrescriptions.class))
        .ifPresent(
            ability ->
                activationResponse.getResponse().getResourceOptional().ifPresent(ability::append));

    return activationResponse;
  }

  public static class Builder {
    private final PatientActor patient;
    private final Pair<KbvPatient, KbvCoverage> patientCoverage;
    private final List<Consumer<KbvErpBundle>> fhirActivateMutators = new LinkedList<>();
    private final List<StringMutator> stringMutators = new LinkedList<>();
    private final List<ByteArrayMutator> signedBundleMutators = new LinkedList<>();
    private PrescriptionAssignmentKind assignmentKind = PrescriptionAssignmentKind.PHARMACY_ONLY;
    @Nullable private FhirResourceFuzz<Bundle> smartFuzzer;
    private DoctorActor responsibleDoctor;

    private Builder(PatientActor patient) {
      this.patient = patient;
      this.patientCoverage = this.patient.getPatientCoverage();
    }

    public Builder ofAssignmentKind(PrescriptionAssignmentKind assignmentKind) {
      this.assignmentKind = assignmentKind;
      return this;
    }

    public Builder withCompositionExtension(Extension extension) {
      this.fhirActivateMutators.add(bundle -> bundle.getComposition().addExtension(extension));
      return this;
    }

    public Builder withMedicationExtension(Extension extension) {
      this.fhirActivateMutators.add(bundle -> bundle.getMedication().addExtension(extension));
      return this;
    }

    public Builder withMedicationRequestExtension(Extension extension) {
      this.fhirActivateMutators.add(
          bundle -> bundle.getMedicationRequest().addExtension(extension));
      return this;
    }

    public Builder withCoverageExtension(Extension extension) {
      this.fhirActivateMutators.add(bundle -> bundle.getCoverage().addExtension(extension));
      return this;
    }

    public Builder withPatientExtension(Extension extension) {
      this.fhirActivateMutators.add(bundle -> bundle.getPatient().addExtension(extension));
      return this;
    }

    public Builder withResourceManipulator(Consumer<KbvErpBundle> mutator) {
      this.fhirActivateMutators.add(mutator);
      return this;
    }

    public Builder withStringFuzzing(StringMutator mutator) {
      this.stringMutators.add(mutator);
      return this;
    }

    public Builder withByteArrayFuzzing(ByteArrayMutator mutator) {
      this.signedBundleMutators.add(mutator);
      return this;
    }

    public Builder withSmartFuzzer(FhirResourceFuzz<Bundle> smartFuzzer) {
      this.smartFuzzer = smartFuzzer;
      return this;
    }

    public Builder withResponsibleDoctor(DoctorActor responsibleDoctor) {
      this.responsibleDoctor = responsibleDoctor;
      return this;
    }

    public IssuePrescription withRandomKbvBundle() {
      val isUnfallKasse =
          patient.getCoverageInsuranceType().equals(InsuranceTypeDe.BG)
              || patient.getPayorType().equals(Optional.of(PayorType.UK));

      if (isUnfallKasse) {
        return withKbvBundleFrom(
            KbvErpBundleFaker.builder()
                .withKvnr(patient.getKvnr())
                .withPractitioner(responsibleDoctor.getPractitioner())
                .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                .withInsurance(patientCoverage.second, patientCoverage.first)
                .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
                .toBuilder());
      }
      return withKbvBundleFrom(KbvErpBundleFaker.builder().withKvnr(patient.getKvnr()).toBuilder());
    }

    public IssuePrescription withKbvBundleFrom(KbvErpBundleBuilder builder) {
      Object[] params = {
        patient,
        patientCoverage,
        responsibleDoctor,
        assignmentKind,
        builder,
        fhirActivateMutators,
        stringMutators,
        signedBundleMutators,
        smartFuzzer
      };
      return new Instrumented.InstrumentedBuilder<>(IssuePrescription.class, params).newInstance();
    }
  }
}
