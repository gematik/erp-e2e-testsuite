/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.date.DateCalculator;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Calendar;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KbvErpMedicationRequestTest extends ParsingTest {
  private final String BASE_PATH = "fhir/valid/kbv/1.0.2/medicationrequest/";

  @Test
  void encodingSingleValidMedicationRequest() {
    val expectedID = "0587787f-3f1b-4578-a412-ce5bae8215b9";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertNotNull(medicationRequest);
    assertEquals(expectedID, medicationRequest.getLogicalId());
    assertTrue(medicationRequest.getCoPaymentStatus().isPresent());
    assertEquals(StatusCoPayment.STATUS_0, medicationRequest.getCoPaymentStatus().get());

    assertTrue(medicationRequest.getNoteText().isPresent());
    val expectedNote = "Patient erneut auf Anwendung der Schmelztabletten hinweisen";
    assertEquals(expectedNote, medicationRequest.getNoteTextOrEmpty());
    assertNotNull(medicationRequest.getDescription());
    assertFalse(medicationRequest.allowSubstitution());
    assertTrue(medicationRequest.getDescription().contains("ohne aut-idem"));
  }

  @ParameterizedTest(name = "[{index}] -> Check Accident in MedicationRequest from {0}")
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/5a3458b0-8364-4682-96e2-b262b2ab16eb.xml",
        "fhir/valid/kbv/1.1.0/bundle/5a3458b0-8364-4682-96e2-b262b2ab16eb.xml"
      })
  void shouldFindAccidents(String file) {
    val content = ResourceUtils.readFileFromResource(file);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val medicationRequest = kbvBundle.getMedicationRequest();

    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(AccidentCauseType.ACCIDENT, medicationRequest.getAccidentCause().orElseThrow());
    assertFalse(medicationRequest.getAccidentWorkplace().isPresent());
    assertTrue(medicationRequest.getAccidentDate().isPresent());
  }

  @ParameterizedTest(name = "[{index}] -> Check Accident at work in MedicationRequest from {0}")
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/5f66314e-459a-41e9-a3d7-65c935a8be2c.xml",
        "fhir/valid/kbv/1.1.0/bundle/5f66314e-459a-41e9-a3d7-65c935a8be2c.xml"
      })
  void shouldFindAccidentsAtWork(String file) {
    val content = ResourceUtils.readFileFromResource(file);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val medicationRequest = kbvBundle.getMedicationRequest();

    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(
        AccidentCauseType.ACCIDENT_AT_WORK, medicationRequest.getAccidentCause().orElseThrow());
    assertEquals("Arbeitsplatz", medicationRequest.getAccidentWorkplace().orElseThrow());
    assertTrue(medicationRequest.getAccidentDate().isPresent());
  }

  @ParameterizedTest(name = "[{index}] -> Check occupational disease in MedicationRequest from {0}")
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/218b581d-ccbe-480e-b8d7-f5f9b925e8c4.xml",
        "fhir/valid/kbv/1.1.0/bundle/218b581d-ccbe-480e-b8d7-f5f9b925e8c4.xml"
      })
  void shouldFindAccidentsOccupationalDisease(String file) {
    val content = ResourceUtils.readFileFromResource(file);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val medicationRequest = kbvBundle.getMedicationRequest();

    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(
        AccidentCauseType.OCCUPATIONAL_DISEASE, medicationRequest.getAccidentCause().orElseThrow());
    assertFalse(medicationRequest.getAccidentWorkplace().isPresent());
    assertFalse(medicationRequest.getAccidentDate().isPresent());
  }

  @Test
  void encodeMedicationRequestWithoutNote() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d4f";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertFalse(medicationRequest.getNoteText().isPresent());
    assertFalse(medicationRequest.isMultiple());
    assertEquals("", medicationRequest.getNoteTextOrEmpty());
    assertEquals("N/A", medicationRequest.getNoteTextOr("N/A"));
  }

  @Test
  void encodeMedicationRequestWithMvo() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isPresent());
    assertNotNull(medicationRequest.getDescription());
    assertTrue(medicationRequest.getDescription().contains("mit aut-idem"));
  }
  
  @Test
  void shouldDefaultSubstitutionToFalse() {
    val bundle = KbvErpBundleBuilder.faker().build();
    val medicationRequest = bundle.getMedicationRequest();
    
    medicationRequest.setSubstitution(null);
    assertFalse(bundle.isSubstitutionAllowed());
  }

  @Test
  void encodeMedicationRequCheckForNumAndDenom() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getNumerator().isPresent());
    assertTrue(medicationRequest.getDemoninator().isPresent());

    assertEquals(4, medicationRequest.getNumerator().orElseThrow());
    assertEquals(4, medicationRequest.getDemoninator().orElseThrow());
  }

  @Test
  void shouldManuallyChangeMvoDates() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isPresent());

    val originalStart = medicationRequest.getMvoStart().orElseThrow();
    val originalEnd = medicationRequest.getMvoEnd().orElseThrow();

    medicationRequest.getMvoPeriod().orElseThrow().setStart(new Date());
    medicationRequest.getMvoPeriod().orElseThrow().setEnd(new Date());
    val newStart = medicationRequest.getMvoStart().orElseThrow();
    val newEnd = medicationRequest.getMvoEnd().orElseThrow();
    assertNotEquals(originalStart, newStart);
    assertNotEquals(originalEnd, newEnd);

    // now check updateMvoDates() won't change a valid period
    medicationRequest.updateMvoDates();
    assertNotEquals(newStart, medicationRequest.getMvoStart().orElseThrow());
    assertNotEquals(newEnd, medicationRequest.getMvoEnd().orElseThrow());
  }

  @Test
  void shouldUpdateMvoDates() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isPresent());

    val oStart = medicationRequest.getMvoStart().orElseThrow();
    val oEnd = medicationRequest.getMvoEnd().orElseThrow();

    medicationRequest.updateMvoDates();
    val nStart = medicationRequest.getMvoStart().orElseThrow();
    val nEnd = medicationRequest.getMvoEnd().orElseThrow();

    // back-to-back checking original vs. new dates
    assertEquals(1, diff(Calendar.YEAR, oStart, oEnd));
    assertEquals(1, diff(Calendar.MONTH, oStart, oEnd));
    assertEquals(1, diff(Calendar.DAY_OF_MONTH, oStart, oEnd));

    val cEnd = Calendar.getInstance();
    cEnd.setTime(nEnd);

    // the newly calculated year my flip to two if add one day on the last day of the year
    if (cEnd.get(Calendar.DAY_OF_YEAR) == 1) {
      assertEquals(2, diff(Calendar.YEAR, nStart, nEnd));
    } else {
      assertEquals(1, diff(Calendar.YEAR, nStart, nEnd));
    }

    // the newly calculated month my flip to two if add one day on the last day of the month
    if (cEnd.get(Calendar.DAY_OF_MONTH) == 1) {
      // so we are expecting a diff of 2 months
      assertEquals(2, diff(Calendar.MONTH, nStart, nEnd));
      // and a negative diff of days! something like 1-30=29 or 1-31=-30 here
      assertTrue(diff(Calendar.DAY_OF_MONTH, nStart, nEnd) < 0);
    } else {
      assertEquals(1, diff(Calendar.MONTH, nStart, nEnd));
      assertEquals(1, diff(Calendar.DAY_OF_MONTH, nStart, nEnd));
    }
  }

  @Test
  void shouldUpdateWithoutEnd() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d5e";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isEmpty());

    medicationRequest.updateMvoDates();

    val dc = new DateCalculator();
    assertTrue(dc.isToday(medicationRequest.getMvoStart().orElseThrow()));
    assertTrue(medicationRequest.getMvoEnd().isEmpty());
  }

  @Test
  void shouldGetEmptyOptionalOnNonMvo() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d4f";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertFalse(medicationRequest.getNoteText().isPresent());
    assertFalse(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoPeriod().isEmpty());
    assertTrue(medicationRequest.getMvoStart().isEmpty());
    assertTrue(medicationRequest.getMvoEnd().isEmpty());
  }

  @Test
  void shouldGetKbvErpMedicationRequestFromResource() {
    val resource = new MedicationRequest();
    val kbvMedReq = KbvErpMedicationRequest.fromMedicationRequest(resource);
    assertNotNull(kbvMedReq);
    assertEquals(KbvErpMedicationRequest.class, kbvMedReq.getClass());
  }

  @Test
  void getEmptyAtNoMultiple() {
    val expectedID = "43c2b7ae-ad11-4387-910a-e6b7a3c38d4f";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertFalse(medicationRequest.getNoteText().isPresent());
    assertFalse(medicationRequest.isMultiple());

    assertEquals(java.util.Optional.empty(), medicationRequest.getNumerator());
    assertEquals(java.util.Optional.empty(), medicationRequest.getDemoninator());
    assertEquals(java.util.Optional.empty(), medicationRequest.getMvoEnd());
    assertEquals(java.util.Optional.empty(), medicationRequest.getMvoStart());
  }

  private int diff(int type, Date start, Date end) {
    val cStart = Calendar.getInstance();
    cStart.setTime(start);
    val cEnd = Calendar.getInstance();
    cEnd.setTime(end);
    return cEnd.get(type) - cStart.get(type);
  }
}
