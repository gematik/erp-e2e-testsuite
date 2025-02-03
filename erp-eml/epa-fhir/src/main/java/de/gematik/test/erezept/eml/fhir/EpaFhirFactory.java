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

package de.gematik.test.erezept.eml.fhir;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.ResourceTypeHint;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.test.erezept.eml.fhir.parser.profiles.EpaMedStructDef;
import de.gematik.test.erezept.eml.fhir.parser.profiles.EpaVersion;
import de.gematik.test.erezept.eml.fhir.parser.profiles.GematikDirStrucDef;
import de.gematik.test.erezept.eml.fhir.parser.profiles.GematikDirVersion;
import de.gematik.test.erezept.eml.fhir.r4.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings("java:S6204") // the suppressed check is 'Replace this usage of
// 'Stream.collect(Collectors.toList())' with 'Stream.toList()', but we need
// mutable lists
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaFhirFactory {

  private static List<ResourceTypeHint<?, ?>> getTypeHints() {
    val list = new LinkedList<ResourceTypeHint<?, ?>>();
    // EpaFhir
    list.addAll(registerEpaStruc(EpaMedStructDef.EPA_MEDICATION, EpaMedication.class));
    list.addAll(
        registerEpaStruc(EpaMedStructDef.EPA_MEDICATION_DISPENSE, EpaMedicationDispense.class));
    list.addAll(
        registerEpaStruc(EpaMedStructDef.EPA_MEDICATION_REQUEST, EpaMedicationRequest.class));
    list.addAll(
        registerEpaStruc(
            EpaMedStructDef.EPA_OP_PROVIDE_PRESCRIPTION, EpaOpProvidePrescription.class));
    list.addAll(
        registerEpaStruc(
            EpaMedStructDef.EPA_OP_CANCEL_PRESCRIPTION, EpaOpCancelPrescription.class));
    list.addAll(
        registerEpaStruc(
            EpaMedStructDef.EPA_OP_PROVIDE_DISPENSATION, EpaOpProvideDispensation.class));
    list.addAll(
        registerEpaStruc(
            EpaMedStructDef.EPA_OP_CANCEL_DISPENSATION, EpaOpCancelDispensation.class));
    // GematikFhir
    list.addAll(registerGematikDirStruc(GematikDirStrucDef.PRACTITIONER, EpaPractitioner.class));
    list.addAll(registerGematikDirStruc(GematikDirStrucDef.ORGANIZATION, EpaOrganisation.class));
    return list;
  }

  private static <R extends Resource> List<ResourceTypeHint<EpaVersion, R>> registerEpaStruc(
      EpaMedStructDef def, Class<R> rClass) {
    val list =
        Arrays.stream(EpaVersion.values())
            .map(v -> ResourceTypeHint.forStructure(def, v, rClass))
            .collect(Collectors.toList());
    list.add(ResourceTypeHint.forStructure(def, rClass));
    return list;
  }

  private static <R extends Resource>
      List<ResourceTypeHint<GematikDirVersion, R>> registerGematikDirStruc(
          GematikDirStrucDef def, Class<R> rClass) {
    val list =
        Arrays.stream(GematikDirVersion.values())
            .map(v -> ResourceTypeHint.forStructure(def, v, rClass))
            .collect(Collectors.toList());
    list.add(ResourceTypeHint.forStructure(def, rClass));
    return list;
  }

  public static FhirCodec create() {
    return FhirCodec.forR4().withTypeHints(getTypeHints()).andBbriccsValidator();
  }

  public static FhirCodec create(ValidatorFhir fhirValidator) {
    return FhirCodec.forR4().withTypeHints(getTypeHints()).andCustomValidator(fhirValidator);
  }
}
