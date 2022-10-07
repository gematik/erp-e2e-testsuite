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

package de.gematik.test.konnektor.soap;

import de.gematik.test.konnektor.profile.KonnektorProfile;
import de.gematik.test.konnektor.profile.ProfileType;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import lombok.Getter;

public abstract class ServicePortProvider {

  @Getter protected final KonnektorProfile profile;

  protected ServicePortProvider(KonnektorProfile profile) {
    this.profile = profile;
  }

  public final ProfileType getType() {
    return profile.getType();
  }

  public abstract AuthSignatureServicePortType getAuthSignatureService();

  public abstract CertificateServicePortType getCertificateService();

  public abstract EventServicePortType getEventService();

  public abstract SignatureServicePortType getSignatureService();

  public abstract CardServicePortType getCardService();

  public abstract CardTerminalServicePortType getCardTerminalService();

  public abstract VSDServicePortType getVSDServicePortType();

  public abstract EncryptionServicePortType getEncryptionServicePortType();
}
