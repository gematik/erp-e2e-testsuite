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

package de.gematik.test.fuzzing;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import lombok.val;

public class FuzzingUtils {

  private FuzzingUtils() {
    throw new IllegalStateException("Utility class");
  }

  @SuppressWarnings("unchecked")
  public static <V extends Enum<?> & IStructureDefinition<? extends ProfileVersion<?>>>
      Class<V> randomStructureDefinitionClass() {
    val d =
        GemFaker.randomElement(
            AbdaErpBasisStructDef.class,
            AbdaErpPkvStructDef.class,
            DeBasisStructDef.class,
            ErpWorkflowStructDef.class,
            Hl7StructDef.class,
            KbvBasisStructDef.class,
            KbvItaErpStructDef.class,
            KbvItaForStructDef.class,
            PatientenrechnungStructDef.class);
    return (Class<V>) d;
  }

  @SuppressWarnings("java:S1452") // concrete VersionType not required here!
  public static IStructureDefinition<? extends ProfileVersion<?>> randomStructureDefinition() {
    val c = randomStructureDefinitionClass();
    return GemFaker.fakerValueSet(c);
  }
}
