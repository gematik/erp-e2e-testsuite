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

package de.gematik.test.erezept.client.idpclient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.idp.client.IIdpClient;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.model.PkiIdentity;
import java.util.function.UnaryOperator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class InvalidIdpClientTest {

  @Mock private IIdpClient delegate;
  @Mock private PkiIdentity idpIdentity;
  @Mock private IdpTokenResult validToken;
  @Mock private IdpTokenResult manipulatedToken;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldDelegateInitialize() {
    val client = new InvalidIdpClient(delegate, UnaryOperator.identity());

    val result = client.initialize();

    verify(delegate).initialize();
    assertSame(client, result);
  }

  @Test
  void shouldApplyTokenManipulatorOnLogin() {
    when(delegate.login(idpIdentity)).thenReturn(validToken);
    UnaryOperator<IdpTokenResult> manipulator = token -> manipulatedToken;
    val client = new InvalidIdpClient(delegate, manipulator);

    val result = client.login(idpIdentity);

    verify(delegate).login(idpIdentity);
    assertSame(manipulatedToken, result);
  }

  @Test
  void shouldReturnValidTokenUnchangedWhenManipulatorIsNull() {
    when(delegate.login(idpIdentity)).thenReturn(validToken);
    val client = new InvalidIdpClient(delegate, null);

    val result = client.login(idpIdentity);
    assertSame(validToken, result);
  }
}
