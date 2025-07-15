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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.model.ActorContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class InfoResponseBuilderTest extends TestWithActorContext {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(InfoResponseBuilder.class));
  }

  @Test
  @SetSystemProperty(key = ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE, value = "1.4.0")
  void shouldChangeProfileViaSystemProperty() {
    val ctx = ActorContext.getInstance();
    val info = InfoResponseBuilder.getInfo(ctx);
    assertNotNull(info.getFhir());
    assertEquals("1.4.0", info.getFhir().getConfigured());
    assertNotNull(info.getFhir());
  }
}
