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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.Value;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Pattern;
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

  public TaskId getBasedOnReferenceId() {
    val reference = this.getFixedBasedOnReference();
    val idToken = reference.split("/")[1];
    return TaskId.from(idToken.split("\\?")[0]);
  }

  public Optional<AccessCode> getBasedOnAccessCode() {
    val pattern = Pattern.compile("ac=(.+)");
    val ref = this.getBasedOnFirstRep().getReference();
    val matcher = pattern.matcher(ref);
    if (matcher.find()) {
      val ac = AccessCode.fromString(matcher.group(1));
      return Optional.of(ac);
    } else {
      return Optional.empty();
    }
  }

  public LocalDateTime getSentDate() {
    return this.getSent().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
            ext -> ext.getUrl().equals(ErpWorkflowStructDef.SUBSTITUTION_ALLOWED.getCanonicalUrl()))
        .map(ext -> ext.castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(true);
  }

  @SuppressWarnings({"java:S1452"}) // the concrete ProfileVersion-Type is not of interest for now
  public ICommunicationType<ProfileVersion<?>> getType() {
    return this.getMeta().getProfile().stream()
        .map(url -> ICommunicationType.fromUrl(url.getValue()))
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
}
