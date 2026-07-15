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

package de.gematik.test.erezept.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.crypto.certificate.ProfessionOid;
import de.gematik.bbriccs.popp.PoppTokenGenerator;
import de.gematik.bbriccs.popp.PoppTokenGenerator.TokenGenerationRequest;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmcB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UsePoppTokenGeneratorTest {

  private PoppTokenGenerator generator;
  private SmcB smcb;
  private Egk egk;

  private UsePoppTokenGenerator ability;

  @BeforeEach
  void setUp() {
    generator = mock(PoppTokenGenerator.class);
    smcb = mock(SmcB.class);
    egk = mock(Egk.class);

    when(smcb.getTelematikId()).thenReturn("test-telematik-id");

    ProfessionOid professionOid = mock(ProfessionOid.class);
    when(professionOid.getValue()).thenReturn("1.2.276.0.76.4.50");
    when(smcb.getProfession()).thenReturn(professionOid);
    when(egk.getKvnr()).thenReturn("X123456789");

    ability = UsePoppTokenGenerator.with(generator, smcb);

    when(generator.sign(any())).thenReturn("signed");
    when(generator.signExpiredToken(any())).thenReturn("expired");
    when(generator.signWithWrongKey(any())).thenReturn("wrong-key");
  }

  @Test
  void shouldSignToken() {
    String result = ability.sign(egk);

    assertEquals("signed", result);

    verify(generator).sign(any(TokenGenerationRequest.class));
  }

  @Test
  void shouldSignExpiredToken() {
    String result = ability.signExpired(egk);

    assertEquals("expired", result);

    verify(generator).signExpiredToken(any(TokenGenerationRequest.class));
  }

  @Test
  void shouldSignWithWrongKey() {
    String result = ability.signWithWrongKey(egk);

    assertEquals("wrong-key", result);

    verify(generator).signWithWrongKey(any(TokenGenerationRequest.class));
  }

  @Test
  void shouldSetInvalidKid() {
    ArgumentCaptor<TokenGenerationRequest> captor =
        ArgumentCaptor.forClass(TokenGenerationRequest.class);

    when(generator.sign(any())).thenReturn("ok");

    ability.signWithInvalidKid(egk);

    verify(generator).sign(captor.capture());

    TokenGenerationRequest req = captor.getValue();
    assertEquals("invalidKid", req.getKid());
  }

  @Test
  void shouldSetInvalidIssuer() {
    ArgumentCaptor<TokenGenerationRequest> captor =
        ArgumentCaptor.forClass(TokenGenerationRequest.class);

    when(generator.sign(any())).thenReturn("ok");

    ability.signWithInvalidIssuer(egk);

    verify(generator).sign(captor.capture());

    TokenGenerationRequest req = captor.getValue();
    assertEquals("https://dummy.de", req.getIssuer());
  }
}
