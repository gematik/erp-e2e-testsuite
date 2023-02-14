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

package de.gematik.test.konnektor.soap;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.profile.MockProfile;
import de.gematik.test.konnektor.soap.mock.MockAuthSignatureServicePortType;
import de.gematik.test.konnektor.soap.mock.MockCardServicePortType;
import de.gematik.test.konnektor.soap.mock.MockCertificateServicePortType;
import de.gematik.test.konnektor.soap.mock.MockEncryptionPortType;
import de.gematik.test.konnektor.soap.mock.MockEventServicePortType;
import de.gematik.test.konnektor.soap.mock.MockKonnektor;
import de.gematik.test.konnektor.soap.mock.MockSignatureServicePortType;
import de.gematik.test.konnektor.soap.mock.MockVSDServicePortType;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;

public class MockKonnektorServiceProvider extends ServicePortProvider {

  private final MockKonnektor mockKonnektor;

  public MockKonnektorServiceProvider(SmartcardArchive smartcardArchive) {
    super(new MockProfile());
    this.mockKonnektor = new MockKonnektor(smartcardArchive);
  }

  @Override
  public AuthSignatureServicePortType getAuthSignatureService() {
    return new MockAuthSignatureServicePortType(mockKonnektor);
  }

  @Override
  public CertificateServicePortType getCertificateService() {
    return new MockCertificateServicePortType(mockKonnektor);
  }

  @Override
  public EventServicePortType getEventService() {
    return new MockEventServicePortType(mockKonnektor);
  }

  @Override
  public SignatureServicePortType getSignatureService() {
    return new MockSignatureServicePortType(this.mockKonnektor);
  }

  @Override
  public CardServicePortType getCardService() {
    return new MockCardServicePortType(this.mockKonnektor);
  }

  @Override
  public CardTerminalServicePortType getCardTerminalService() {
    return null;
  }

  @Override
  public VSDServicePortType getVSDServicePortType() {
    return new MockVSDServicePortType(this.mockKonnektor);
  }

  @Override
  public EncryptionServicePortType getEncryptionServicePortType() {
    return new MockEncryptionPortType(this.mockKonnektor);
  }

  @Override
  public String toString() {
    return format("{0}", profile.getType());
  }
}
