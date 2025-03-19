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
 */

package de.gematik.test.erezept.primsys;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PrimSysRestFactory extends ConfiguredFactory {

  private final PrimsysConfigurationDto dto;
  private final SmartcardArchive sca;

  public EnvironmentConfiguration getActiveEnvironment() {
    return this.getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public List<Doctor> createDoctorActors() {
    val activeEnv = this.getActiveEnvironment();
    return dto.getActors().getDoctors().stream()
        .map(d -> new Doctor(d, activeEnv, instantiateKonnektor(d), sca))
        .toList();
  }

  public List<Pharmacy> createPharmacyActors() {
    val activeEnv = this.getActiveEnvironment();
    return dto.getActors().getPharmacies().stream()
        .map(d -> new Pharmacy(d, activeEnv, instantiateKonnektor(d), sca))
        .toList();
  }

  public List<HealthInsurance> createHealthInsuranceActors() {
    val activeEnv = this.getActiveEnvironment();
    return dto.getActors().getHealthInsurances().stream()
        .map(d -> new HealthInsurance(d, activeEnv, instantiateKonnektor(d), sca))
        .toList();
  }

  private Konnektor instantiateKonnektor(PsActorConfiguration config) {
    return instantiateKonnektorClient(config.getKonnektor());
  }

  private Konnektor instantiateKonnektorClient(@Nullable String name) {
    val konName = Optional.ofNullable(name).orElse("Soft-Konn");
    return KonnektorFactory.createKonnektor(this.getConfig(konName, dto.getKonnektors()));
  }

  public static PrimSysRestFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    return new PrimSysRestFactory(dto, sca);
  }
}
