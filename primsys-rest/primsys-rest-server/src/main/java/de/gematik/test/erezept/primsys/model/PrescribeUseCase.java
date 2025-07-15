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

package de.gematik.test.erezept.primsys.model;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvBaseBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.mapping.CoverageDataMapper;
import de.gematik.test.erezept.primsys.mapping.PatientDataMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class PrescribeUseCase<B extends KbvBaseBundle> {

  protected final Doctor doctor;

  protected PrescribeUseCase(Doctor doctor) {
    this.doctor = doctor;
  }

  protected PrescriptionDto prescribeFor(B bundle, PrescriptionFlowType flowType) {
    var erxTask = this.createTaskFor(flowType);
    this.adjustBundle(erxTask, bundle);
    val signedKbv = this.signPrescription(bundle);
    erxTask = this.activateTaskFor(erxTask, signedKbv);
    return this.createPrescriptionDto(erxTask, bundle);
  }

  protected ErxTask createTaskFor(PrescriptionFlowType flowType) {
    val create = new TaskCreateCommand(flowType);
    val erxTask = doctor.erpRequest2(create);
    log.info(
        "Task created by {} (authoredOn {}) for Flow-Type {}",
        doctor.getName(),
        erxTask.getAuthoredOn(),
        flowType.getCode());
    return erxTask;
  }

  protected ErxTask activateTaskFor(ErxTask draftTask, byte[] signedBundle) {
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();
    val activate = new TaskActivateCommand(draftTask.getTaskId(), accessCode, signedBundle);
    val readyErxTask = doctor.erpRequest2(activate);
    val forKvnr = readyErxTask.getForKvnr().map(KVNR::getValue).orElse("no KVNR");
    log.info(
        "Task activated by {} (authoredOn {}) for {}",
        doctor.getName(),
        readyErxTask.getAuthoredOn(),
        forKvnr);
    log.info(
        "Task: {} / {}",
        readyErxTask.getPrescriptionId().getValue(),
        readyErxTask
            .getOptionalAccessCode()
            .map(SemanticValue::getValue)
            .orElse("without AccessCode"));
    return readyErxTask;
  }

  protected void adjustBundle(ErxTask from, B to) {
    // make sure we get the value of the prescription ID but still remain the system from the
    // original kbv bundle because otherwise we will have a version mixture
    val draftPrescriptionId = from.getPrescriptionId();
    val kbvBundlePrescriptionIdentifier = to.getPrescriptionId().asIdentifier();
    kbvBundlePrescriptionIdentifier.setValue(draftPrescriptionId.getValue());
    val prescriptionId = PrescriptionId.from(kbvBundlePrescriptionIdentifier);

    val taskId = from.getTaskId();
    to.setPrescriptionId(prescriptionId);
    to.setId(taskId.getValue());

    // will update all dates contained within the KBV Bundle to current date
    to.setAllDates();
    if (to instanceof KbvErpBundle kbvErp && kbvErp.getMedicationRequest().isMultiple()) {
      kbvErp.getMedicationRequest().updateMvoDates();
    }

    /*
    While lastUpdated is optional for most WFs, it is not permitted to set lastUpdated
    for the WF 162 (more precisely the KBV_PR_EVDGA_Bundle).
    */
    to.getMeta().setLastUpdated(null);
  }

  protected byte[] signPrescription(B bundle) {
    val xml = doctor.encode(bundle, EncodingType.XML);
    log.info("Doctor {} signs Prescription: {}", doctor.getName(), xml);
    return doctor.signDocument(xml);
  }

  private PrescriptionDto createPrescriptionDto(ErxTask activatedTask, B bundle) {
    val patientMapper = PatientDataMapper.from(bundle.getPatient());
    val coverageMapper = CoverageDataMapper.from(bundle.getCoverage(), bundle.getPatient());

    val doctorData = doctor.getDoctorInformation(bundle);

    val builder =
        PrescriptionDto.builder()
            .practitioner(doctorData)
            .prescriptionId(activatedTask.getPrescriptionId().getValue())
            .accessCode(activatedTask.getAccessCode().getValue())
            .taskId(activatedTask.getTaskId().getValue())
            .authoredOn(activatedTask.getAuthoredOn())
            .patient(patientMapper.getDto())
            .coverage(coverageMapper.getDto());

    val dto = this.finalise(bundle, builder);
    ActorContext.getInstance().addPrescription(dto);
    return dto;
  }

  protected abstract PrescriptionDto finalise(
      B bundle, PrescriptionDto.PrescriptionDtoBuilder builder);
}
