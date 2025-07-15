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

package de.gematik.test.erezept.primsys;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.val;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.Test;

class GrizzlyServerShutdownHookTest {

  @Test
  @SuppressWarnings("unchecked")
  void shouldThrowInterruptedException() throws ExecutionException, InterruptedException {
    val server = mock(HttpServer.class);
    val shutdownHook = new GrizzlyServerShutdownHook(server);
    GrizzlyFuture<HttpServer> future = mock(GrizzlyFuture.class);

    when(server.shutdown(anyLong(), any(TimeUnit.class))).thenReturn(future);
    when(future.get()).thenThrow(new InterruptedException("Interrupted!"));
    shutdownHook.start();
    assertThrows(InterruptedException.class, future::get);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testRun() throws ExecutionException, InterruptedException {
    val server = mock(HttpServer.class);
    val shutdownHook = new GrizzlyServerShutdownHook(server);
    GrizzlyFuture<HttpServer> future = mock(GrizzlyFuture.class);

    when(future.get()).thenReturn(server);
    when(server.shutdown(anyLong(), any(TimeUnit.class))).thenReturn(future);
    shutdownHook.start();
    assertFalse(shutdownHook.isInterrupted());
  }
}
