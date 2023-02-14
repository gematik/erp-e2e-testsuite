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

package de.gematik.test.erezept.pharmacyserviceprovider;

import static de.gematik.test.erezept.pharmacyserviceprovider.Main.bindGrizzlyWsServer;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.pharmacyserviceprovider.serviceprovider.ServiceProvider;
import java.net.URI;
import lombok.SneakyThrows;
import lombok.val;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PSPServerContextTest {

  static PSPServerContext pspServerContext;
  static String BASE_URI;
  static HttpServer server;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;

  @BeforeAll
  static void fetchTargetAddress() {
    BASE_URI = "HTTP" + System.getProperty("targetUrl", "://localhost:9095");
  }

  @SneakyThrows
  @BeforeEach
  void setup() {
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
    val rc = ResourceConfig.forApplicationClass(ServiceProvider.class);
    server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
    val webSocketServer = bindGrizzlyWsServer(server);
    pspServerContext = PSPServerContext.initialize(server, webSocketServer);
  }

  @SneakyThrows
  @Test
  void shouldStartPspServerContext() {
    var countWsServerBeforStart = WebSocketServer.WebSocketWorker.activeCount();
    pspServerContext.startPSPServerContext();
    assertTrue(server.isStarted());
    var countWsServerAfterStart = WebSocketServer.WebSocketWorker.activeCount();
    assertNotEquals(countWsServerBeforStart, countWsServerAfterStart);
    pspServerContext.shutDownNow();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
  }

  @SneakyThrows
  @Test
  void shouldStopPspServerContext() {
    var beforeStart = server.isStarted();
    pspServerContext.startPSPServerContext();
    assertTrue(server.isStarted());
    pspServerContext.shutDownNow();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
  }
}
