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

package de.gematik.test.erezept.fhir.resources.kbv;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(
    name = "MedicationRequest",
    profile = StructureDefinitionFixedUrls.KBV_PR_ERP_PRESCRIPTION)
public class KbvErpMedicationRequest extends MedicationRequest {

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
                ext.getUrl()
                    .equals(ErpStructureDefinition.KBV_MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .map(ext -> ext.getExtensionByUrl("Kennzeichen"))
        .map(
            kennzeichen ->
                kennzeichen.getValue().castToBoolean(kennzeichen.getValue()).booleanValue())
        .findAny()
        .orElse(false);
  }

  public String getNoteTextOr(String alternative) {
    return getNoteText().orElse(alternative);
  }

  public String getNoteTextOrEmpty() {
    return getNoteTextOr("");
  }

  public static KbvErpMedicationRequest fromMedicationRequest(MedicationRequest adaptee) {
    val kbvMedicationRequest = new KbvErpMedicationRequest();
    adaptee.copyValues(kbvMedicationRequest);
    return kbvMedicationRequest;
  }

  public static KbvErpMedicationRequest fromMedicationRequest(Resource adaptee) {
    return fromMedicationRequest((MedicationRequest) adaptee);
  }
}
