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

package de.gematik.test.erezept.integration.pki;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.pki.OCSPRespVerifier.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.screenplay.abilities.UseHBA;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.toggle.PkiQesRsaEnableToggle;
import de.gematik.test.konnektor.soap.mock.LocalSigner;
import de.gematik.test.konnektor.soap.mock.utils.OcspTokenGenerator;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("QES Testfälle")
@Tag("PKI")
class ActivateTaskQesIT extends ErpTest {
  @Actor(name = "Gündüla Gunther")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @TestcaseId("ERP_QES_OCSP_01")
  @Test
  @DisplayName("Verordnender Arzt aktiviert E-Rezept ohne OCSP-Response für die QES")
  void activateQesWithoutOcspResp() {
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    Function<String, byte[]> signingFunc =
        data -> LocalSigner.signQES(hba, CryptoSystem.ECC_256).signDocument(false, data);
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    doctor.attemptsTo(
        Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());

    val acceptResp =
        pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
    pharmacy.attemptsTo(Verify.that(acceptResp).withExpectedType().has(isQesValid()).isCorrect());
  }

  @TestcaseId("ERP_QES_OCSP_02")
  @Test
  @DisplayName("Verordnender Arzt aktiviert E-Rezept mit gültiger OCSP-Response für die QES")
  void activateQesWithOcspResp() {
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    val ocspToken =
        OcspTokenGenerator.with(hba.getQesCertificate(CryptoSystem.ECC_256).getX509Certificate())
            .asOnlineToken();
    Function<String, byte[]> signingFunc =
        data ->
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(List.of(ocspToken), data.getBytes(StandardCharsets.UTF_8));
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    doctor.attemptsTo(
        Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());

    val acceptResp =
        pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
    pharmacy.attemptsTo(
        Verify.that(acceptResp)
            .withExpectedType()
            .has(isQesValid())
            .has(containsOcspResp(ocspToken))
            .isCorrect());
  }

  @TestcaseId("ERP_QES_OCSP_03")
  @Test
  @DisplayName("Verordnender Arzt aktiviert E-Rezept mit einer RSA-QES")
  void activateQesWithRSA() {
    val isRsaEnable = featureConf.getToggle(new PkiQesRsaEnableToggle());
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    Function<String, byte[]> signingFunc =
        data -> LocalSigner.signQES(hba, CryptoSystem.RSA_2048).signDocument(false, data);
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    if (isRsaEnable) {
      doctor.attemptsTo(
          Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());
      val acceptResp =
          pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
      pharmacy.attemptsTo(Verify.that(acceptResp).withExpectedType().has(isQesValid()).isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(activateResp)
              .withOperationOutcome()
              .responseWith(returnCode(400))
              .and(
                  operationOutcomeHasDetailsText(
                      "OCSP Check failed, certificate is revoked.", ErpAfos.A_20159))
              .isCorrect());
    }
  }

  @TestcaseId("ERP_QES_OCSP_04")
  @Test
  @DisplayName(
      "Verordnender Arzt aktiviert E-Rezept mit einer QES und einer zeitlich invaliden"
          + " OCSP-Response")
  void activateQesWithInvalidOcspResp() {
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    val ocspToken =
        OcspTokenGenerator.with(hba.getQesCertificate(CryptoSystem.ECC_256).getX509Certificate())
            .asSelfSignedToken(ZonedDateTime.now(), ZonedDateTime.now().minusDays(5));
    Function<String, byte[]> signingFunc =
        data ->
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(List.of(ocspToken), data.getBytes(StandardCharsets.UTF_8));
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    doctor.attemptsTo(
        Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());

    val acceptResp =
        pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
    pharmacy.attemptsTo(
        Verify.that(acceptResp)
            .withExpectedType()
            .has(isQesValid())
            .has(replacedOcspResp(ocspToken))
            .isCorrect());
  }

  @TestcaseId("ERP_QES_OCSP_05")
  @Test
  @DisplayName(
      "Verordnender Arzt aktiviert E-Rezept mit einer QES und einer OCSP-Response von einem anderen"
          + " HBA")
  void activateQesWithOcspRespWithAnotherHBA() {
    val hbaForOcspResp = SmartcardArchive.fromResources().getHbaByICCSN("80276001011699901501");
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    val ocspToken =
        OcspTokenGenerator.with(
                hbaForOcspResp.getQesCertificate(CryptoSystem.ECC_256).getX509Certificate())
            .asOnlineToken();
    Function<String, byte[]> signingFunc =
        data ->
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(List.of(ocspToken), data.getBytes(StandardCharsets.UTF_8));
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    doctor.attemptsTo(
        Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());

    val acceptResp =
        pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
    pharmacy.attemptsTo(
        Verify.that(acceptResp)
            .withExpectedType()
            .has(isQesValid())
            .has(replacedOcspResp(ocspToken))
            .isCorrect());
  }

  @TestcaseId("ERP_QES_OCSP_06")
  @Test
  @DisplayName(
      "Verordnender Arzt aktiviert E-Rezept mit einer QES und einer zurückgezogenen (revoked)"
          + " OCSP-Response")
  void activateQesWithRevokedOcspResp() {
    val hba = SafeAbility.getAbility(doctor, UseHBA.class).getHba();
    val ocspToken =
        OcspTokenGenerator.with(hba.getQesCertificate(CryptoSystem.ECC_256).getX509Certificate())
            .asSelfSignedRevokedToken(ZonedDateTime.now(), ZonedDateTime.now().minusDays(5));
    Function<String, byte[]> signingFunc =
        data ->
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(List.of(ocspToken), data.getBytes(StandardCharsets.UTF_8));
    val activateResp =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .withRandomKbvBundle()
                .setCustomSigningFunction(signingFunc));
    doctor.attemptsTo(
        Verify.that(activateResp).withExpectedType().responseWith(returnCode(200)).isCorrect());

    val acceptResp =
        pharmacy.performs(AcceptPrescription.forTheTask(activateResp.getExpectedResponse()));
    pharmacy.attemptsTo(
        Verify.that(acceptResp)
            .withExpectedType()
            .has(isQesValid())
            .has(replacedOcspResp(ocspToken))
            .isCorrect());
  }
}
