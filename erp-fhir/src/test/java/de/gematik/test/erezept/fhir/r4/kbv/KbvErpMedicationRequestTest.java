/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestFaker;
import de.gematik.test.erezept.fhir.date.DateCalculator;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;

class KbvErpMedicationRequestTest extends ErpFhirParsingTest {
  private static final String PATH = "fhir/valid/kbv/1.4.0/MedRequest/medReqFromBundle.xml";

  @Test
  void encodingSingleValidMedicationRequest() {
    val expectedID = "fe73acaf-c683-460d-9961-3ae6f87068f2";
    val content = ResourceLoader.readFileFromResource(PATH);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);
    val expectedNote = "Patient erneut auf Anwendung der Schmelztabletten hinweisen";

    assertNotNull(medicationRequest);
    assertEquals(expectedID, medicationRequest.getLogicalId());
    assertTrue(medicationRequest.getCoPaymentStatus().isPresent());
    assertEquals(StatusCoPayment.STATUS_0, medicationRequest.getCoPaymentStatus().get());
    medicationRequest.setNote(List.of(new Annotation().setText(expectedNote)));
    assertTrue(medicationRequest.getNoteText().isPresent());
    assertEquals(expectedNote, medicationRequest.getNoteTextOrEmpty());
    assertNotNull(medicationRequest.getDescription());
    assertTrue(medicationRequest.allowSubstitution());
    assertTrue(medicationRequest.getDescription().contains("aut-idem"));
  }

  @Test
  void shouldFindAccidents() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withAccident(AccidentExtension.accident())
            .fake();
    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(AccidentCauseType.ACCIDENT, medicationRequest.getAccidentCause().orElseThrow());
    assertFalse(medicationRequest.getAccidentWorkplace().isPresent());
    assertTrue(medicationRequest.getAccidentDate().isPresent());
  }

  @Test()
  void shouldFindAccidentsAtWork() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
            .fake();
    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(
        AccidentCauseType.ACCIDENT_AT_WORK, medicationRequest.getAccidentCause().orElseThrow());
    assertEquals("Arbeitsplatz", medicationRequest.getAccidentWorkplace().orElseThrow());
    assertTrue(medicationRequest.getAccidentDate().isPresent());
  }

  @Test()
  void shouldFindAccidentsOccupationalDisease() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withAccident(AccidentExtension.occupationalDisease())
            .fake();
    assertTrue(medicationRequest.hasAccidentExtension());
    assertTrue(medicationRequest.getAccident().isPresent());
    assertEquals(
        AccidentCauseType.OCCUPATIONAL_DISEASE, medicationRequest.getAccidentCause().orElseThrow());
    assertFalse(medicationRequest.getAccidentWorkplace().isPresent());
    assertFalse(medicationRequest.getAccidentDate().isPresent());
  }

  @Test
  void encodeMedicationRequestWithoutNote() {

    val content = ResourceLoader.readFileFromResource(PATH);
    val medicationRequest = parser.decode(KbvErpMedicationRequest.class, content);

    assertFalse(medicationRequest.getNoteText().isPresent());
    assertFalse(medicationRequest.isMultiple());
    assertEquals("", medicationRequest.getNoteTextOrEmpty());
    assertEquals("N/A", medicationRequest.getNoteTextOr("N/A"));
  }

  @Test
  void encodeMedicationRequestWithMvo() {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMvo(
                MultiplePrescriptionExtension.asMultiple(1, 4).withRandomId().validForDays(365))
            .fake();

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isPresent());

    assertTrue(medicationRequest.getNumerator().isPresent());
    assertTrue(medicationRequest.getDemoninator().isPresent());
    assertEquals(1, medicationRequest.getNumerator().orElseThrow());
    assertEquals(4, medicationRequest.getDemoninator().orElseThrow());

    assertNotNull(medicationRequest.getDescription());
  }

  @Test
  void shouldDefaultSubstitutionToFalse() {
    val bundle = KbvErpBundleFaker.builder().fake();
    val medicationRequest = bundle.getMedicationRequest();

    medicationRequest.setSubstitution(null);
    assertFalse(bundle.getMedicationRequest().isSubstitutionAllowed());
  }

  @Test
  void shouldManuallyChangeMvoDates() {
    val converter = DateConverter.getInstance();
    val today = LocalDate.now();

    // deterministic start/end dates which are guaranteed to differ from the manually set ones
    val originalStartDate = converter.localDateToDate(today.minusDays(400));
    val originalEndDate = converter.localDateToDate(today.minusDays(35));
    val fakedMedReq =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMvo(
                MultiplePrescriptionExtension.asMultiple(1, 4)
                    .withRandomId()
                    .validThrough(originalStartDate, originalEndDate))
            .fake();

    assertTrue(fakedMedReq.isMultiple());
    assertTrue(fakedMedReq.getMvoStart().isPresent());
    assertTrue(fakedMedReq.getMvoEnd().isPresent());

    val originalStart = fakedMedReq.getMvoStart().orElseThrow();
    val originalEnd = fakedMedReq.getMvoEnd().orElseThrow();

    // manually set an expired period
    val expiredStart = converter.localDateToDate(today.minusDays(20));
    val expiredEnd = converter.localDateToDate(today.minusDays(10));
    fakedMedReq.getMvoPeriod().orElseThrow().setStart(expiredStart);
    fakedMedReq.getMvoPeriod().orElseThrow().setEnd(expiredEnd);
    val newStart = fakedMedReq.getMvoStart().orElseThrow();
    val newEnd = fakedMedReq.getMvoEnd().orElseThrow();
    assertNotEquals(originalStart, newStart);
    assertNotEquals(originalEnd, newEnd);
    assertEquals(expiredStart, newStart);
    assertEquals(expiredEnd, newEnd);

    // updateMvoDates() must move the expired period to the present
    fakedMedReq.updateMvoDates();
    val updatedStart = fakedMedReq.getMvoStart().orElseThrow();
    val updatedEnd = fakedMedReq.getMvoEnd().orElseThrow();
    assertNotEquals(newStart, updatedStart);
    assertNotEquals(newEnd, updatedEnd);
    assertEquals(today, converter.dateToLocalDate(updatedStart));
    assertEquals(today.plusDays(10), converter.dateToLocalDate(updatedEnd));

    // updateMvoDates() must not change a still valid period
    fakedMedReq.updateMvoDates();
    assertEquals(updatedStart, fakedMedReq.getMvoStart().orElseThrow());
    assertEquals(updatedEnd, fakedMedReq.getMvoEnd().orElseThrow());
  }

  @Test
  void shouldUpdateMvoDates() {
    val content = ResourceLoader.readFileFromResource("fhir/valid/kbv/1.4.0/Beispiel_10_2.xml");
    val medicationRequest = parser.decode(KbvErpBundle.class, content).getMedicationRequest();

    assertTrue(medicationRequest.isMultiple());
    assertTrue(medicationRequest.getMvoStart().isPresent());
    assertTrue(medicationRequest.getMvoEnd().isPresent());

    val oStart = medicationRequest.getMvoStart().orElseThrow();
    val oEnd = medicationRequest.getMvoEnd().orElseThrow();

    medicationRequest.updateMvoDates();
    val nStart = medicationRequest.getMvoStart().orElseThrow();
    val nEnd = medicationRequest.getMvoEnd().orElseThrow();

    // back-to-back checking original vs. new dates
    assertEquals(0, diff(Calendar.YEAR, oStart, oEnd));
    assertEquals(3, diff(Calendar.MONTH, oStart, oEnd));
    assertEquals(15, diff(Calendar.DAY_OF_MONTH, oStart, oEnd));

    // ensure the original difference from start to end is kept to the updated dates
    val converter = DateConverter.getInstance();
    val oDiff = Period.between(converter.dateToLocalDate(oStart), converter.dateToLocalDate(oEnd));
    val nDiff = Period.between(converter.dateToLocalDate(nStart), converter.dateToLocalDate(nEnd));
    assertEquals(oDiff, nDiff);
  }

  @Test
  void shouldUpdateWithoutEnd() {
    val fakedMedReq =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMvo(
                MultiplePrescriptionExtension.asMultiple(1, 4).withRandomId().validForDays(365))
            .fake();

    fakedMedReq.getMvoPeriod().get().setEnd(null);
    assertTrue(fakedMedReq.isMultiple());
    assertTrue(fakedMedReq.getMvoStart().isPresent());
    assertTrue(fakedMedReq.getMvoEnd().isEmpty());

    fakedMedReq.updateMvoDates();

    val dc = new DateCalculator();
    assertTrue(dc.isToday(fakedMedReq.getMvoStart().orElseThrow()));
    assertTrue(fakedMedReq.getMvoEnd().isEmpty());
  }

  @Test
  void shouldGetEmptyOptionalOnNonMvo() {
    val content = ResourceLoader.readFileFromResource(PATH);
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
    val content = ResourceLoader.readFileFromResource(PATH);
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
