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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import java.util.Calendar;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.junit.jupiter.api.Test;

class AccidentExtensionTest {

  @Test
  void shouldBuildAccidentExtension() {
    val ae = AccidentExtension.accident();
    val outer = ae.asExtension();
    val kennzeichen =
        outer.getExtension().stream()
            .filter(e -> e.getUrl().equalsIgnoreCase("unfallkennzeichen"))
            .findAny();
    assertTrue(kennzeichen.isPresent());

    val unfalltag =
        outer.getExtension().stream()
            .filter(e -> e.getUrl().equalsIgnoreCase("unfalltag"))
            .findAny();
    assertTrue(unfalltag.isPresent());

    assertTrue(
        ae.toString()
            .contains(
                ae.accidentCauseType().getDisplay())); // well, simply covering the toString :/
  }

  @Test
  void shouldBuildAccidentExtensionWithFaker() {
    for (var i = 0; i < 5; i++) {
      val ae = AccidentExtension.faker();
      val outer = ae.asExtension();
      val kennzeichen =
          outer.getExtension().stream()
              .filter(e -> e.getUrl().equalsIgnoreCase("unfallkennzeichen"))
              .findAny();
      assertTrue(kennzeichen.isPresent());
    }
  }

  @Test
  void shouldEqualAccidents() {
    val date = new Date();
    val ae1 = AccidentExtension.accident(date);
    val ae2 = AccidentExtension.accident(date);
    assertEquals(ae1, ae2);
    assertEquals(ae1.hashCode(), ae2.hashCode());
  }

  @Test
  void shouldNotEqualOnDifferentAccidentDates() {
    val cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    val ae1 = AccidentExtension.accident(new Date());
    val ae2 = AccidentExtension.accident(cal.getTime());
    assertNotEquals(ae1, ae2);
    assertNotEquals(ae1.hashCode(), ae2.hashCode());
  }

  @Test
  void shouldEqualAccidentsAtWork() {
    val date = new Date();
    val ae1 = AccidentExtension.accidentAtWork(date).atWorkplace("Arbeitsplatz");
    val ae2 = AccidentExtension.accidentAtWork(date).atWorkplace("Arbeitsplatz");
    assertEquals(ae1, ae2);
    assertEquals(ae1.hashCode(), ae2.hashCode());
  }

  @Test
  void shouldNotEqualAccidentsAtWorkOnDifferentWorkplaces() {
    val date = new Date();
    val ae1 = AccidentExtension.accidentAtWork(date).atWorkplace("Arbeitsplatz");
    val ae2 = AccidentExtension.accidentAtWork(date).atWorkplace("Büro");
    assertNotEquals(ae1, ae2);
    assertNotEquals(ae1.hashCode(), ae2.hashCode());
  }

  @Test
  void shouldEqualAccidentsOccupationalDiseases() {
    val ae1 = AccidentExtension.occupationalDisease();
    val ae2 = AccidentExtension.occupationalDisease();
    assertEquals(ae1, ae2);
    assertEquals(ae1.hashCode(), ae2.hashCode());
  }

  @Test
  void shouldNotEqualOnDifferentCauses() {
    val ae1 = AccidentExtension.accident();
    val ae2 = AccidentExtension.accidentAtWork().atWorkplace();
    val ae3 = AccidentExtension.occupationalDisease();
    assertNotEquals(ae1, ae2);
    assertNotEquals(ae1, ae3);
    assertNotEquals(ae2, ae3);
  }

  @Test
  void shouldThrowOnInvalidExtension() {
    val extension = new Extension();
    extension.addExtension(new Extension("Testkennzeichen"));
    assertThrows(MissingFieldException.class, () -> AccidentExtension.fromExtension(extension));
  }

  @Test
  void shouldEqualOnSameObject() {
    val ae = AccidentExtension.accident();
    val ae2 = ae;
    assertEquals(ae, ae2);
  }

  @Test
  @SuppressWarnings("java:S5785")
  void shouldNotEqualOnOtherType() {
    val ae = AccidentExtension.accident();
    assertNotEquals(null, ae);
    assertNotEquals("Hello World", ae);
  }
}
