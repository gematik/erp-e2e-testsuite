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

package de.gematik.test.erezept.actors;

import de.gematik.test.core.exceptions.MissingCacheException;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

public class ActorStage {

  @Getter private static final ErpFdTestsuiteFactory config = ErpFdTestsuiteFactory.create();
  private final Map<String, DoctorActor> doctors;
  private final Map<String, PatientActor> patients;
  private final Map<String, PharmacyActor> pharmacies;
  private final Map<String, EuPharmacyActor> euPharmacies;
  private final Map<String, KtrActor> ktrs;

  private final List<ErpActor> actorsCast;

  public ActorStage() {
    this.doctors = new HashMap<>();
    this.patients = new HashMap<>();
    this.pharmacies = new HashMap<>();
    this.euPharmacies = new HashMap<>();
    this.ktrs = new HashMap<>();
    this.actorsCast = new LinkedList<>();
  }

  public void drawTheCurtain() {
    this.actorsCast.forEach(Actor::wrapUp);
  }

  public DoctorActor getDoctorNamed(String name) {
    return getFromCache(DoctorActor.class, doctors, name);
  }

  public PatientActor getPatientNamed(String name) {
    return getFromCache(PatientActor.class, patients, name);
  }

  public PharmacyActor getPharmacyNamed(String name) {
    return getFromCache(PharmacyActor.class, pharmacies, name);
  }

  public EuPharmacyActor getEuPharmacyNamed(String name) {
    return getFromCache(EuPharmacyActor.class, euPharmacies, name);
  }

  public KtrActor getKtrNamed(String name) {
    return getFromCache(KtrActor.class, ktrs, name);
  }

  private <T extends ErpActor> T getFromCache(Class<T> klass, Map<String, T> cache, String name) {
    var actor = cache.get(name);
    if (actor == null) {
      actor = instrumentNewActor(klass, name);
    }
    return actor;
  }

  public <T extends ErpActor> T instrumentNewActor(Class<T> klass, String name) {
    val actor = new Instrumented.InstrumentedBuilder<>(klass).withProperties(name);
    this.actorsCast.add(actor);

    // add to cache for later calls
    if (klass.equals(DoctorActor.class)) {
      config.equipAsDoctor(actor);
      doctors.put(name, (DoctorActor) actor);
    } else if (klass.equals(PatientActor.class)) {
      config.equipAsPatient(actor);
      patients.put(name, (PatientActor) actor);
    } else if (klass.equals(PharmacyActor.class)) {
      config.equipAsPharmacy(actor);
      pharmacies.put(name, (PharmacyActor) actor);
    } else if (klass.equals(EuPharmacyActor.class)) {
      config.equipAsEuPharmacy(actor);
      euPharmacies.put(name, (EuPharmacyActor) actor);
    } else if (klass.equals(KtrActor.class)) {
      config.equipAsKtr(actor);
      ktrs.put(name, (KtrActor) actor);
    } else {
      throw new MissingCacheException(klass);
    }
    return actor;
  }
}
