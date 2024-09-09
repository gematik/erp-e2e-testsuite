/*
 * Copyright 2024 gematik GmbH
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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.PerformerType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/gemerxtask">Gem_erxTask</a>
 */
@Slf4j
@ResourceDef(name = "Task")
@SuppressWarnings({"java:S110"})
public class ErxTask extends Task {

  /**
   * While ErxTask.getId() returns a qualified ID (Task/[ID]) this method will return only the
   * plain/unqualified ID
   *
   * @return the unqualified ID without the prefixed resource type
   */
  private String getUnqualifiedId() {
    val taskIdTokens = this.getId().split("/");
    return taskIdTokens[taskIdTokens.length - 1];
  }

  public TaskId getTaskId() {
    return TaskId.from(this.getUnqualifiedId());
  }

  public PrescriptionId getPrescriptionId() {
    return this.getIdentifier().stream()
        .filter(PrescriptionId::isPrescriptionId)
        .map(PrescriptionId::from)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID,
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID_121));
  }

  public PrescriptionFlowType getFlowType() {
    return this.getExtension().stream()
        .filter(
            extension ->
                extension.getUrl().equals(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl())
                    || extension
                        .getUrl()
                        .equals(ErpWorkflowStructDef.PRESCRIPTION_TYPE_12.getCanonicalUrl()))
        .map(Extension::getValue)
        .filter(Coding.class::isInstance)
        .map(Coding.class::cast)
        .filter(
            coding ->
                ErpWorkflowCodeSystem.FLOW_TYPE.getCanonicalUrl().equals(coding.getSystem())
                    || ErpWorkflowCodeSystem.FLOW_TYPE_12
                        .getCanonicalUrl()
                        .equals(coding.getSystem()))
        .map(coding -> PrescriptionFlowType.fromCode(coding.getCode()))
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), PrescriptionFlowType.CODE_SYSTEM));
  }

  public Optional<AccessCode> getOptionalAccessCode() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                ErpWorkflowNamingSystem.ACCESS_CODE.getCanonicalUrl().equals(identifier.getSystem())
                    || ErpWorkflowNamingSystem.ACCESS_CODE_121
                        .getCanonicalUrl()
                        .equals(identifier.getSystem()))
        .map(identifier -> new AccessCode(identifier.getValue()))
        .findFirst(); // AccessCode has cardinality of 0..1 -> is optional
  }

  public AccessCode getAccessCode() {
    return this.getOptionalAccessCode()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxTask.class,
                    ErpWorkflowNamingSystem.ACCESS_CODE,
                    ErpWorkflowNamingSystem.ACCESS_CODE_121));
  }

  public boolean hasAccessCode() {
    return this.getOptionalAccessCode().isPresent();
  }

  public Optional<Secret> getSecret() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                ErpWorkflowNamingSystem.SECRET.getCanonicalUrl().equals(identifier.getSystem())
                    || ErpWorkflowNamingSystem.SECRET_12
                        .getCanonicalUrl()
                        .equals(identifier.getSystem()))
        .map(identifier -> new Secret(identifier.getValue()))
        .findFirst(); // Secret has cardinality of 0..1 -> is optional
  }

  public boolean hasSecret() {
    return this.getSecret().isPresent();
  }

  public PerformerType getPerformerFirstRep() {
    return PerformerType.fromCode(this.getPerformerTypeFirstRep().getCodingFirstRep().getCode());
  }

  public Optional<KVNR> getForKvnr() {
    val kvnrValue = this.getFor().getIdentifier().getValue();
    if (kvnrValue == null) {
      return Optional.empty();
    } else {
      return Optional.of(KVNR.from(kvnrValue));
    }
  }

  public Date getExpiryDate() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ErpWorkflowStructDef.EXPIRY_DATE_12.match(ext.getUrl())
                    || ErpWorkflowStructDef.EXPIRY_DATE.match(ext.getUrl()))
        .map(ext -> DateConverter.getInstance().dateFromIso8601(ext.getValue().primitiveValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowStructDef.EXPIRY_DATE_12,
                    ErpWorkflowStructDef.EXPIRY_DATE));
  }

  public Date getAcceptDate() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ErpWorkflowStructDef.ACCEPT_DATE_12.match(ext.getUrl())
                    || ErpWorkflowStructDef.ACCEPT_DATE.match(ext.getUrl()))
        .map(ext -> DateConverter.getInstance().dateFromIso8601(ext.getValue().primitiveValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowStructDef.ACCEPT_DATE_12,
                    ErpWorkflowStructDef.ACCEPT_DATE));
  }

  @Override
  public String toString() {
    val profile =
        this.getMeta().getProfile().stream()
            .map(PrimitiveType::asStringValue)
            .collect(Collectors.joining(", "));
    return format("{0} from Profile {1}", this.getClass().getSimpleName(), profile);
  }

  /**
   * This constructor translates a Task into a ErxTask. For example if you receive an
   * ErxPrescriptionBundle HAPI interprets the containing Task as plain HAPI-Task and not as a
   * ErxTask. This constructor allows mapping to ErxTask
   *
   * @param adaptee
   */
  public static ErxTask fromTask(Task adaptee) {
    if (adaptee instanceof ErxTask erxTask) {
      return erxTask;
    } else {
      val erxTask = new ErxTask();
      adaptee.copyValues(erxTask);
      return erxTask;
    }
  }

  public static ErxTask fromTask(Resource adaptee) {
    return fromTask((Task) adaptee);
  }
}
