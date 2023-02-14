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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DecideUserBehaviourTest {

  @Test
  void shouldPreferManual() {
    val ability = DecideUserBehaviour.manual();
    assertTrue(ability.doesPreferManualSteps());
  }

  @Test
  void shouldPreferAutomated() {
    val ability = DecideUserBehaviour.automated();
    assertFalse(ability.doesPreferManualSteps());
  }

  @Test
  void shouldPreferFromConfig() {
    List.of(true, false)
        .forEach(
            choice -> {
              val config = new TestsuiteConfiguration();
              config.setPreferManualSteps(choice);
              val ability = DecideUserBehaviour.fromConfiguration(config);
              assertEquals(choice, ability.doesPreferManualSteps());
            });
  }

  @Test
  void shouldToStringCorrectly() {
    List.of(true, false)
        .forEach(
            choice -> {
              val config = new TestsuiteConfiguration();
              config.setPreferManualSteps(choice);
              val ability = DecideUserBehaviour.fromConfiguration(config);
              if (choice) assertTrue(ability.toString().contains("manuell"));
              else assertTrue(ability.toString().contains("automatisiert"));
            });
  }
}
