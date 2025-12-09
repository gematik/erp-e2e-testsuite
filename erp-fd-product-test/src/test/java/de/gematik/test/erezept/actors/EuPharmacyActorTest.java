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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuPharmacyActorTest {

  @Test
  void shouldProvideCorrectData() {
    StopwatchProvider.init();
    val config = ErpFdTestsuiteFactory.create();
    val pharmacy = new EuPharmacyActor("Hannes Vogt");
    val pharmacyConfig = config.getEuPharmacyConfig(pharmacy.getName());
    val smcb = config.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    pharmacy.can(useSmcb);

    val cfg = new EuPharmacyConfiguration();
    cfg.setCountryCode(IsoCountryCode.NL.getCode());
    pharmacy.can(ProvidePharmacyBaseData.fromConfiguration(cfg));

    assertNotNull(pharmacy.getCommonName());
    assertNotNull(pharmacy.getTelematikId());

    assertDoesNotThrow(pharmacy::getCountryCode);
    assertDoesNotThrow(pharmacy::getTelematikId);
    assertDoesNotThrow(pharmacy::getCommonName);
    assertDoesNotThrow(pharmacy::getDescription);
  }
}
