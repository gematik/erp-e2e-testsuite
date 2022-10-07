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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.valuesets.PerformerType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/** @see <a href="https://simplifier.net/erezept-workflow/gemerxtask">Gem_erxTask</a> */
@Slf4j
@ResourceDef(name = "Task", profile = StructureDefinitionFixedUrls.GEM_ERX_TASK)
@SuppressWarnings({"java:S110"})
public class ErxTask extends Task {

  /**
   * While ErxTask.getId() returns a qualified ID (Task/[ID]) this method will return only the
   * plain/unqualified ID
   *
   * @return the unqualified ID without the prefixed resource type
   */
  public String getUnqualifiedId() {
    val taskIdTokens = this.getId().split("/");
    return taskIdTokens[taskIdTokens.length - 1];
  }

  public PrescriptionId getPrescriptionId() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                ErpNamingSystem.PRESCRIPTION_ID.getCanonicalUrl().equals(identifier.getSystem()))
        .map(identifier -> new PrescriptionId(identifier.getValue()))
        .findFirst() // Prescription ID has cardinality of 1..1 anyways
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), ErpNamingSystem.PRESCRIPTION_ID));
  }

  public PrescriptionFlowType getFlowType() {
    return this.getExtensionsByUrl(ErpStructureDefinition.GEM_PRESCRIPTION_TYPE.getCanonicalUrl())
        .stream()
        .map(Extension::getValue)
        .filter(Coding.class::isInstance)
        .map(Coding.class::cast)
        .filter(coding -> ErpCodeSystem.FLOW_TYPE.getCanonicalUrl().equals(coding.getSystem()))
        .map(coding -> PrescriptionFlowType.fromCode(coding.getCode()))
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), PrescriptionFlowType.CODE_SYSTEM));
  }

  public Optional<AccessCode> getOptionalAccessCode() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                ErpNamingSystem.ACCESS_CODE.getCanonicalUrl().equals(identifier.getSystem()))
        .map(identifier -> new AccessCode(identifier.getValue()))
        .findFirst(); // AccessCode has cardinality of 0..1 -> is optional
  }

  public AccessCode getAccessCode() {
    return this.getOptionalAccessCode()
        .orElseThrow(() -> new MissingFieldException(ErxTask.class, ErpNamingSystem.ACCESS_CODE));
  }

  public boolean hasAccessCode() {
    return this.getOptionalAccessCode().isPresent();
  }

  public Optional<Secret> getSecret() {
    return this.getIdentifier().stream()
        .filter(
            identifier -> ErpNamingSystem.SECRET.getCanonicalUrl().equals(identifier.getSystem()))
        .map(identifier -> new Secret(identifier.getValue()))
        .findFirst(); // Secret has cardinality of 0..1 -> is optional
  }

  public boolean hasSecret() {
    return this.getSecret().isPresent();
  }

  public PerformerType getPerformerFirstRep() {
    return PerformerType.fromCode(this.getPerformerTypeFirstRep().getCodingFirstRep().getCode());
  }

  public Optional<String> getForKvid() {
    return Optional.ofNullable(this.getFor().getIdentifier().getValue());
  }

  /**
   * This constructor translates a Task into a ErxTask. For example if you receive an
   * ErxPrescriptionBundle HAPI interprets the containing Task as plain HAPI-Task and not as a
   * ErxTask. This constructor allows mapping to ErxTask
   *
   * @param adaptee
   */
  public static ErxTask fromTask(Task adaptee) {
    val erxTask = new ErxTask();
    adaptee.copyValues(erxTask);
    return erxTask;
  }

  public static ErxTask fromTask(Resource adaptee) {
    return fromTask((Task) adaptee);
  }

  @Override
  public String toString() {
    val profile =
        this.getMeta().getProfile().stream()
            .map(PrimitiveType::asStringValue)
            .collect(Collectors.joining(", "));
    return format("{0} from Profile {1}", this.getClass().getSimpleName(), profile);
  }
}
