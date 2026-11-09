/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.primsys;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrimSysRestFactory extends ConfiguredFactory {

  private final PrimsysConfigurationDto dto;
  private final ErpClientFactory erpClientFactory;
  private final SmartcardArchive sca;

  private PrimSysRestFactory(PrimsysConfigurationDto dto, ErpClientFactory erpClientFactory) {
    this.dto = dto;
    this.erpClientFactory = erpClientFactory;
    this.sca = erpClientFactory.getSmartcards();
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public List<Doctor> createDoctorActors() {
    return dto.getActors().getDoctors().stream()
        .map(
            d -> {
              val name = d.getName();
              val builder = erpClientFactory.prepareStagedClientFor(d);
              val smcb = builder.getSmcb();
              val hba = sca.getHbaByICCSN(d.getHbaIccsn());
              val client = builder.createWithSmcBAuthentication();
              val cs = CryptoSystem.fromString(d.getAlgorithm());
              val kon = instantiateKonnektor(d);

              return new Doctor(name, smcb, hba, client, cs, kon);
            })
        .toList();
  }

  public List<Pharmacy> createPharmacyActors() {
    return dto.getActors().getPharmacies().stream()
        .map(
            d -> {
              val name = d.getName();
              val builder = erpClientFactory.prepareStagedClientFor(d);
              val smcb = builder.getSmcb();
              val client = builder.createWithSmcBAuthentication();
              val cs = CryptoSystem.fromString(d.getAlgorithm());
              val kon = instantiateKonnektor(d);

              return new Pharmacy(name, smcb, client, cs, kon);
            })
        .toList();
  }

  public List<HealthInsurance> createHealthInsuranceActors() {
    return dto.getActors().getHealthInsurances().stream()
        .map(
            d -> {
              val name = d.getName();
              val builder = erpClientFactory.prepareStagedClientFor(d);
              val smcb = builder.getSmcb();
              val client = builder.createWithSmcBAuthentication();
              val cs = CryptoSystem.fromString(d.getAlgorithm());
              val kon = instantiateKonnektor(d);

              return new HealthInsurance(name, smcb, client, cs, kon);
            })
        .toList();
  }

  private Konnektor instantiateKonnektor(PsActorConfiguration config) {
    return instantiateKonnektorClient(config.getKonnektor());
  }

  private Konnektor instantiateKonnektorClient(@Nullable String name) {
    val konName = Optional.ofNullable(name).orElse("Soft-Konn");
    return Konnektor.create(getConfig(konName, dto.getKonnektors()));
  }

  public static PrimSysRestFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    val activeEnv = getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
    val erpClientFactory =
        ErpClientFactory.forEnvironment(activeEnv).withSmartcardArchive(sca).build();
    return from(dto, erpClientFactory);
  }

  public static PrimSysRestFactory from(
      PrimsysConfigurationDto dto, ErpClientFactory erpClientFactory) {
    return new PrimSysRestFactory(dto, erpClientFactory);
  }
}
