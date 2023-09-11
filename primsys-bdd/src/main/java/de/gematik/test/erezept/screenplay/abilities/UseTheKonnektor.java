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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.cardterminal.CardInfo;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import de.gematik.test.erezept.lei.exceptions.MissingSmartcardException;
import de.gematik.test.erezept.lei.exceptions.VerifyPinFailed;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorResponse;
import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.cfg.KonnektorConfiguration;
import de.gematik.test.konnektor.commands.*;
import de.gematik.test.smartcard.*;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.vsds.vsdservice.v5.ReadVSDResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Ability;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static java.text.MessageFormat.format;

@Slf4j
public class UseTheKonnektor implements Ability {

  @Nullable private final SmcB smcb;
  @Nullable private final Hba hba;
  private final Konnektor konnektor;
  @Nullable private CardInfo smcbHandle;
  @Nullable private CardInfo hbaHandle;

  private UseTheKonnektor(Konnektor konnektor, @Nullable SmcB smcb, @Nullable Hba hba) {
    this.konnektor = konnektor;
    this.smcb = smcb;
    this.hba = hba;
    this.insertCards(smcb, hba);
    this.initCardHandles();
  }

  public void insertCards(Smartcard... cards) {
    if (konnektor.getCardTerminalManager() == null) {
      return;
    }

    Arrays.stream(cards)
        .filter(Objects::nonNull)
        .forEach(card -> konnektor.getCardTerminalManager().insertCard(card));
  }

  public static KonnektorAbilityBuilder with(SmcB smcb) {
    return new KonnektorAbilityBuilder(smcb);
  }

  public static KonnektorAbilityBuilder with(Hba hba) {
    return new KonnektorAbilityBuilder(hba);
  }

  private void initCardHandles() {
    if (smcb != null) {
      this.smcbHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb)).getPayload();
    }
    if (hba != null) {
      this.hbaHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(hba)).getPayload();
    }
  }

  public KonnektorResponse<byte[]> decrypt(byte[] data) {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "Decrypt Document");
    return this.konnektor.execute(new DecryptDocumentCommand(smcbHandle, data));
  }

  public KonnektorResponse<byte[]> encrypt(String data) {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "Encrypt Document");
    return this.konnektor.execute(
        new EncryptDocumentCommand(smcbHandle, data.getBytes(StandardCharsets.UTF_8)));
  }

  public KonnektorResponse<byte[]> signDocumentWithHba(String document) {
    checkCardHandle(SmartcardType.HBA, hbaHandle, "Sign Document");
    return signDocument(hba, hbaHandle, document);
  }

  public KonnektorResponse<byte[]> signDocumentWithSmcb(String document) {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "Sign Document");
    return signDocument(smcb, smcbHandle, document);
  }

  private KonnektorResponse<byte[]> signDocument(
      Smartcard smartcard, CardInfo handle, String document) {
    val title = format("Sign Document with {0} (Cardhandle: {1})", smartcard, handle);
    Serenity.recordReportData().withTitle(title).andContents(document);
    val signCmd = new SignXMLDocumentCommand(handle, document);
    val ret = konnektor.execute(signCmd);

    val signOperation =
        format(
            "Document to sign with {0} {1} on {2}",
            smartcard.getType(), smartcard.getIccsn(), konnektor.getName());
    val signOperationResponse =
        format(
            "Signed Document with {0} {1} on {2}",
            smartcard.getType(), smartcard.getIccsn(), konnektor.getName());
    val dumper = FhirDumper.getInstance();
    dumper.writeDump(signOperation, "document_to_sign.xml", document);
    dumper.writeDump(
        signOperationResponse,
        "signed_document.b64",
        Base64.getEncoder().encodeToString(ret.getPayload()));
    return ret;
  }

  public KonnektorResponse<Boolean> verifyDocument(byte[] document) {
    Serenity.recordReportData()
        .withTitle(format("Verify Document with length of {0} Bytes", document.length))
        .andContents(Base64.getEncoder().encodeToString(document));
    val verifyCmd = new VerifyDocumentCommand(document);
    return konnektor.execute(verifyCmd);
  }

  public KonnektorResponse<byte[]> externalAuthenticate(byte[] challenge) {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "External Authenticate");

    Serenity.recordReportData()
        .withTitle(
            format(
                "VerifyPin for Card {0} with card handle {2} and ICCSN {1}",
                smcbHandle.getType(), smcbHandle.getIccsn(), smcbHandle.getHandle()));
    val pinResponseType =
        konnektor.execute(new VerifyPinCommand(smcbHandle, PinType.PIN_SMC)).getPayload();
    if (pinResponseType.getPinResult() != PinResultEnum.OK) {
      throw new VerifyPinFailed(smcbHandle.getIccsn());
    }
    val externalAuthenticateCmd = new ExternalAuthenticateCommand(smcbHandle);
    externalAuthenticateCmd.setToBeSignedData(challenge);
    return konnektor.execute(externalAuthenticateCmd);
  }

  public KonnektorResponse<ReadVSDResponse> requestEvidenceForEgk(Egk egk) {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "Request EGK Evidence");
    this.insertCards(egk);
    val pinResponseType =
        konnektor.execute(new VerifyPinCommand(smcbHandle, PinType.PIN_SMC)).getPayload();
    if (pinResponseType.getPinResult() != PinResultEnum.OK) {
      throw new VerifyPinFailed(smcbHandle.getIccsn());
    }
    val egkCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(egk)).getPayload();
    val readVsdCommand = new ReadVsdCommand(egkCardHandle, smcbHandle, true, true);
    return konnektor.execute(readVsdCommand);
  }

  public KonnektorResponse<X509Certificate> getSmcbAuthCertificate() {
    checkCardHandle(SmartcardType.SMC_B, smcbHandle, "Get SMC-B Auth Certificate");
    val readCardCertificateCommand = new ReadCardCertificateCommand(smcbHandle);
    return konnektor.execute(readCardCertificateCommand);
  }

  private void checkCardHandle(SmartcardType type, CardInfo handle, String operationName) {
    if (handle == null) {
      throw new MissingSmartcardException(type, operationName);
    }
  }

  @Override
  public String toString() {
    var ret = konnektor.toString();

    if (hba != null) {
      ret += format(" / HBA (ICCSN {0}) von {1}", hba.getIccsn(), hba.getOwner().getCommonName());
    }

    if (smcb != null) {
      ret += format(" / SMC-B {0}", smcb.getIccsn());
    }

    return ret;
  }

  public static class KonnektorAbilityBuilder {

    private SmcB smcb;
    private Hba hba;

    private KonnektorAbilityBuilder(SmcB smcb) {
      this.smcb = smcb;
    }

    private KonnektorAbilityBuilder(Hba hba) {
      this.hba = hba;
    }

    public KonnektorAbilityBuilder and(Hba hba) {
      this.hba = hba;
      return this;
    }

    public KonnektorAbilityBuilder and(SmcB smcb) {
      this.smcb = smcb;
      return this;
    }

    public UseTheKonnektor on(KonnektorConfiguration cfg) {
      return on(cfg.create());
    }

    public UseTheKonnektor on(Konnektor konnektor) {
      return new UseTheKonnektor(konnektor, smcb, hba);
    }
  }
}