/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.crypto.encryption.cms.CmsAuthEnvelopedData;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.operator.UIProvider;
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import de.gematik.test.erezept.pspwsclient.dataobjects.PharmacyPrescriptionInformation;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Algorithm;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/** Alternative Zuweisung über den Apothekendienstleister */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AlternativelyAssign implements Task {

  private final DequeStrategy deque;
  private final Actor pharmacy;
  private final DeliveryOption option;

  public static Builder thePrescriptionReceived(String order) {
    return thePrescriptionReceived(DequeStrategy.fromString(order));
  }

  public static Builder thePrescriptionReceived(DequeStrategy deque) {
    return new Builder(deque);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val userBehaviour = SafeAbility.getAbility(actor, DecideUserBehaviour.class);
    if (userBehaviour.doesPreferManualSteps()) {
      performManual(actor);
    } else {
      performAutomated(actor);
    }
  }

  private <T extends Actor> void performAutomated(T actor) {
    val pharmacyPsp = SafeAbility.getAbility(pharmacy, UsePspClient.class);

    val apoSmcb = SafeAbility.getAbility(pharmacy, UseSMCB.class);
    val dmcStack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcPrescription = deque.chooseFrom(dmcStack.getDmcs());

    val pspUrl = pharmacyPsp.getFullUrl(option).replace("ws", "http");

    val plainBody = createPspInformation(actor, dmcPrescription);

    // encrypt the plain body with the SMC-B of the receiving pharmacy
    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val encryptedBody =
        cmsAuthEnvelopedData.encrypt(
            List.of(apoSmcb.getSmcB().getEncCertificate(Algorithm.RSA_2048).getX509Certificate()),
            plainBody);

    val req = SerenityRest.given();
    pharmacyPsp.getXAuth().ifPresent(xAuth -> req.header("X-Authorization", xAuth));
    val response = req.contentType("application/pkcs7-mime").body(encryptedBody).post(pspUrl);
    assertTrue(
        response.statusCode() < 300,
        format(
            "PharmacyServiceProvider must accept call but returned StatusCode {0}",
            response.statusCode()));
  }

  private <T extends Actor> void performManual(T actor) {
    val egkAbility = SafeAbility.getAbility(actor, ProvideEGK.class);
    val dmcStack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmcPrescription = deque.chooseFrom(dmcStack.getDmcs());
    val dmcImage =
        DataMatrixCodeGenerator.getBufferedImage(
            dmcPrescription.getTaskId().getValue(), dmcPrescription.getAccessCode());
    UIProvider.getInstructionResult(
        dmcImage,
        format(
            "{0} ({1}) bitte den DMC an die Apotheke {2} mit Belieferungsoption {3} zuweisen",
            actor.getName(), egkAbility.getKvnr(), pharmacy.getName(), option));
  }

  @SneakyThrows
  private byte[] createPspInformation(Actor actor, DmcPrescription dmcPrescription) {
    val patientData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);

    val pspInfo = new PharmacyPrescriptionInformation();
    pspInfo.setVersion("1"); // TODO: check for correct version and/or create setter with enum
    pspInfo.setTaskID(dmcPrescription.getTaskId().getValue());
    pspInfo.setAccessCode(dmcPrescription.getAccessCode().getValue());
    pspInfo.setSupplyOptionsType(option.name());
    pspInfo.setName(patientData.getFullName());
    pspInfo.setMail(GemFaker.eMail(patientData.getFullName()));
    pspInfo.setPhone(GemFaker.fakerPhone());
    pspInfo.setHint(GemFaker.fakerCommunicationReplyMessage()); // random text
    pspInfo.setText(GemFaker.fakerCommunicationRepresentativeMessage()); // just random text
    val patientAddress = patientData.getPatient().getAddressFirstRep();
    val addressElements =
        new String[] {
          patientAddress.getCountry(),
          patientAddress.getPostalCode(),
          patientAddress.getCity(),
          GemFaker.fakerStreetName()
        };
    pspInfo.setAddress(addressElements);
    val jsonInfo = new ObjectMapper().writeValueAsString(pspInfo);

    Serenity.recordReportData()
        .withTitle(
            "Create PrescriptionInformation for the alternative assignment via"
                + " PharmacyServiceProvider")
        .andContents(jsonInfo);

    return jsonInfo.getBytes(StandardCharsets.UTF_8);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;
    private Actor pharmacy;

    public Builder to(Actor pharmacy) {
      this.pharmacy = pharmacy;
      return this;
    }

    public AlternativelyAssign with(String option) {
      return with(DeliveryOption.defineDeliveryOption(option));
    }

    public AlternativelyAssign with(DeliveryOption option) {
      return new AlternativelyAssign(deque, pharmacy, option);
    }
  }
}
