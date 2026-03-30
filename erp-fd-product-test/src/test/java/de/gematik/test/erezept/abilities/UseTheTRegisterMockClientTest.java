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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockClient;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseTheTRegisterMockClientTest extends ErpFhirParsingTest {

  private TRegisterMockClient tRegisterMockClient;
  private UseTheTRegisterMockClient useTheTRegisterMockClient;

  @BeforeEach
  void setUp() {
    tRegisterMockClient = mock(TRegisterMockClient.class);
    useTheTRegisterMockClient = useTheTRegisterMockClient.with(tRegisterMockClient, parser);
  }

  @Test
  void shouldDownloadRequestViaTRegisterMockClient() {
    TRegisterMockDownloadRequest request = mock(TRegisterMockDownloadRequest.class);
    List<TRegisterLog> expectedLogs = List.of(mock(TRegisterLog.class));

    when(tRegisterMockClient.downloadRequest(request)).thenReturn(expectedLogs);

    List<TRegisterLog> result = useTheTRegisterMockClient.downloadRequest(request);

    assertEquals(expectedLogs, result);
    verify(tRegisterMockClient).downloadRequest(request);
    verifyNoMoreInteractions(tRegisterMockClient);
  }

  @Test
  void shouldCreateAbilityWithHttpBClient() {
    HttpBClient restClient = mock(HttpBClient.class);

    UseTheTRegisterMockClient ability = useTheTRegisterMockClient.with(restClient, parser);

    assertEquals(UseTheTRegisterMockClient.class, ability.getClass());
  }

  @Test
  void shouldReturnEmptyListWhenClientReturnsEmpty() {
    TRegisterMockDownloadRequest request = mock(TRegisterMockDownloadRequest.class);
    when(tRegisterMockClient.downloadRequest(request)).thenReturn(List.of());

    List<TRegisterLog> result = useTheTRegisterMockClient.downloadRequest(request);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tRegisterMockClient).downloadRequest(request);
    verifyNoMoreInteractions(tRegisterMockClient);
  }
}
