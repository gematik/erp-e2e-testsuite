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

package de.gematik.test.erezept.fhir.r4.erp.tprescription;

import static de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem.DARREICHUNGSFORM;

import de.gematik.test.erezept.eml.fhir.r4.EpaMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings("java:S110") // we want to extend EpaMedication here
public class ErpTPrescriptionMedication extends EpaMedication {

  public Darreichungsform getDarreichungsform() {
    return Darreichungsform.fromCode(
        this.getForm().getCoding().stream()
            .filter(DARREICHUNGSFORM::matches)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Medication has no Darreichungsform"))
            .getCode());
  }

  public static ErpTPrescriptionMedication fromMedication(Resource adaptee) {
    return fromMedication((Medication) adaptee);
  }

  public static ErpTPrescriptionMedication fromMedication(Medication adaptee) {
    if (adaptee instanceof ErpTPrescriptionMedication erpMedication) {
      return erpMedication;
    } else {
      val tPrescriptionMedication = new ErpTPrescriptionMedication();
      adaptee.copyValues(tPrescriptionMedication);
      return tPrescriptionMedication;
    }
  }
}
