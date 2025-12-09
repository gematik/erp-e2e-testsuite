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
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Resource;

@ResourceDef(name = "Medication")
@SuppressWarnings({"java:S110"})
public class EuMedication extends GemErpMedication {

  public static EuMedication fromMedication(Medication adaptee) {
    if (adaptee instanceof EuMedication euMedication) {
      return euMedication;
    } else {
      val euMedication = new EuMedication();
      adaptee.copyValues(euMedication);
      return euMedication;
    }
  }

  public static EuMedication fromMedication(Resource adaptee) {
    return fromMedication((Medication) adaptee);
  }
}
