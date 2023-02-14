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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.resources.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

@Slf4j
@ResourceDef(name = "MedicationRequest")
@SuppressWarnings({"java:S110"})
public class KbvErpMedicationRequest extends MedicationRequest implements ErpFhirResource {

  public static KbvErpMedicationRequest fromMedicationRequest(MedicationRequest adaptee) {
    val kbvMedicationRequest = new KbvErpMedicationRequest();
    adaptee.copyValues(kbvMedicationRequest);
    return kbvMedicationRequest;
  }

  public static KbvErpMedicationRequest fromMedicationRequest(Resource adaptee) {
    return fromMedicationRequest((MedicationRequest) adaptee);
  }

  public String getLogicalId() {
    return this.id.getIdPart();
  }

  public Optional<String> getNoteText() {
    val note = this.getNoteFirstRep().getText();
    return Optional.ofNullable(note);
  }

  /**
   * check if the medicationrequest is a MVO
   *
   * @return true if MVO, false otherwise
   */
  public boolean isMultiple() {
    return this.getExtension().stream()
        .filter(
            ext ->
                ext.getUrl().contains(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .map(ext -> ext.getExtensionByUrl("Kennzeichen"))
        .map(
            kennzeichen ->
                kennzeichen.getValue().castToBoolean(kennzeichen.getValue()).booleanValue())
        .findAny()
        .orElse(false);
  }

  public boolean allowSubstitution() {
    return this.getSubstitution().getAllowedBooleanType().booleanValue();
  }

  public String getNoteTextOr(String alternative) {
    return getNoteText().orElse(alternative);
  }

  public String getNoteTextOrEmpty() {
    return getNoteTextOr("");
  }

  @Override
  public String getDescription() {
    val prescription = this.isMultiple() ? "Mehrfachverordnung" : "Verordnung";
    val dosageInstruction = this.getDosageInstructionFirstRep().getText();
    val quantity = this.getDispenseRequest().getQuantity();
    val autIdem = this.allowSubstitution() ? "mit aut-idem" : "ohne aut-idem";
    return format(
        "{0} {1} für {2} {3} mit Dosieranweisung {4}",
        prescription, autIdem, quantity.getValue(), quantity.getCode(), dosageInstruction);
  }
}
