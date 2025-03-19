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
 */

package de.gematik.test.fuzzing.erx;

import static de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvBasisStructDef.*;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErxChargeItemManipulatorFactory {

  /**
   * Method to set new Value in ChargeItem Binary.meta.profile the concatenated profile will be a
   * combination from "binaryStrucDef + | + binaryStrucDef"
   * "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Binary + "
   *
   * @return manipulator, who can be used by an acceptation
   */
  public static List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> binaryVersionManipulator() {
    val retList = new ArrayList<NamedEnvelope<FuzzingMutator<ErxChargeItem>>>();
    retList.addAll(forContainedProfile(ErpWorkflowStructDef.BINARY_12, ErpWorkflowVersion.V1_2_0));
    retList.addAll(forContainedProfile(ErpWorkflowStructDef.BINARY_12, ErpWorkflowVersion.V1_3_0));
    retList.addAll(forContainedProfile(ErpWorkflowStructDef.BINARY_12, ErpWorkflowVersion.V1_4_0));
    return retList;
  }

  private static List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> forContainedProfile(
      ErpWorkflowStructDef structDef, ErpWorkflowVersion version) {
    val defaultVersion = structDef.asCanonicalType(version);
    val versionWithPatch = new CanonicalType(defaultVersion.asStringValue() + ".0");
    return List.of(
        NamedEnvelope.of(
            format("switch BinaryProfile to Version {0}", defaultVersion.asStringValue()),
            ci -> ci.getContained().forEach(c -> c.getMeta().setProfile(List.of(defaultVersion)))),
        NamedEnvelope.of(
            format("switch BinaryProfile to Version {0}", versionWithPatch.asStringValue()),
            ci ->
                ci.getContained().forEach(c -> c.getMeta().setProfile(List.of(versionWithPatch)))));
  }

  public static List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>>
      supportingReferenceManipulator() {
    return List.of(
        NamedEnvelope.of(
            "delete all supporting Reference",
            ci -> ci.getSupportingInformation().forEach(sI -> sI.setReference(""))),
        NamedEnvelope.of(
            "cut the first quarter of all supporting Reference",
            ci ->
                ci.getSupportingInformation()
                    .forEach(ErxChargeItemManipulatorFactory::cutFirstQuarterOfRef)),
        NamedEnvelope.of(
            "cut the last quarter of all supporting Reference",
            ci ->
                ci.getSupportingInformation()
                    .forEach(ErxChargeItemManipulatorFactory::cutLastQuarterOfRef)),
        NamedEnvelope.of(
            "delete Prescription supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> SUPPORTING_PRESCRIPTION_REF.matches(s.getDisplay()))
                    .findFirst()
                    .map(sI -> sI.setReference(""))),
        NamedEnvelope.of(
            "delete Receipt supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.matches(s.getDisplay()))
                    .findFirst()
                    .map(sI -> sI.setReference(""))),
        NamedEnvelope.of(
            "cut the first quarter of Prescription supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> SUPPORTING_PRESCRIPTION_REF.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutFirstQuarterOfRef)),
        NamedEnvelope.of(
            "cut the first quarter of Receipt supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutFirstQuarterOfRef)),
        NamedEnvelope.of(
            "cut the last quarter of Receipt supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutLastQuarterOfRef)),
        NamedEnvelope.of(
            "cut the last quarter of Prescription supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> SUPPORTING_PRESCRIPTION_REF.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutLastQuarterOfRef)),
        NamedEnvelope.of(
            "delete Binary supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> BINARY.matches(s.getDisplay()))
                    .findFirst()
                    .map(sI -> sI.setReference(""))),
        NamedEnvelope.of(
            "cut the first quarter of Binary supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> BINARY.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutFirstQuarterOfRef)),
        NamedEnvelope.of(
            "cut the last quarter of Binary supporting Reference",
            ci ->
                ci.getSupportingInformation().stream()
                    .filter(s -> BINARY.matches(s.getDisplay()))
                    .findFirst()
                    .map(ErxChargeItemManipulatorFactory::cutLastQuarterOfRef)));
  }

  private static Reference cutFirstQuarterOfRef(Reference sI) {
    if (sI.hasReference()) {
      return sI.setReference(sI.getReference().substring(sI.getReference().length() / 4));
    } else {
      log.info("there is no reference to cut in {} just set null", sI.getDisplay());
      return sI.setReference(null);
    }
  }

  private static Reference cutLastQuarterOfRef(Reference sI) {
    if (sI.hasReference()) {
      return sI.setReference(sI.getReference().substring(0, sI.getReference().length() * 3 / 4));
    } else {
      log.info("there is no reference to cut in {} just set null", sI.getDisplay());
      return sI.setReference(null);
    }
  }
}
