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

package de.gematik.test.erezept.integration.chargeitem;

import static de.gematik.test.core.expectations.verifier.ChargeItemBundleVerifier.chargeItemIdIsEqualTo;
import static de.gematik.test.core.expectations.verifier.ChargeItemBundleVerifier.prescriptionIdIsEqualTo;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.chargeitem.GetChargeItemById;
import de.gematik.test.erezept.actions.chargeitem.PostChargeItem;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@DisplayName("Charge Item Integration Tests")
@Tag("CHARGE_ITEM")
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
public class ChargeItemIT extends ErpTest {
  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor waldApo;

  public static Stream<Arguments> chargeItemBinaryVersions() {
    return ErxChargeItemManipulatorFactory.binaryVersionManipulator().stream()
        .map(namedEnvelope -> Arguments.arguments(namedEnvelope.getName(), namedEnvelope));
  }

  @Test
  @TestcaseId("ERP_CHARGE_ITEM_01")
  @DisplayName("Vergleich der PrescriptionID und ChargeItemID auf Gleichheit")
  void validateChargeItemId() {

    // patient preset behavior
    sina.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    sina.performs(GrantConsent.forOneSelf().withDefaultConsent());

    // Doc behavior
    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();

    // pharma behavior
    val acceptation =
        flughafenApo.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptation));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(task.getPrescriptionId()).fake();
    flughafenApo.performs(
        PostChargeItem.forPatient(sina)
            .davBundle(davAbgabedatenBundle)
            .withAcceptBundle(acceptation));

    // patients verification
    val chargeItem =
        sina.performs(
            GetChargeItemById.withPrescriptionId(task.getPrescriptionId())
                .withAccessCode(task.getAccessCode()));

    sina.attemptsTo(
        Verify.that(chargeItem)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(chargeItemIdIsEqualTo(task.getPrescriptionId()))
            .and(prescriptionIdIsEqualTo(task.getTaskId()))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_CHARGE_ITEM_02")
  @DisplayName(
      "Versuch einen fremden Task mit richtigem Secret und korrekter BundleReference mit einem"
          + " ChargeItem zu erweitern")
  void validateChargeItemIdAndFail() {

    // patient preset behavior
    sina.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    sina.performs(GrantConsent.forOneSelf().ensureConsentIsUnset(true).withDefaultConsent());

    // Doc behavior
    val task1 =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();
    val task2 =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();

    // pharma1 behavior
    val acceptation1 =
        flughafenApo.performs(AcceptPrescription.forTheTask(task1)).getExpectedResponse();
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptation1));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(task1.getPrescriptionId()).fake();

    // waldApo behavior
    val acceptation2 = waldApo.performs(AcceptPrescription.forTheTask(task2)).getExpectedResponse();
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptation2));

    val pharmaciesChargeItem =
        flughafenApo.performs(
            PostChargeItem.forPatient(sina)
                .davBundle(davAbgabedatenBundle)
                .withCustomValue(
                    task2.getPrescriptionId(),
                    acceptation1.getSecret(),
                    task1.getPrescriptionId().getValue()));

    flughafenApo.attemptsTo(
        Verify.that(pharmaciesChargeItem)
            .withOperationOutcome(ErpAfos.A_24471)
            .responseWith(returnCodeIs(403))
            .has(
                operationOutcomeHasDetailsText(
                    "No or invalid secret provided for referenced Task", ErpAfos.A_24471))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_CHARGE_ITEM_03")
  @DisplayName(
      "Versuch einen akzeptierten Task und fremder BundleReference mit einem ChargeItem zu"
          + " erweitern")
  void validateChargeItemIdAndFailWhileWrongBundleReference() {

    // patient preset behavior
    sina.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    sina.performs(GrantConsent.forOneSelf().ensureConsentIsUnset(true).withDefaultConsent());

    // Doc behavior
    val task1 =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();
    val task2 =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();

    // pharma behavior
    val acceptation =
        flughafenApo.performs(AcceptPrescription.forTheTask(task1)).getExpectedResponse();
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptation));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(task1.getPrescriptionId()).fake();

    val pharmaciesChargeItem =
        flughafenApo.performs(
            PostChargeItem.forPatient(sina)
                .davBundle(davAbgabedatenBundle)
                .withCustomValue(
                    acceptation.getTask().getPrescriptionId(),
                    acceptation.getSecret(),
                    task2.getPrescriptionId().getValue()));

    flughafenApo.attemptsTo(
        Verify.that(pharmaciesChargeItem)
            .withExpectedType()
            .responseWith(returnCode(201))
            .isCorrect());

    val chargeItem =
        sina.performs(
            GetChargeItemById.withPrescriptionId(task1.getPrescriptionId())
                .withAccessCode(task1.getAccessCode()));

    sina.attemptsTo(
        Verify.that(chargeItem)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(chargeItemIdIsEqualTo(task1.getPrescriptionId()))
            .and(prescriptionIdIsEqualTo(task1.getTaskId()))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Einstellen eines ChargeItemBundlesBundles als Apotheke manipulierten Binary"
              + " Profilversionen: {0}")
  @DisplayName(
      "Nachweis, dass ein ChargeItem in Version 1.3 mit Binary verschiedenen Profilversionen"
          + " eingestellt werden kann")
  @MethodSource("chargeItemBinaryVersions")
  void postChargeItemsWithDifferentBinaryVersions(
      String description, NamedEnvelope<FuzzingMutator<ErxChargeItem>> chargeItemMutators) {

    // patient preset behavior
    sina.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    sina.performs(GrantConsent.forOneSelf().withDefaultConsent());

    // Doc behavior
    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).withRandomKbvBundle())
            .getExpectedResponse();

    // pharma behavior
    val acceptation =
        flughafenApo.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    flughafenApo.performs(ClosePrescription.acceptedWith(acceptation));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(task.getPrescriptionId()).fake();

    val pharmaciesChargeItem =
        flughafenApo.performs(
            PostChargeItem.forPatient(sina)
                .davBundle(davAbgabedatenBundle)
                .withCustomStructureAndVersion(chargeItemMutators)
                .withAcceptBundle(acceptation));

    flughafenApo.attemptsTo(
        Verify.that(pharmaciesChargeItem)
            .withExpectedType()
            .responseWith(returnCode(201))
            .isCorrect());

    // patients verification
    val chargeItem =
        sina.performs(
            GetChargeItemById.withPrescriptionId(task.getPrescriptionId())
                .withAccessCode(task.getAccessCode()));

    sina.attemptsTo(
        Verify.that(chargeItem)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(chargeItemIdIsEqualTo(task.getPrescriptionId()))
            .and(prescriptionIdIsEqualTo(task.getTaskId()))
            .isCorrect());
  }
}
