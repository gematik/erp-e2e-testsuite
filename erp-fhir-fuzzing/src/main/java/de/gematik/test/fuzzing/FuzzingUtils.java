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

package de.gematik.test.fuzzing;

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvBasisStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import lombok.val;

public class FuzzingUtils {

  private FuzzingUtils() {
    throw new IllegalStateException("Utility class");
  }

  @SuppressWarnings("unchecked")
  public static <V extends Enum<?> & WithStructureDefinition<? extends ProfileVersion>>
      Class<V> randomStructureDefinitionClass() {
    val d =
        GemFaker.randomElement(
            AbdaErpBasisStructDef.class,
            AbdaErpPkvStructDef.class,
            DeBasisProfilStructDef.class,
            ErpWorkflowStructDef.class,
            HL7StructDef.class,
            KbvBasisStructDef.class,
            KbvItaErpStructDef.class,
            KbvItaForStructDef.class,
            PatientenrechnungStructDef.class);
    return (Class<V>) d;
  }

  @SuppressWarnings("java:S1452") // concrete VersionType not required here!
  public static WithStructureDefinition<? extends ProfileVersion> randomStructureDefinition() {
    val c = randomStructureDefinitionClass();
    return GemFaker.fakerValueSet(c);
  }
}
