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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.primsys.model.actor.BaseActor;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ActorContext {

  private static ActorContext instance;

  private final SmartcardArchive sca;

  @Getter private final List<Doctor> doctors;

  @Getter private final List<Pharmacy> pharmacies;

  private final ContextData contextData;

  private ActorContext() {
    val cfg = TestsuiteConfiguration.getInstance();
    this.sca = SmartcardFactory.getArchive();

    this.doctors = new ArrayList<>();
    cfg.getActors()
        .getDoctors()
        .forEach(
            d ->
                this.doctors.add(
                    new Doctor(
                        d, cfg.getActiveEnvironment(), cfg.instantiateDoctorKonnektor(d), sca)));

    this.pharmacies = new ArrayList<>();
    cfg.getActors()
        .getPharmacies()
        .forEach(p -> this.pharmacies.add(new Pharmacy(p, cfg.getActiveEnvironment(), sca)));

    contextData = new ContextData();
  }

  public static ActorContext getInstance() {
    if (instance == null) {
      instance = new ActorContext();
    }

    return instance;
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

  public void addPrescription(PrescriptionData prescription) {
    contextData.addPrescription(prescription);
  }

  public void addAcceptedPrescription(AcceptData prescription) {
    contextData.addAcceptedPrescription(prescription);
  }

  public void addDispensedMedications(DispensedData dispensed) {
    contextData.addDispensedMedications(dispensed);
  }

  public boolean removeAcceptedPrescription(AcceptData prescription) {
    return removeAcceptedPrescription(prescription.getTaskId());
  }

  public boolean removeAcceptedPrescription(String taskId) {
    return contextData.removeAcceptedPrescription(taskId);
  }

  public List<PrescriptionData> getPrescriptions() {
    return contextData.getPrescriptions();
  }

  public List<AcceptData> getAcceptedPrescriptions() {
    return contextData.getAcceptedPrescriptions();
  }

  public List<DispensedData> getDispensedMedications() {
    return contextData.getDispensedMedications();
  }

  public void shutdown() {
    // nothing to be done yet
  }
}
