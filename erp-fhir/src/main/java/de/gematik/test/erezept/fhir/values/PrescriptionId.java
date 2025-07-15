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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

public class PrescriptionId extends SemanticValue<String, ErpWorkflowNamingSystem> {

  private PrescriptionId(String value) {
    this(ErpWorkflowNamingSystem.PRESCRIPTION_ID, value);
  }

  private PrescriptionId(ErpWorkflowNamingSystem system, String value) {
    super(system, value);
  }

  public static PrescriptionId random() {
    return random(PrescriptionFlowType.FLOW_TYPE_160);
  }

  public static PrescriptionId random(PrescriptionFlowType flowType) {
    return PrescriptionId.from(GemFaker.fakerPrescriptionId(flowType));
  }

  public static PrescriptionId from(Identifier identifier) {
    return Optional.of(identifier)
        .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
        .map(id -> PrescriptionId.from(id.getValue()))
        .orElseThrow(
            () ->
                new BuilderException(
                    format("Cannot extract PrescriptionId from {0}", identifier.getSystem())));
  }

  public static PrescriptionId from(TaskId id) {
    return from(id.getValue());
  }

  public static PrescriptionId from(String id) {
    return new PrescriptionId(id);
  }

  public static boolean checkId(PrescriptionId prescriptionId) {
    return checkId(prescriptionId.getValue());
  }

  public static boolean checkId(String value) {
    val raw = Long.parseLong(value.replace(".", ""));
    val check = raw % 97;
    return check == 1;
  }

  public static boolean isPrescriptionId(Identifier identifier) {
    return isPrescriptionId(identifier.getSystem());
  }

  public static boolean isPrescriptionId(String system) {
    if (system == null) {
      return false;
    }
    return ErpWorkflowNamingSystem.PRESCRIPTION_ID.matches(system);
  }

  public boolean check() {
    return checkId(this.getValue());
  }

  public TaskId toTaskId() {
    return TaskId.from(this);
  }

  public PrescriptionFlowType getFlowType() {
    val firstChunk = this.getValue().split("\\.")[0];
    return PrescriptionFlowType.fromCode(firstChunk);
  }

  /**
   * Note: ErpWorkflowNamingSystem.PRESCRIPTION_ID is the default NamingSystem for PrescriptionId,
   * however on some newer profiles ErpWorkflowNamingSystem.PRESCRIPTION_ID_121 is required. This
   * method will become obsolete once all profiles are using a common naming system for
   * PrescriptionId
   *
   * @param system to use for encoding the PrescriptionId value
   * @return the PrescriptionId as Identifier
   */
  public Identifier asIdentifier(WithNamingSystem system) {
    return new Identifier().setSystem(system.getCanonicalUrl()).setValue(this.getValue());
  }

  public Reference asReference(WithNamingSystem system) {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier(system));
    return ref;
  }
}
