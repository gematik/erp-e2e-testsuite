/*
 * Copyright 2023 gematik GmbH
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
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.PrimSysRestFactory;
import de.gematik.test.erezept.primsys.actors.BaseActor;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.data.actors.ActorDto;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActorContext {

  private static ActorContext instance;

  @Getter
  private final EnvironmentConfiguration environment;

  @Getter private final List<Doctor> doctors;

  @Getter private final List<Pharmacy> pharmacies;

  private final ContextData contextData;

  
  private ActorContext(PrimSysRestFactory factory) {
    this.environment = factory.getActiveEnvironment();
    this.doctors = factory.createDoctorActors();
    this.pharmacies = factory.createPharmacyActors();

    contextData = new ContextData();
  }

  public Optional<Doctor> getDoctor(String id) {
    return this.doctors.stream().filter(doc -> doc.getIdentifier().equals(id)).findFirst();
  }

  public Optional<Pharmacy> getPharmacy(String id) {
    return this.pharmacies.stream().filter(pharm -> pharm.getIdentifier().equals(id)).findFirst();
  }

  public List<BaseActor> getActors() {
    return Stream.concat(doctors.stream(), pharmacies.stream()).toList();
  }

  public List<ActorDto> getActorsSummary() {
      return this.getActors().stream().map(BaseActor::getActorSummary).toList();
  }

  public List<ActorDto> getActorsSummary(ActorType type) {
    return this.getActorsSummary().stream().filter(actor -> actor.getType().equals(type)).toList();
  }

  public void addPrescription(PrescriptionDto prescription) {
    contextData.addPrescription(prescription);
  }

  public void addAcceptedPrescription(AcceptedPrescriptionDto prescription) {
    contextData.addAcceptedPrescription(prescription);
  }

  public void addDispensedMedications(DispensedMedicationDto dispensed) {
    contextData.addDispensedMedications(dispensed);
  }

  public boolean removeAcceptedPrescription(AcceptedPrescriptionDto prescription) {
    return removeAcceptedPrescription(TaskId.from(prescription.getPrescriptionId()));
  }

  public boolean removeAcceptedPrescription(TaskId taskId) {
    return contextData.removeAcceptedPrescription(taskId.getValue());
  }

  public List<PrescriptionDto> getPrescriptions() {
    return contextData.getPrescriptions();
  }

  public List<AcceptedPrescriptionDto> getAcceptedPrescriptions() {
    return contextData.getAcceptedPrescriptions();
  }

  public Optional<AcceptedPrescriptionDto> getAcceptedPrescription(String prescriptionId) {
    return this.getAcceptedPrescriptions().stream()
            .filter(p -> p.getPrescriptionId().equals(prescriptionId))
            .findFirst();
  }

  public List<DispensedMedicationDto> getDispensedMedications() {
    return contextData.getDispensedMedications();
  }

  public Optional<DispensedMedicationDto> getDispensedMedication(String prescriptionId) {
    return this.getDispensedMedications().stream()
            .filter(dd -> dd.getPrescriptionId().equals(prescriptionId))
            .findFirst();
  }

  public void shutdown() {
    // nothing to be done yet
  }

  public static void init(PrimSysRestFactory factory) {
    if (instance == null) {
      instance = new ActorContext(factory);
    }
  }

  public static ActorContext getInstance() {
    if (instance == null) {
      throw new ConfigurationException("ActorContext is not initialized");
    }

    return instance;
  }
}
