package de.gematik.test.erezept.primsys;

import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.smartcard.SmartcardArchive;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrimSysRestFactory extends ConfiguredFactory {

  private final PrimsysConfigurationDto dto;
  private final SmartcardArchive sca;

  public PrimSysRestFactory(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    this.dto = dto;
    this.sca = sca;
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return this.getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public List<Doctor> createDoctorActors() {
    val activeEnv = this.getActiveEnvironment();
    return dto.getActors().getDoctors().stream()
        .map(d -> new Doctor(d, activeEnv, instantiateDoctorKonnektor(d), sca))
        .toList();
  }

  public List<Pharmacy> createPharmacyActors() {
    val activeEnv = this.getActiveEnvironment();
    return dto.getActors().getPharmacies().stream()
        .map(d -> new Pharmacy(d, activeEnv, instantiatePharmacyKonnektor(d), sca))
        .toList();
  }

  private Konnektor instantiateDoctorKonnektor(DoctorConfiguration docConfig) {
    return instantiateKonnektorClient(docConfig.getKonnektor());
  }

  private Konnektor instantiatePharmacyKonnektor(PharmacyConfiguration pharmacyConfig) {
    return instantiateKonnektorClient(pharmacyConfig.getKonnektor());
  }

  private Konnektor instantiateKonnektorClient(String name) {
    return KonnektorFactory.createKonnektor(this.getConfig(name, dto.getKonnektors()));
  }

  public static PrimSysRestFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    return new PrimSysRestFactory(dto, sca);
  }
}
