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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.eu.EuGetPrescriptionPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuGetPrescriptionInputBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganizationProfession;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RetrievalEuPrescriptions extends ErpAction<EuPrescriptionBundle> {

  private final KVNR kvnr;
  private final EuAccessCode accessCode;
  private final List<PrescriptionId> prescriptionIds;
  private final List<Consumer<EuGetPrescriptionInput>> manipulator;

  @Override
  public ErpInteraction<EuPrescriptionBundle> answeredBy(Actor actor) {
    val baseData = SafeAbility.getAbility(actor, ProvidePharmacyBaseData.class);

    val input =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.PRESCRIPTION_RETRIEVAL)
            .kvnr(kvnr)
            .accessCode(accessCode)
            .countryCode(baseData.getCountryCode())
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("Pharmacy")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .practitionerName(actor.getName())
            .prescriptionIds(prescriptionIds)
            .build();
    manipulator.forEach(consumer -> consumer.accept(input));
    val command = new EuGetPrescriptionPostCommand(input);

    return performCommandAs(command, actor);
  }

  public static RetrievalEuPrescriptionsBuilder forPatient(PatientActor patient) {
    return forKvnr(patient.getKvnr());
  }

  public static RetrievalEuPrescriptionsBuilder forKvnr(KVNR kvnr) {
    return new RetrievalEuPrescriptionsBuilder(kvnr);
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class RetrievalEuPrescriptionsBuilder {
    private final KVNR kvnr;
    private final List<Consumer<EuGetPrescriptionInput>> manipulator = new ArrayList<>();
    private List<PrescriptionId> prescriptionIds = new ArrayList<>();

    public RetrievalEuPrescriptionsBuilder with(Consumer<EuGetPrescriptionInput> manipulator) {
      this.manipulator.add(manipulator);
      return this;
    }

    public RetrievalEuPrescriptionsBuilder withPrescriptionIds(PrescriptionId... prescriptionIds) {
      return withPrescriptionIds(Arrays.asList(prescriptionIds));
    }

    public RetrievalEuPrescriptionsBuilder withPrescriptionIds(
        List<PrescriptionId> prescriptionIds) {
      this.prescriptionIds.addAll(prescriptionIds);
      return this;
    }

    public RetrievalEuPrescriptions withRandomAccessCode() {
      return withAccessCode(EuAccessCode.random());
    }

    public RetrievalEuPrescriptions withAccessCode(EuAccessCode accessCode) {
      return new RetrievalEuPrescriptions(kvnr, accessCode, prescriptionIds, manipulator);
    }
  }
}
