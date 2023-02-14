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

package de.gematik.test.erezept.primsys.rest.data;

import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.val;

@Data
@XmlRootElement
public class PrescriptionData {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private String prescriptionId;
  private String taskId;
  private String accessCode;
  private DoctorData practitioner;
  private PatientData patient;
  private MedicationData medication;
  private CoverageData coverage;
  private Date authoredOn;
  private String authoredOnFormatted;

  public static PrescriptionData create(
      Doctor practitioner,
      ErxTask prescriptionTask,
      PatientData patient,
      CoverageData coverage,
      MedicationData medication) {
    return create(practitioner.getBaseData(), prescriptionTask, patient, coverage, medication);
  }

  public static PrescriptionData create(
      DoctorData practitioner,
      ErxTask prescriptionTask,
      PatientData patient,
      CoverageData coverage,
      MedicationData medication) {
    val prescriptionData = new PrescriptionData();
    prescriptionData.setPractitioner(practitioner);
    prescriptionData.setPrescriptionId(prescriptionTask.getPrescriptionId().getValue());
    prescriptionData.setAccessCode(prescriptionTask.getAccessCode().getValue());
    prescriptionData.setTaskId(prescriptionTask.getUnqualifiedId());
    prescriptionData.setAuthoredOn(prescriptionTask.getAuthoredOn());
    prescriptionData.setAuthoredOnFormatted(formatDate(prescriptionTask.getAuthoredOn()));
    patient.fakeMissing();
    prescriptionData.setPatient(patient);
    prescriptionData.setMedication(medication);
    prescriptionData.setCoverage(coverage);

    return prescriptionData;
  }

  private static String formatDate(Date date) {
    val ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return FORMATTER.format(ldt);
  }
}
