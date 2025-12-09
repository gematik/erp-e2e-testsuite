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

package de.gematik.test.erezept.abilities;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.Optional;
import lombok.*;
import lombok.experimental.Accessors;
import net.serenitybdd.screenplay.Ability;
import org.hl7.fhir.r4.model.Identifier;

public class ProvidePharmacyBaseData implements Ability {

  @Getter private final IsoCountryCode countryCode;
  @Getter private final Identifier organizationIdentifier;
  @Getter private final Identifier practitionerIdentifier;

  private ProvidePharmacyBaseData(ProvidePharmacyBaseDataBuilder builder) {
    this.countryCode = builder.countryCode;
    this.organizationIdentifier = builder.organizationIdentifier;
    this.practitionerIdentifier = builder.practitionerIdentifier;
  }

  public static ProvidePharmacyBaseDataBuilder forNationalPharmacy() {
    return new ProvidePharmacyBaseDataBuilder(IsoCountryCode.DE);
  }

  public static ProvidePharmacyBaseData fromConfiguration(EuPharmacyConfiguration cfg) {
    return new ProvidePharmacyBaseDataBuilder(IsoCountryCode.fromCode(cfg.getCountryCode()))
        .practitionerIdentifier(cfg.getPharmacistIdentifier())
        .organizationIdentifier(cfg.getOrganizationIdentifier())
        .build();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Accessors(fluent = true)
  @Setter
  public static class ProvidePharmacyBaseDataBuilder {
    private final IsoCountryCode countryCode;
    private Identifier organizationIdentifier;
    private Identifier practitionerIdentifier;

    public ProvidePharmacyBaseDataBuilder practitionerIdentifier(String identifier) {
      val value = Optional.ofNullable(identifier).orElse("Eu-Pharmacist");
      this.practitionerIdentifier =
          new Identifier().setValue(value).setSystem("https://unknown-european-pharmacist-system");
      return this;
    }

    public ProvidePharmacyBaseDataBuilder organizationIdentifier(String identifier) {
      val value = Optional.ofNullable(identifier).orElse("Eu-Pharmacia");
      this.organizationIdentifier =
          new Identifier().setValue(value).setSystem("https://unknown-european-pharmacia-system");
      return this;
    }

    /**
     * national pharmacy pretending to be an EU-Pharmacy can directly use the SMC-B
     *
     * @param smcB of the national pharmacy
     * @return
     */
    public ProvidePharmacyBaseDataBuilder organizationIdentifier(SmcB smcB) {
      this.organizationIdentifier = TelematikID.from(smcB.getTelematikId()).asIdentifier();
      return this;
    }

    public ProvidePharmacyBaseData build() {
      return new ProvidePharmacyBaseData(this);
    }
  }
}
