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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.Test;

class PrimSysRestFactoryTest extends TestWithActorContext {

  @Test
  void shouldReadDefaultEnvironment() {
    val activeEnv = assertDoesNotThrow(primsysFactory::getActiveEnvironment);
    assertEquals(primsysConfig.getActiveEnvironment(), activeEnv.getName());
  }

  @Test
  void shouldCreateDoctors() {
    val docs = assertDoesNotThrow(primsysFactory::createDoctorActors);
    assertEquals(primsysConfig.getActors().getDoctors().size(), docs.size());

    val pharmacies = assertDoesNotThrow(primsysFactory::createPharmacyActors);
    assertEquals(primsysConfig.getActors().getPharmacies().size(), pharmacies.size());

    val ktrs = assertDoesNotThrow(primsysFactory::createHealthInsuranceActors);
    assertEquals(primsysConfig.getActors().getHealthInsurances().size(), ktrs.size());
  }
}
