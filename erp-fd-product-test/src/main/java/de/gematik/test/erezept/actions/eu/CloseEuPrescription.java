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

package de.gematik.test.erezept.actions.eu;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.eu.EuCloseInputPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.*;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import groovy.util.logging.Slf4j;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Identifier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class CloseEuPrescription extends ErpAction<EmptyResource> {

  private final KbvErpBundle kbvErpBundle;
  private final EuAccessCode accessCode;
  private final KVNR kvnr;
  private final List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>> fhirCloseMutators;
  @Getter private EuCloseOperationInput euCloseOperationInput;
  @Getter private Instant timeStamp;

  public static CloseEuPrescriptionBuilder with(EuAccessCode accessCode, KVNR kvnr) {
    val builder = new CloseEuPrescriptionBuilder();
    builder.accessCode = accessCode;
    builder.kvnr = kvnr;
    return builder;
  }

  @Override
  @Step("{0} dispense and closes an EU Prescription")
  public ErpInteraction<EmptyResource> answeredBy(Actor actor) {
    val pharmacyBaseData = SafeAbility.getAbility(actor, ProvidePharmacyBaseData.class);

    val prescInput =
        getEuGetPrescriptionInput(
            accessCode, kvnr, pharmacyBaseData.getCountryCode(), pharmacyBaseData);
    euCloseOperationInput = getEuCloseOperationInput(prescInput, pharmacyBaseData);
    timeStamp = Instant.now();
    this.fhirCloseMutators.forEach(
        manipulator -> {
          Serenity.recordReportData().withTitle("Apply Mutator").andContents(manipulator.getName());
          manipulator.getParameter().accept(euCloseOperationInput);
        });

    val command = new EuCloseInputPostCommand(euCloseOperationInput);

    return performCommandAs(command, actor);
  }

  private EuCloseOperationInput getEuCloseOperationInput(
      EuGetPrescriptionInput prescInput, ProvidePharmacyBaseData pharmacist) {

    val euMedication = EuMedicationPZNFaker.faker().fake();
    val euPractitioner =
        EuPractitionerBuilder.buildPractitioner()
            .identifier(
                pharmacist.getPractitionerIdentifier() != null
                    ? pharmacist.getPractitionerIdentifier()
                    : new Identifier().setValue("dataAbsent"))
            .build();
    val euOrganisation =
        EuOrganizationFaker.faker()
            .withName(
                pharmacist.getOrganizationIdentifier() != null
                    ? pharmacist.getOrganizationIdentifier().getValue()
                    : "No Organization, sry!")
            .withIdentifier(
                pharmacist.getOrganizationIdentifier() != null
                    ? pharmacist.getOrganizationIdentifier()
                    : new Identifier().setValue("data Absent"))
            .withProfession(
                pharmacist.getOrganizationIdentifier() != null
                    ? pharmacist.getOrganizationIdentifier().getValue()
                    : "dataAbsent")
            .fake();

    val euPractitionerRole =
        EuPractitionerRoleBuilder.builder()
            .euPractitioner(euPractitioner)
            .euOrganization(euOrganisation)
            .defaultProfessionOID()
            .build();

    val euMedDsp =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId(kbvErpBundle.getPrescriptionId())
            .withMedication(euMedication)
            .withKvnr(kbvErpBundle.getPatient().getKvnr())
            .withPerformer(euPractitionerRole)
            .fake();

    return EuCloseOperationInputBuilder.builder(euMedDsp, euMedication)
        .requestDataFromGetPrescriptionInput(prescInput)
        .practitioner(euPractitioner)
        .organization(euOrganisation)
        .practitionerRole(euPractitionerRole)
        .build();
  }

  private EuGetPrescriptionInput getEuGetPrescriptionInput(
      EuAccessCode accessCode,
      KVNR kvnr,
      IsoCountryCode isoCountryCode,
      ProvidePharmacyBaseData pharmacy) {
    return EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.PRESCRIPTION_LIST)
        .kvnr(kvnr)
        .accessCode(accessCode)
        .countryCode(isoCountryCode)
        .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
        .pointOfCare(
            pharmacy.getOrganizationIdentifier() != null
                ? pharmacy.getOrganizationIdentifier().getValue()
                : "data Absent")
        .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
        .practitionerName(
            pharmacy.getPractitionerIdentifier() != null
                ? pharmacy.getPractitionerIdentifier().getValue()
                : "Data Absent")
        .build();
  }

  public static class CloseEuPrescriptionBuilder {
    EuAccessCode accessCode;
    KVNR kvnr;

    List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>> fhirCloseMutators =
        new LinkedList<>();

    public CloseEuPrescriptionBuilder withResourceManipulator(
        NamedEnvelope<FuzzingMutator<EuCloseOperationInput>> mutator) {
      this.fhirCloseMutators.add(mutator);
      return this;
    }

    public CloseEuPrescription withAccepted(KbvErpBundle kbvErpBundle) {
      return new CloseEuPrescription(kbvErpBundle, accessCode, kvnr, fhirCloseMutators);
    }
  }
}
