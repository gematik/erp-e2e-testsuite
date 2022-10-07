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

package de.gematik.test.erezept.actors;

import de.gematik.test.core.exceptions.MissingCacheException;
import de.gematik.test.erezept.ErpConfiguration;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;

public class ActorStage {

  private final ErpConfiguration config;
  private final Map<String, DoctorActor> doctors;
  private final Map<String, PatientActor> patients;
  private final Map<String, PharmacyActor> pharmacies;

  public ActorStage() {
    this.config = ErpConfiguration.create();
    this.doctors = new HashMap<>();
    this.patients = new HashMap<>();
    this.pharmacies = new HashMap<>();
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

  private <T extends ErpActor> T getFromCache(Class<T> klass, Map<String, T> cache, String name) {
    var actor = cache.get(name);
    if (actor == null) {
      actor = instrumentNewActor(klass, name);
    }
    return actor;
  }

  public <T extends ErpActor> T instrumentNewActor(Class<T> klass, String name) {
    val actor = new Instrumented.InstrumentedBuilder<>(klass).withProperties(name);

    // add to cache for later calls
    if (klass.equals(DoctorActor.class)) {
      ActorDecorator.decorateDoctorActor(actor, config);
      doctors.put(name, (DoctorActor) actor);
    } else if (klass.equals(PatientActor.class)) {
      ActorDecorator.decoratePatientActor(actor, config);
      patients.put(name, (PatientActor) actor);
    } else if (klass.equals(PharmacyActor.class)) {
      ActorDecorator.decoratePharmacyActor(actor, config);
      pharmacies.put(name, (PharmacyActor) actor);
    } else {
      throw new MissingCacheException(klass);
    }
    return actor;
  }
}
