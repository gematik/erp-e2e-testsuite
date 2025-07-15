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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class BGInsuranceCoverageInfoTest {

  @Test
  void getNamingBGWorks() {
    assertFalse(BGInsuranceCoverageInfo.VERKEHR_BERLIN.getName().isEmpty());
  }

  @Test
  void getNamingBGAsObjectWorks() {
    String insuranceCoverageBG = BGInsuranceCoverageInfo.BG_BAU.getName();
    assertEquals(BGInsuranceCoverageInfo.BG_BAU.getName(), insuranceCoverageBG);
  }

  @Test
  void getContactWORKS() {
    assertFalse(BGInsuranceCoverageInfo.BG_BAU_MITTE.getContact().isEmpty());
  }

  @Test
  void getContactAsObjectWORKS() {
    String insuranceCoverageBGContact = BGInsuranceCoverageInfo.BG_CHEMIE_HAL.getContact();
    assertEquals(BGInsuranceCoverageInfo.BG_CHEMIE_HAL.getContact(), insuranceCoverageBGContact);
  }

  @Test
  void getIknr() {
    assertFalse(BGInsuranceCoverageInfo.GUVV_OLDENBURG.getIknr().isEmpty());
  }

  @Test
  void getIknrFitsEnum() {
    val testdata = BGInsuranceCoverageInfo.BG_HANDEL_ESSEN.getIknr();
    assertEquals("120591129", testdata);
  }

  @Test
  void maxNameLengthGKV45() {
    val testdata = BGInsuranceCoverageInfo.values();
    for (var i : testdata) {
      assertTrue(i.getName().length() <= 45);
    }
  }

  @Test
  void maxNameLengthBG45WithInfo() {
    val testdata = BGInsuranceCoverageInfo.values();
    for (var i : testdata) {
      assertTrue(i.getName().length() <= 45, format("Insurance {0} has more than 45 digits", i));
    }
  }

  @Test
  void maxIKNRLengthBGIs9() {
    val testdata = BGInsuranceCoverageInfo.values();
    for (var i : testdata) {
      assertEquals(
          9,
          i.getIknr().length(),
          format("Insurance {0} has more than 9 digits Nr: {1}", i, i.getIknr()));
    }
  }
}
