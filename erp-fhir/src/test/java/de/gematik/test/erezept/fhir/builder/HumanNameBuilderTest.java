/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.Hl7StructDef;
import lombok.val;
import org.junit.jupiter.api.Test;

class HumanNameBuilderTest {

  @Test
  void shouldBuildOldHumanName() {
    val hn = HumanNameBuilder.official().prefix("Dr. med.").given("Paul").family("Müller").build();
    assertEquals("Dr. med.", hn.getPrefixAsSingleString());
    assertEquals("Paul", hn.getGivenAsSingleString());
    assertEquals("Müller", hn.getFamily());
  }

  @Test
  void shouldBuildNewHumanName() {
    val hn =
        HumanNameBuilder.official()
            .prefix("Dr. med.")
            .given("Paul")
            .family("Freiherr von Müller")
            .buildExt();
    assertEquals("Dr. med.", hn.getPrefixAsSingleString());
    assertEquals("Paul", hn.getGivenAsSingleString());
    assertEquals("Freiherr von Müller", hn.getFamily());

    val prefixExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_PREFIX.getCanonicalUrl());
    assertNotNull(prefixExtension);
    assertEquals("von", prefixExtension.getValue().primitiveValue());

    val ownNameExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_NAME.getCanonicalUrl());
    assertNotNull(ownNameExtension);
    assertEquals("Müller", ownNameExtension.getValue().primitiveValue());

    val nameZusatzExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.NAMENSZUSATT.getCanonicalUrl());
    assertNotNull(nameZusatzExtension);
    assertEquals("Freiherr", nameZusatzExtension.getValue().primitiveValue());
  }

  @Test
  void shouldBuildNewHumanNameWithoutNobility() {
    val hn =
        HumanNameBuilder.official()
            .prefix("Dr. med.")
            .given("Paul")
            .family("von Müller")
            .buildExt();
    assertEquals("Dr. med.", hn.getPrefixAsSingleString());
    assertEquals("Paul", hn.getGivenAsSingleString());
    assertEquals("von Müller", hn.getFamily());

    val prefixExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_PREFIX.getCanonicalUrl());
    assertNotNull(prefixExtension);
    assertEquals("von", prefixExtension.getValue().primitiveValue());

    val ownNameExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_NAME.getCanonicalUrl());
    assertNotNull(ownNameExtension);
    assertEquals("Müller", ownNameExtension.getValue().primitiveValue());

    val nameZusatzExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.NAMENSZUSATT.getCanonicalUrl());
    assertNull(nameZusatzExtension);
  }

  @Test
  void shouldBuildNewComplexHumanName() {
    val hn =
        HumanNameBuilder.official()
            .prefix("Prof. habil. Dr. med")
            .given("Friedrich-Wilhelm-Karl-Gustav-Justus-Gotfried")
            .family("Grossherzog von und zu der Schaumberg-von-und-zu-Schaumburg-und-Radeberg")
            .buildExt();
    assertEquals("Prof. habil. Dr. med", hn.getPrefixAsSingleString());
    assertEquals("Friedrich-Wilhelm-Karl-Gustav-Justus-Gotfried", hn.getGivenAsSingleString());
    assertEquals(
        "Grossherzog von und zu der Schaumberg-von-und-zu-Schaumburg-und-Radeberg", hn.getFamily());

    val prefixExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_PREFIX.getCanonicalUrl());
    assertNotNull(prefixExtension);
    assertEquals("von und zu der", prefixExtension.getValue().primitiveValue());

    val ownNameExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.HUMAN_OWN_NAME.getCanonicalUrl());
    assertNotNull(ownNameExtension);
    assertEquals(
        "Schaumberg-von-und-zu-Schaumburg-und-Radeberg",
        ownNameExtension.getValue().primitiveValue());

    val nameZusatzExtension =
        hn.getFamilyElement().getExtensionByUrl(Hl7StructDef.NAMENSZUSATT.getCanonicalUrl());
    assertNotNull(nameZusatzExtension);
    assertEquals("Grossherzog", nameZusatzExtension.getValue().primitiveValue());
  }

  @Test
  void shouldThrowOnEmptyFamilyName() {
    val hnb = HumanNameBuilder.official().prefix("Dr. med.").given("Paul").family("");
    assertThrows(BuilderException.class, hnb::buildExt);
  }
}
