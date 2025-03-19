/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.erp;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PerformerType;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

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
                WithSystem.anyOf(
                        ErpWorkflowStructDef.PRESCRIPTION_TYPE_12,
                        ErpWorkflowStructDef.PRESCRIPTION_TYPE)
                    .matches(extension))
        .map(Extension::getValue)
        .filter(Coding.class::isInstance)
        .map(Coding.class::cast)
        .filter(
            coding ->
                WithSystem.anyOf(
                        ErpWorkflowCodeSystem.FLOW_TYPE_12, ErpWorkflowCodeSystem.FLOW_TYPE)
                    .matches(coding))
        .map(coding -> PrescriptionFlowType.fromCode(coding.getCode()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowCodeSystem.FLOW_TYPE,
                    ErpWorkflowCodeSystem.FLOW_TYPE_12));
  }

  public Optional<AccessCode> getOptionalAccessCode() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                WithSystem.anyOf(
                        ErpWorkflowNamingSystem.ACCESS_CODE_121,
                        ErpWorkflowNamingSystem.ACCESS_CODE)
                    .matches(identifier))
        .map(identifier -> new AccessCode(identifier.getValue()))
        .findFirst();
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
                WithSystem.anyOf(ErpWorkflowNamingSystem.SECRET_12, ErpWorkflowNamingSystem.SECRET)
                    .matches(identifier))
        .map(identifier -> new Secret(identifier.getValue()))
        .findFirst();
  }

  public boolean hasSecret() {
    return this.getSecret().isPresent();
  }

  public PerformerType getPerformerFirstRep() {
    return PerformerType.fromCode(this.getPerformerTypeFirstRep().getCodingFirstRep().getCode());
  }

  public Optional<KVNR> getForKvnr() {
    return KVNR.extractFrom(this.getFor().getIdentifier());
  }

  public Date getExpiryDate() {
    return this.getExtension().stream()
        .filter(
            extension ->
                WithSystem.anyOf(
                        ErpWorkflowStructDef.EXPIRY_DATE_12, ErpWorkflowStructDef.EXPIRY_DATE)
                    .matches(extension))
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
            extension ->
                WithSystem.anyOf(
                        ErpWorkflowStructDef.ACCEPT_DATE_12, ErpWorkflowStructDef.ACCEPT_DATE)
                    .matches(extension))
        .map(ext -> DateConverter.getInstance().dateFromIso8601(ext.getValue().primitiveValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowStructDef.ACCEPT_DATE_12,
                    ErpWorkflowStructDef.ACCEPT_DATE));
  }

  public Optional<Instant> getLastMedicationDispenseDate() {
    return this.getExtension().stream()
        .filter(ErpWorkflowStructDef.LAST_MEDICATION_DISPENSE::matches)
        .map(ext -> ext.getValue().castToInstant(ext.getValue()).getValue().toInstant())
        .findFirst();
  }

  public Optional<InstantType> getLastMedicationDispenseDateElement() {
    return this.getExtension().stream()
        .filter(ErpWorkflowStructDef.LAST_MEDICATION_DISPENSE::matches)
        .map(ext -> ext.getValue().castToInstant(ext.getValue()))
        .findFirst();
  }

  public boolean hasLastMedicationDispenseDate() {
    return this.getLastMedicationDispenseDate().isPresent();
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
