/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.lei.exceptions.MissingSmartcardException;
import de.gematik.test.erezept.lei.exceptions.VerifyPinFailed;
import de.gematik.test.konnektor.CardHandle;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.PinType;
import de.gematik.test.konnektor.cfg.KonnektorConfiguration;
import de.gematik.test.konnektor.commands.*;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.test.smartcard.SmcB;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Ability;

@Slf4j
public class UseTheKonnektor implements Ability {

  @Nullable private SmcB smcb;
  @Nullable private CardHandle smcbHandle;
  @Nullable private Hba hba;
  @Nullable private CardHandle hbaHandle;
  private final Konnektor konnektor;

  private UseTheKonnektor(Konnektor konnektor, @Nullable SmcB smcb, @Nullable Hba hba) {
    this.konnektor = konnektor;
    this.smcb = smcb;
    this.hba = hba;
    this.initCardHandles();
  }

  private void initCardHandles() {
    if (smcb != null) this.smcbHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(smcb));
    if (hba != null) this.hbaHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(hba));
  }

  public byte[] signDocument(String document) {
    if (hbaHandle == null) {
      throw new MissingSmartcardException(SmartcardType.HBA, "Sign Document");
    }

    val title = format("Sign Document with {0} (Cardhandle: {1})", hba, hbaHandle.getHandle());
    Serenity.recordReportData().withTitle(title).andContents(document);
    val signCmd = new SignXMLDocumentCommand(hbaHandle, document);
    return konnektor.execute(signCmd);
  }

  public boolean verifyDocument(byte[] document) {
    Serenity.recordReportData()
        .withTitle(format("Verify Document with length of {0} Bytes", document.length))
        .andContents(Base64.getEncoder().encodeToString(document));
    val verifyCmd = new VerifyDocumentCommand(document);
    return konnektor.execute(verifyCmd);
  }

  public byte[] externalAuthenticate(byte[] challenge) {
    if (smcbHandle == null) {
      throw new MissingSmartcardException(SmartcardType.HBA, "External Authenticate");
    }

    Serenity.recordReportData()
        .withTitle(
            format(
                "VerifyPin for Card {0} with card handle {2} and ICCSN {1}",
                smcbHandle.getType(), smcbHandle.getIccsn(), smcbHandle.getHandle()));
    val pinResponseType = konnektor.execute(new VerifyPinCommand(smcbHandle, PinType.PIN_SMC));
    if (pinResponseType.getPinResult() != PinResultEnum.OK) {
      throw new VerifyPinFailed(smcbHandle.getIccsn());
    }
    val externalAuthenticateCmd = new ExternalAuthenticateCommand(smcbHandle);
    externalAuthenticateCmd.setToBeSignedData(challenge);
    return konnektor.execute(externalAuthenticateCmd);
  }

  public Optional<byte[]> requestEvidenceForEgk(Egk egk) {
    val egkCardHandle = konnektor.execute(GetCardHandleCommand.forSmartcard(egk));
    val readVsdCommand = new ReadVsdCommand(egkCardHandle, smcbHandle, true, true);
    val response = konnektor.execute(readVsdCommand);
    return Optional.ofNullable(response.getPruefungsnachweis());
  }

  public X509Certificate getSmcbAuthCerticate() {
    val readCardCertificateCommand = new ReadCardCertificateCommand(smcbHandle);
    return konnektor.execute(readCardCertificateCommand);
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

  public static KonnektorAbilityBuilder with(SmcB smcb) {
    return new KonnektorAbilityBuilder(smcb);
  }

  public static KonnektorAbilityBuilder with(Hba hba) {
    return new KonnektorAbilityBuilder(hba);
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
