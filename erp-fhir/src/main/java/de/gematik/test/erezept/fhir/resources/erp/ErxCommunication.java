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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.InvalidCommunicationType;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Value;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Communication")
@SuppressWarnings({"java:S110"})
public class ErxCommunication extends Communication {

  /**
   * While Communication.getId() returns a qualified ID (Communication/[ID]) this method will return
   * only the plain/unqualified ID
   *
   * @return the unqualified ID without the prefixed resource type
   */
  public String getUnqualifiedId() {
    return IdentifierUtil.getUnqualifiedId(this.getId());
  }

  public String getBasedOnReferenceId() {
    val reference = this.getFixedBasedOnReference();
    return reference.split("/")[1];
  }

  public LocalDateTime getSentDate() {
    return this.getSent().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public Optional<AccessCode> getBasedOnAccessCode() {
    val reference = this.getFixedBasedOnReference();
    // second: split the reference
    val tokens = reference.split("/");
    // if we have 3 tokens, the last one contains the AccessCode
    if (tokens.length >= 3) {
      val acToken = tokens[2];
      val accessCode = acToken.split("=")[1];
      return Optional.of(new AccessCode(accessCode));
    } else {
      return Optional.empty();
    }
  }

  /**
   * This method fetches the first basedOn-Entry and cuts the prepended '/' at the resource type.
   *
   * <p>first: clean the reference if it is staring with a / (prepended by HAPI) HAPI-Bug? -> see
   * FhirParser.fixEncoded()
   *
   * @return the Reference/ID of the resource which this communication is based on without the
   *     prepended '/'
   */
  private String getFixedBasedOnReference() {
    val rawRef = this.getBasedOnFirstRep().getReference();
    // now cut the prefixed / which are prepended by HAPI, if one was prepended at all
    return rawRef.startsWith("/") ? rawRef.replaceFirst("/", "") : rawRef;
  }

  public Optional<String> getBasedOnAccessCodeString() {
    return getBasedOnAccessCode().map(Value::getValue);
  }

  public Optional<String> getAboutReference() {
    Optional<String> ret = Optional.empty();
    if (hasAboutReference()) {
      ret = Optional.of(this.getAboutFirstRep().getReference());
    }
    return ret;
  }

  public boolean hasAboutReference() {
    return !this.getAbout().isEmpty();
  }

  public String getSenderId() {
    return this.getSender().getIdentifier().getValue();
  }

  public String getRecipientId() {
    return this.getRecipientFirstRep().getIdentifier().getValue();
  }

  public String getMessage() {
    return this.getPayload().stream()
        .map(payload -> payload.getContentStringType().getValue())
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Message Payload"));
  }

  public boolean isSubstitutionAllowed() {
    return this.getPayload().get(0).getExtension().stream()
        .filter(
            ext ->
                ext.getUrl()
                    .equals(ErpStructureDefinition.GEM_SUBSTITION_ALLOWED.getCanonicalUrl()))
        .map(ext -> ext.castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(true);
  }

  public CommunicationType getType() {
    return this.getMeta().getProfile().stream()
        .map(url -> CommunicationType.fromUrl(url.getValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(ErxCommunication.class, "Profile of Communication Type"));
  }

  public static ErxCommunication fromCommunication(Communication adaptee) {
    val erxCommunication = new ErxCommunication();
    adaptee.copyValues(erxCommunication);
    return erxCommunication;
  }

  public static ErxCommunication fromCommunication(Resource adaptee) {
    return fromCommunication((Communication) adaptee);
  }

  public enum CommunicationType {
    INFO_REQ(ErpStructureDefinition.GEM_COM_INFO_REQ),
    DISP_REQ(ErpStructureDefinition.GEM_COM_DISP_REQ),
    REPLY(ErpStructureDefinition.GEM_COM_REPLY),
    REPRESENTATIVE(ErpStructureDefinition.GEM_COM_REPRESENTATIVE),
    CHANGE_REQ(ErpStructureDefinition.GEM_COM_CHARGE_CHANGE_REQ),
    CHANGE_REPLY(ErpStructureDefinition.GEM_COM_CHARGE_CHANGE_REPLY);

    // which communication types are received by KVIDs
    private static final List<CommunicationType> PATIENT_RECEIVING =
        List.of(REPLY, REPRESENTATIVE, CHANGE_REPLY);

    // which communication types are sent by pharmacies
    private static final List<CommunicationType> PHARMACY_SENDING = List.of(REPLY, CHANGE_REPLY);

    @Getter private final ErpStructureDefinition type;

    CommunicationType(ErpStructureDefinition type) {
      this.type = type;
    }

    public String getTypeUrl() {
      return type.getCanonicalUrl();
    }

    public ErpNamingSystem getRecipientNamingSystem() {
      ErpNamingSystem ns;
      if (PATIENT_RECEIVING.contains(this)) {
        ns = ErpNamingSystem.KVID; // TODO: not always true, Patient might also be PKV!
      } else {
        ns = ErpNamingSystem.TELEMATIK_ID;
      }
      return ns;
    }

    public ErpNamingSystem getSenderNamingSystem() {
      ErpNamingSystem ns;
      if (PHARMACY_SENDING.contains(this)) {
        ns = ErpNamingSystem.TELEMATIK_ID;
      } else {
        ns = ErpNamingSystem.KVID; // TODO: not always true, Patient might also be PKV!
      }
      return ns;
    }

    public static CommunicationType fromUrl(@NonNull final String profileUrl) {
      return Arrays.stream(CommunicationType.values())
          .filter(type -> type.getTypeUrl().contains(profileUrl))
          .findFirst()
          .orElseThrow(() -> new InvalidCommunicationType(profileUrl));
    }
  }
}
