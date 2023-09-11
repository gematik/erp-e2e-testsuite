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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;

public class MedicationDataConverter {
  private final MedicationData medicationData;

  public MedicationDataConverter(MedicationData medicationData) {
    this.medicationData = medicationData;
  }

  public KbvErpMedication convert() {

    return KbvErpMedicationBuilder.builder()
        .isVaccine(false) // default false
        .normgroesse(medicationData.getEnumStandardSize()) // default NB (nicht betroffen)
        .darreichungsform(medicationData.getEnumDarreichungsForm()) // default TAB
        .amount(medicationData.getAmount())
        .pzn(medicationData.getPzn(), medicationData.getName())
        //   .category(medicationData.getEnumCategory())  default 10
        .build();
  }
}
