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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.*;
import static java.text.MessageFormat.format;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.fhir.date.DateConverter;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("MedicationDispense FilterTests")
@Tag("MedicationDispense")
public class GetMedicationDispenseWithFilterIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @BeforeAll
  static void setTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_01")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense nach equals"
          + " HandedOver filtern kann")
  public void shouldDownloadMedicDispenseWithFilterHandedOver() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(
        ClosePrescription.alternative()
            .acceptedWith(
                acceptation,
                DateConverter.getInstance().localDateToDate(LocalDate.now().minusDays(2)),
                DateConverter.getInstance().localDateToDate(LocalDate.now().minusDays(1))));

    val medDisp =
        patient.performs(
            GetMedicationDispense.whenHandedOver(SearchPrefix.EQ, LocalDate.now().minusDays(1)));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyWhenHandedOverIsEqual(LocalDate.now().minusDays(1)))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_02")
  @Test
  @DisplayName(
      "Prüfe, dass ein Patient beim Abruf seiner MedicationDispense als SearchSet den Filter"
          + " whenhandedover nutzen kann. In diesem Fall kombiniert mit before (LowerThan - lt) ")
  public void shouldDownloadMedicDispenseWithFilterBeforeAndHandedOver() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    val medDisp =
        patient.performs(GetMedicationDispense.whenHandedOver(SearchPrefix.LT, LocalDate.now()));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyWhenHandedOverIsBefore(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_03")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense nach"
          + " HandedOver Filtern kann")
  public void shouldDownloadMedicDispenseWithFilterGraterThanAndHandedOver() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    val medDisp =
        patient.performs(GetMedicationDispense.whenHandedOver(SearchPrefix.GT, LocalDate.now()));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyWhenHandedOverIsAfter(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_04")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense als"
          + " DefaultFilter equalsHandedOver benutzt wird")
  public void shouldDownloadMedicDispenseWithDefaultFilterHandedOver() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    val medDisp = patient.performs(GetMedicationDispense.whenHandedOverDefault());
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyWhenHandedOverIsSortedSerialAscend())
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_05")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense nach equals"
          + " WhenPrepared filtern kann")
  public void shouldDownloadMedicDispenseWithFilterPrepared() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(ClosePrescription.alternative().acceptedWith(acceptation, new Date()));
    val medDisp =
        patient.performs(GetMedicationDispense.whenPrepared(SearchPrefix.EQ, LocalDate.now()));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(
                verifyWhenPreparedWithPredicate(
                    ld -> ld.isEqual(LocalDate.now()),
                    format(
                        "Der Wert im Feld MedicationDispense.whenPrepared muss {0} sein ",
                        LocalDate.now())))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_06")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense nach before"
          + " WhenPrepared filtern kann")
  public void shouldDownloadMedicDispenseWithFilterBeforePrepared() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(
        ClosePrescription.alternative()
            .acceptedWith(
                acceptation,
                DateConverter.getInstance().localDateToDate(LocalDate.now().minusDays(1))));
    val medDisp =
        patient.performs(
            GetMedicationDispense.whenPrepared(SearchPrefix.EQ, LocalDate.now().minusDays(1)));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyWhenPreparedIsBefore(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_07")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispense nach equals"
          + " PerformerId filtern kann")
  public void shouldDownloadMedicDispenseWithFilterPerformer() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    val dispPresc = ClosePrescription.acceptedWith(acceptation);
    pharmacy.performs(dispPresc);

    val medDisp = patient.performs(GetMedicationDispense.fromPerformer(pharmacy.getTelematikId()));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(verifyAllPerformerIdsAre(pharmacy.getTelematikId()))
            .isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_08")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispenseBundles als"
          + " Wert für totalCount Null bekommt")
  public void shouldDownloadMedicDispenseWithCountZero() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    val dispPresc = ClosePrescription.acceptedWith(acceptation);
    pharmacy.performs(dispPresc);

    val medDisp = patient.performs(GetMedicationDispense.fromPerformer(pharmacy.getTelematikId()));
    patient.attemptsTo(
        Verify.that(medDisp).withExpectedType().and(containsTotalCountOf(0)).isCorrect());
  }

  @TestcaseId("ERP_MEDICATION_DISPENSE_09")
  @Test
  @DisplayName(
      "Es muss geprüft werden, dass ein Patient beim Abruf seiner MedicationDispenseBundles KEINE"
          + " RelationLinks angeboten werden")
  public void shouldDownloadMedicDispenseWithoutRelationLinks() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    val dispPresc = ClosePrescription.acceptedWith(acceptation);
    pharmacy.performs(dispPresc);

    val medDisp = patient.performs(GetMedicationDispense.withCount(5));
    patient.attemptsTo(
        Verify.that(medDisp)
            .withExpectedType()
            .and(containsCountOfGivenLinks(List.of("next", "prev", "self", "first", "last"), 0L))
            .isCorrect());
  }
}
