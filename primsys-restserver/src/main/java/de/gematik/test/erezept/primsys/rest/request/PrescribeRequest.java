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

package de.gematik.test.erezept.primsys.rest.request;

import static de.gematik.test.erezept.primsys.rest.data.CoverageData.create;

import de.gematik.test.erezept.primsys.rest.data.CoverageData;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;
import de.gematik.test.erezept.primsys.rest.data.PatientData;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement
public class PrescribeRequest {

  private PatientData patient;
  private CoverageData coverage;
  private MedicationData medication;

  public CoverageData getCoverage() {
    if (coverage == null) {
      coverage = create();
    } else {
      coverage.fakeMissing();
    }
    return coverage;
  }

  public MedicationData getMedication() {
    if (medication == null) {
      medication = MedicationData.create();
    } else {
      medication.fakeMissing();
    }
    return medication;
  }

  public PatientData getPatient() {
    if (patient == null) {
      patient = PatientData.create();
    } else {
      patient.fakeMissing();
    }
    return patient;
  }
}
