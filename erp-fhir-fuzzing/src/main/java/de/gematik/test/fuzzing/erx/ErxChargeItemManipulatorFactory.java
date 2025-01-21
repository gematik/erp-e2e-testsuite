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

package de.gematik.test.fuzzing.erx;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
    return List.of(
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.2",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_2_0, true))))),
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.2.0",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_2_0, false))))),
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.3",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_3_0, true))))),
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.3.0",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_3_0, false))))),
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.4",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_4_0, true))))),
        NamedEnvelope.of(
            "switch BinaryProfile to Version |1.4.0",
            ci ->
                ci.getContained()
                    .forEach(
                        c ->
                            c.getMeta()
                                .setProfile(
                                    List.of(
                                        ErpWorkflowStructDef.BINARY_12.asCanonicalType(
                                            ErpWorkflowVersion.V1_4_0, false))))));
  }
}
