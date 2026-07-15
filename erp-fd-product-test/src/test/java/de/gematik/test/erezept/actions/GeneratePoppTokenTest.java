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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.abilities.UsePoppTokenGenerator;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeneratePoppTokenTest {

  private Egk egk;
  private UsePoppTokenGenerator ability;
  private Actor actor;

  @BeforeEach
  void setUp() {
    egk = mock(Egk.class);
    ability = mock(UsePoppTokenGenerator.class);

    actor = Actor.named("test-actor");
    actor.can(ability);
  }

  @Test
  void shouldUseValidTokenByDefault() {
    when(ability.sign(egk)).thenReturn("valid-token");

    String result = GeneratePoppToken.forEgk(egk).answeredBy(actor);

    assertEquals("valid-token", result);
    verify(ability).sign(egk);
  }

  @Test
  void shouldGenerateExpiredToken() {
    when(ability.signExpired(egk)).thenReturn("expired-token");

    String result = GeneratePoppToken.forEgk(egk).expired().answeredBy(actor);

    assertEquals("expired-token", result);
    verify(ability).signExpired(egk);
  }

  @Test
  void shouldGenerateWrongKeyToken() {
    when(ability.signWithWrongKey(egk)).thenReturn("wrong-key-token");

    String result = GeneratePoppToken.forEgk(egk).wrongKey().answeredBy(actor);

    assertEquals("wrong-key-token", result);
    verify(ability).signWithWrongKey(egk);
  }

  @Test
  void shouldGenerateInvalidKidToken() {
    when(ability.signWithInvalidKid(egk)).thenReturn("invalid-kid-token");

    String result = GeneratePoppToken.forEgk(egk).invalidKid().answeredBy(actor);

    assertEquals("invalid-kid-token", result);
    verify(ability).signWithInvalidKid(egk);
  }

  @Test
  void shouldGenerateInvalidIssuerToken() {
    when(ability.signWithInvalidIssuer(egk)).thenReturn("invalid-issuer-token");

    String result = GeneratePoppToken.forEgk(egk).invalidIssuer().answeredBy(actor);

    assertEquals("invalid-issuer-token", result);
    verify(ability).signWithInvalidIssuer(egk);
  }
}
