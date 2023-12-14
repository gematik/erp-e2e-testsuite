/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.jupiter.api.Assertions.*;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class InfoResponseBuilderTest extends TestWithActorContext{
  
  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(InfoResponseBuilder.class));
  }

  @Test
  void shouldGenerateDefaultInfoResource() {
    val ctx = ActorContext.getInstance();
    val info = InfoResponseBuilder.getInfo(ctx);
    assertTrue(info.getDoctors() > 0);
    assertTrue(info.getPharmacies() > 0);
    assertNotNull(info.getBuild());
    assertNotNull(info.getFhir());
    assertNotNull(info.getFhir());
    assertEquals("default", info.getFhir().getConfigured());
  }


  @Test
  @SetSystemProperty(key = "erp.fhir.profile", value = "1.2.0")
  void shouldChangeProfileViaSystemProperty() {
    val ctx = ActorContext.getInstance();
    val info = InfoResponseBuilder.getInfo(ctx);
    assertNotNull(info.getFhir());
    assertEquals("1.2.0", info.getFhir().getConfigured());
    assertNotNull(info.getFhir());

  }

  @Test
  @SetSystemProperty(key = ParserConfigurations.SYS_PROP_TOGGLE, value = "")
  void shouldGenerateDefaultInfoResponseWhenEmptyVersion(){
    val ctx = ActorContext.getInstance();
    val info = InfoResponseBuilder.getInfo(ctx);
    assertNotNull(info.getFhir());
    assertEquals("default", info.getFhir().getConfigured());
  }
  @Test
  @SetSystemProperty(key = ParserConfigurations.SYS_PROP_TOGGLE, value = " ")
  void shouldGenerateDefaultInfoResponseWhenBlankVersion(){
    val ctx = ActorContext.getInstance();
    val info = InfoResponseBuilder.getInfo(ctx);
    assertNotNull(info.getFhir());
    assertEquals("default", info.getFhir().getConfigured());
  }







}
