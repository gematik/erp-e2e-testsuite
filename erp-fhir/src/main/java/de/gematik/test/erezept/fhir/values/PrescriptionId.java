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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.stream.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public class PrescriptionId extends Value<String> {

  public static final ErpWorkflowNamingSystem NAMING_SYSTEM =
      ErpWorkflowNamingSystem.PRESCRIPTION_ID;

  public PrescriptionId(final String value) {
    this(
        "1.2.0".equalsIgnoreCase(System.getProperty("erp.fhir.profile"))
            ? ErpWorkflowNamingSystem.PRESCRIPTION_ID_121
            : ErpWorkflowNamingSystem.PRESCRIPTION_ID,
        value);
  }

  public PrescriptionId(final ErpWorkflowNamingSystem system, final String value) {
    super(system, value);
  }

  public boolean check() {
    return checkId(this.getValue());
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
  public Identifier asIdentifier(INamingSystem system) {
    return new Identifier().setSystem(system.getCanonicalUrl()).setValue(this.getValue());
  }

  /**
   * see {@link this#asIdentifier(INamingSystem)}
   *
   * @param system
   * @return
   */
  public Reference asReference(INamingSystem system) {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier(system));
    return ref;
  }

  public static PrescriptionId random() {
    return random(PrescriptionFlowType.FLOW_TYPE_160);
  }

  public static PrescriptionId random(PrescriptionFlowType flowType) {
    return GemFaker.fakerPrescriptionId(flowType);
  }

  public static PrescriptionId from(Identifier identifier) {
    val system =
        Stream.of(
                ErpWorkflowNamingSystem.PRESCRIPTION_ID,
                ErpWorkflowNamingSystem.PRESCRIPTION_ID_121)
            .filter(ns -> ns.match(identifier.getSystem()))
            .findFirst()
            .orElseThrow(
                () ->
                    new BuilderException(
                        format("Cannot build PrescriptionId from {0}", identifier.getSystem())));
    return new PrescriptionId(system, identifier.getValue());
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
    return system.equals(ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl())
        || system.equals(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121.getCanonicalUrl());
  }
}
