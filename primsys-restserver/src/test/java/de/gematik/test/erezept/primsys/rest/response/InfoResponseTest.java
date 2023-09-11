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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class InfoResponseTest {

  private static ActorContext ctx;

  @BeforeAll
  static void init() {
    ctx = mock(ActorContext.class);
    when(ctx.getDoctors()).thenReturn(List.of(mock(Doctor.class), mock(Doctor.class)));
    when(ctx.getPharmacies()).thenReturn(List.of(mock(Pharmacy.class)));
    
    val cfg = TestsuiteConfiguration.getInstance();
    val env = cfg.getActiveEnvironment();
    when(ctx.getEnvironment()).thenReturn(env);
    when(ctx.getConfiguration()).thenReturn(cfg);
  }

  @Test
  void shouldGenerateDefaultInfoResource() {
    val info = InfoResponse.getInfo(ctx);
    assertEquals(2, info.getDoctors());
    assertEquals(1, info.getPharmacies());
    assertNotNull(info.getBuild());
    assertNotNull(info.getFhir());
    assertFalse(info.getFhir().isEmpty());
    assertEquals("default", info.getFhir().get("configured"));
  }

  @Test
  @SetSystemProperty(key = "erp.fhir.profile", value = "1.2.0")
  void shouldChangeProfileViaSystemProperty() {
    val info = InfoResponse.getInfo(ctx);
    assertNotNull(info.getFhir());
    assertEquals("1.2.0", info.getFhir().get("configured"));
    assertFalse(info.getFhir().isEmpty());
  }
}
