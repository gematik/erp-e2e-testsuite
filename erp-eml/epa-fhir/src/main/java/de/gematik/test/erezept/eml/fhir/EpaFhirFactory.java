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

package de.gematik.test.erezept.eml.fhir;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.ResourceTypeHint;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.profile.GematikDirStrucDef;
import de.gematik.test.erezept.eml.fhir.profile.GematikDirVersion;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedication;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedicationDispense;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedicationRequest;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelDispensation;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.eml.fhir.r4.EpaOrganisation;
import de.gematik.test.erezept.eml.fhir.r4.EpaPractitioner;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaFhirFactory {

  private static List<ResourceTypeHint<?, ?>> getTypeHints() {
    val list = new LinkedList<ResourceTypeHint<?, ?>>();
    // EpaFhir
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_MEDICATION)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaMedication.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_MEDICATION_DISPENSE)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaMedicationDispense.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_MEDICATION_REQUEST)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaMedicationRequest.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_OP_PROVIDE_PRESCRIPTION)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaOpProvidePrescription.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_OP_CANCEL_PRESCRIPTION)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaOpCancelPrescription.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_OP_PROVIDE_DISPENSATION)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaOpProvideDispensation.class));
    list.add(
        ResourceTypeHint.forStructure(EpaMedicationStructDef.EPA_OP_CANCEL_DISPENSATION)
            .forAllVersionsFrom(EpaMedicationVersion.class)
            .mappingTo(EpaOpCancelDispensation.class));

    // GematikFhirDirectory
    list.add(
        ResourceTypeHint.forStructure(GematikDirStrucDef.PRACTITIONER)
            .forAllVersionsFrom(GematikDirVersion.class)
            .mappingTo(EpaPractitioner.class));
    list.add(
        ResourceTypeHint.forStructure(GematikDirStrucDef.ORGANIZATION)
            .forAllVersionsFrom(GematikDirVersion.class)
            .mappingTo(EpaOrganisation.class));
    return list;
  }

  public static FhirCodec create() {
    return FhirCodec.forR4().withTypeHints(getTypeHints()).andBbriccsValidator();
  }

  public static FhirCodec create(ValidatorFhir fhirValidator) {
    return FhirCodec.forR4().withTypeHints(getTypeHints()).andCustomValidator(fhirValidator);
  }
}
