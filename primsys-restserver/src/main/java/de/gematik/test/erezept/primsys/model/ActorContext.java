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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.primsys.model.actor.BaseActor;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  @Getter private final List<PrescriptionData> prescriptions;

  @Getter private final List<AcceptData> acceptedPrescriptions;

  @Getter private final List<DispensedData> dispensedMedications;

  private ActorContext() {
    val cfg = TestsuiteConfiguration.getInstance();
    this.sca = SmartcardFactory.readArchive();

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

    this.prescriptions = new ArrayList<>();
    this.acceptedPrescriptions = new ArrayList<>();
    this.dispensedMedications = new ArrayList<>();
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
    return Stream.concat(doctors.stream(), pharmacies.stream()).collect(Collectors.toList());
  }

  public void addPrescription(PrescriptionData prescription) {
    this.prescriptions.add(prescription);
  }

  public void addAcceptedPrescription(AcceptData prescription) {
    this.acceptedPrescriptions.add(prescription);
  }

  public void addDispensedMedications(DispensedData dispensed) {
    this.dispensedMedications.add(dispensed);
  }

  public boolean removeAcceptedPrescription(AcceptData prescription) {
    return this.acceptedPrescriptions.removeIf(
        ad -> ad.getTaskId().equals(prescription.getTaskId()));
  }

  public void shutdown() {
    sca.destroy();
  }
}
