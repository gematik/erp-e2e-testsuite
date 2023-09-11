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

import static java.text.MessageFormat.*;

import de.gematik.test.konnektor.profile.*;
import de.gematik.test.konnektor.soap.mock.*;
import de.gematik.test.konnektor.soap.mock.vsdm.*;
import de.gematik.test.smartcard.*;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.*;
import de.gematik.ws.conn.cardservice.wsdl.v8.*;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.*;
import de.gematik.ws.conn.certificateservice.wsdl.v6.*;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.*;
import de.gematik.ws.conn.eventservice.wsdl.v7.*;
import de.gematik.ws.conn.signatureservice.wsdl.v7.*;
import de.gematik.ws.conn.vsds.vsdservice.v5.*;

public class MockKonnektorServiceProvider extends ServicePortProvider {

  private final MockKonnektor mockKonnektor;
  private final VsdmService vsdmService;

  public MockKonnektorServiceProvider(SmartcardArchive smartcardArchive, VsdmService service) {
    super(new MockProfile());
    this.mockKonnektor = new MockKonnektor(smartcardArchive);
    this.vsdmService = service;
  }

  public MockKonnektorServiceProvider(SmartcardArchive smartcardArchive) {
    this(smartcardArchive, new VsdmService(new byte[32], 'S', '1'));
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
    return new MockVSDServicePortType(this.mockKonnektor, this.vsdmService);
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
