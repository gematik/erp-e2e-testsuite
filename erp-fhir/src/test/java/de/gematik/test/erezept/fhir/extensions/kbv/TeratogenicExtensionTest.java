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

package de.gematik.test.erezept.fhir.extensions.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.junit.jupiter.api.Test;

class TeratogenicExtensionTest {

  @Test
  void shouldBuildExtensionWithDefaultValues() {
    val teratogenicExtension = new TeratogenicExtension();
    val ext = teratogenicExtension.asExtension();

    assertEquals(KbvItaErpStructDef.TERATOGENIC.getCanonicalUrl(), ext.getUrl());
    assertEquals(5, ext.getExtension().size());

    assertFalse(teratogenicExtension.isChildbearingWoman());
    assertFalse(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_OFF_LABEL).getValue())
            .booleanValue());
    assertFalse(
        ((BooleanType)
                ext.getExtensionByUrl(TeratogenicExtension.URL_CHILDBEARING_WOMAN).getValue())
            .booleanValue());
    assertTrue(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_SAFETY_MEASURES).getValue())
            .booleanValue());
    assertTrue(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_INFO_MATERIALS).getValue())
            .booleanValue());
    assertTrue(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_EXPERTISE).getValue())
            .booleanValue());
  }

  @Test
  void shouldBuildExtensionWithCustomValues() {
    val te = new TeratogenicExtension();
    te.setOffLabel(true);
    te.setChildbearingWoman(true);
    te.setEinhaltungSicherheitsmassnahmen(false);
    te.setAushaendigungInformationsmaterialien(false);
    te.setErklaerungSachkenntnis(false);

    val ext = te.asExtension();

    assertTrue(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_OFF_LABEL).getValue())
            .booleanValue());
    assertTrue(
        ((BooleanType)
                ext.getExtensionByUrl(TeratogenicExtension.URL_CHILDBEARING_WOMAN).getValue())
            .booleanValue());
    assertFalse(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_SAFETY_MEASURES).getValue())
            .booleanValue());
    assertFalse(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_INFO_MATERIALS).getValue())
            .booleanValue());
    assertFalse(
        ((BooleanType) ext.getExtensionByUrl(TeratogenicExtension.URL_EXPERTISE).getValue())
            .booleanValue());
  }

  @Test
  void shouldCreateIndependentExtensionsForEachCall() {
    val te = new TeratogenicExtension();
    val ext1 = te.asExtension();

    te.setOffLabel(true);
    val ext2 = te.asExtension();

    assertFalse(
        ((BooleanType) ext1.getExtensionByUrl(TeratogenicExtension.URL_OFF_LABEL).getValue())
            .booleanValue());
    assertTrue(
        ((BooleanType) ext2.getExtensionByUrl(TeratogenicExtension.URL_OFF_LABEL).getValue())
            .booleanValue());
  }
}
