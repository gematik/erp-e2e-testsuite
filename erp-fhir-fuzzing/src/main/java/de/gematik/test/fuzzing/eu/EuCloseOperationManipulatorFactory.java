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

package de.gematik.test.fuzzing.eu;

import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.*;

import de.gematik.test.erezept.fhir.r4.eu.EuCloseOperationInput;
import de.gematik.test.erezept.fhir.r4.eu.EuMedicationDispense;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EuCloseOperationManipulatorFactory {

  /**
   * Provides date manipulators for EuCloseOperation resources.
   *
   * @return list of NamedEnvelopes containing manipulators
   */
  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getAllEuCloseOperationManipulators() {
    val manipulators = new LinkedList<>(getParametersManipulator());
    manipulators.addAll(getDispensationManipulator());
    manipulators.addAll(getOrganizationDataManipulator());
    manipulators.addAll(getPractitionerDataManipulator());
    manipulators.addAll(getRequestDataManipulator());
    manipulators.addAll(getPractitionerRoleDataManipulator());

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getParametersManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Remove medication in RxDispensation, ",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(RX_DISPENSATION.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(MEDICATION.getCode())))
                    .forEach(para -> para.setResource(null))));

    manipulators.add(
        NamedEnvelope.of(
            "Remove Name of PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .forEach(para -> para.setName(null))));

    manipulators.add(
        NamedEnvelope.of(
            "Remove Name of RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .forEach(para -> para.setName(null))));

    manipulators.add(
        NamedEnvelope.of(
            "Remove Name of Organisation",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .forEach(para -> para.setName(null))));

    manipulators.add(
        NamedEnvelope.of(
            "Remove in PractitionerRoleData the ParameterName",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
                    .forEach(para -> para.setName(null))));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getPractitionerDataManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Delete name.text in PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .map(
                        para ->
                            ((EuPractitioner) para.getResource())
                                .getName().stream().findFirst().orElseThrow().setTextElement(null))
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete name.family in PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .map(
                        para ->
                            ((EuPractitioner) para.getResource())
                                .getName().stream().findFirst().orElseThrow().setFamily(null))
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete name in PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .map(para -> ((EuPractitioner) para.getResource()).setName(null))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "Delete identifier in PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .map(para -> ((EuPractitioner) para.getResource()).setIdentifier(null))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "change meta.system in PractitionerData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
                    .map(
                        para ->
                            para.getResource().getMeta().getProfile().stream().findFirst().stream()
                                .map(
                                    profile ->
                                        profile.setValue("http://Unknown.System.practitioner"))
                                .toList())
                    .findFirst()
                    .orElseThrow()));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getPractitionerRoleDataManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Change meta.system in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
                    .map(
                        para ->
                            para.getResource().getMeta().getProfile().stream().findFirst().stream()
                                .map(
                                    profile ->
                                        profile.setValue(
                                            "http://Unknown.System.PractitionerRoleData"))
                                .toList())
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "delete meta.system in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
                    .map(para -> para.getResource().setMeta(null))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "Delete PractitionerReference in PractitionerRoleData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
                    .map(
                        para ->
                            ((PractitionerRole) para.getResource())
                                .setPractitioner(new Reference()))
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete OrganizationReference in PractitionerRoleData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
                    .map(
                        para ->
                            ((PractitionerRole) para.getResource())
                                .setOrganization(new Reference()))
                    .findFirst()
                    .orElseThrow()));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getOrganizationDataManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Change meta.system in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            para.getResource().getMeta().getProfile().stream().findFirst().stream()
                                .map(
                                    profile ->
                                        profile.setValue("http://Unknown.System.organizytion"))
                                .toList())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete Name in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(para -> ((EuOrganization) para.getResource()).setName(null))
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address.line in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(address -> address.setLine(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address.city Value in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(address -> address.setCity(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address.state Values in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(address -> address.setState(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address Values in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(
                                        address ->
                                            address
                                                .setLine(null)
                                                .setCity(null)
                                                .setPostalCode(null)
                                                .setCountry(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address.postal Value in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(address -> address.setPostalCode(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "Delete mandatory Address.county value in OrganizationData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
                    .map(
                        para ->
                            ((EuOrganization) para.getResource())
                                .getAddress().stream()
                                    .findFirst()
                                    .map(address -> address.setCountry(null))
                                    .orElseThrow())
                    .findFirst()
                    .orElseThrow()));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getRequestDataManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Delete KVNR in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(EU_KVNR.getCode())))
                    .forEach(part -> part.setValue(null))));
    manipulators.add(
        NamedEnvelope.of(
            "Delete AccessCode in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(ACCESS_CODE.getCode())))
                    .forEach(part -> part.setValue(null))));
    manipulators.add(
        NamedEnvelope.of(
            "Delete CountryCode in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(COUNTRY_CODE.getCode())))
                    .forEach(part -> part.setValue(null))));

    manipulators.add(
        NamedEnvelope.of(
            "Delete PractitionerName in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(PRACTITIONER_NAME.getCode())))
                    .forEach(part -> part.setValue(null))));
    manipulators.add(
        NamedEnvelope.of(
            "Delete PractitionerRole in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(PRACTITIONER_ROLE.getCode())))
                    .forEach(part -> part.setValue(null))));
    manipulators.add(
        NamedEnvelope.of(
            "Delete PointOfCare in RequestData",
            p ->
                p.getParameter().stream()
                    .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
                    .flatMap(
                        para ->
                            para.getPart().stream()
                                .filter(part -> part.getName().equals(POINT_OF_CARE.getCode())))
                    .forEach(part -> part.setValue(null))));
    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>
      getDispensationManipulator() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Change Metas Profile in MedicationDispense",
            eCOI ->
                dispensationOf(eCOI).getMeta().getProfile().stream()
                    .findFirst()
                    .orElseThrow()
                    .setValueAsString("http://Unknown.System.sed")));

    manipulators.add(
        NamedEnvelope.of(
            "Delete WhenHnadedOver in MedicationDispense",
            eCOI -> dispensationOf(eCOI).setWhenHandedOver(null)));

    manipulators.add(
        NamedEnvelope.of(
            "delete Medication ref. in MedicationDispense",
            eCOI -> dispensationOf(eCOI).setMedication(new Reference())));

    manipulators.add(
        NamedEnvelope.of(
            "Delete Perforner in MedicationDispense",
            eCOI -> dispensationOf(eCOI).setPerformer(null)));
    manipulators.add(
        NamedEnvelope.of(
            "Change Status in MedicationDispense toi cancelled",
            eCOI ->
                dispensationOf(eCOI)
                    .setStatus(MedicationDispense.MedicationDispenseStatus.CANCELLED)));
    manipulators.add(
        NamedEnvelope.of(
            "Change Status in MedicationDispense toi inProgress",
            eCOI ->
                dispensationOf(eCOI)
                    .setStatus(MedicationDispense.MedicationDispenseStatus.INPROGRESS)));

    return manipulators;
  }

  private static EuMedicationDispense dispensationOf(EuCloseOperationInput eCOI) {
    return eCOI.getFirstRxDispension().stream()
        .map(p -> (EuMedicationDispense) p.getPartFirstRep().getResource())
        .findFirst()
        .orElseThrow();
  }
}
