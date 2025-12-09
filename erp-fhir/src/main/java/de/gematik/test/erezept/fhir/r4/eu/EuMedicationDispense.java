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

package de.gematik.test.erezept.fhir.r4.eu;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Resource;

@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class EuMedicationDispense extends ErxMedicationDispense {

  public static EuMedicationDispense fromMedicationDispense(MedicationDispense adaptee) {
    if (adaptee instanceof EuMedicationDispense eumd) {
      return eumd;
    } else {
      val euMedicationDispense = new EuMedicationDispense();
      adaptee.copyValues(euMedicationDispense);
      return euMedicationDispense;
    }
  }

  public static EuMedicationDispense fromMedicationDispense(Resource adaptee) {
    return fromMedicationDispense((MedicationDispense) adaptee);
  }
}
