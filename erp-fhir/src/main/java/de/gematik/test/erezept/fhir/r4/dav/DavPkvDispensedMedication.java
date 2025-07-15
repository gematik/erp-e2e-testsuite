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

package de.gematik.test.erezept.fhir.r4.dav;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class DavPkvDispensedMedication extends MedicationDispense {

  public PrescriptionId getPrescriptionId() {
    val id = this.getAuthorizingPrescriptionFirstRep().getIdentifier();
    return PrescriptionId.from(id);
  }

  public String getPerformerReference() {
    return this.getPerformerFirstRep().getActor().getReference();
  }

  public static DavPkvDispensedMedication fromMedicationDispense(MedicationDispense adaptee) {
    val dispensed = new DavPkvDispensedMedication();
    adaptee.copyValues(dispensed);
    return dispensed;
  }

  public static DavPkvDispensedMedication fromMedicationDispense(Resource adaptee) {
    return fromMedicationDispense((MedicationDispense) adaptee);
  }
}
