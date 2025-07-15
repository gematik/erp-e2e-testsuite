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

package de.gematik.test.erezept.pharmacyserviceprovider;

import de.gematik.test.erezept.pharmacyserviceprovider.helper.EmptyServerContextException;
import de.gematik.test.erezept.pharmacyserviceprovider.serviceprovider.WsServerToUse;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.server.HttpServer;

@Slf4j
public class PSPServerContext {

  private static final int WAITING_TIME_IN_MILLIS = 10000;
  private static PSPServerContext pspServerContext;

  private final HttpServer httpServer;
  @Getter private final WsServerToUse wsServerToUse;

  public static PSPServerContext getInstance() {
    if (pspServerContext == null) throw new EmptyServerContextException();
    return pspServerContext;
  }

  public static PSPServerContext initialize(HttpServer httpServer, WsServerToUse webSocketServer) {
    pspServerContext = new PSPServerContext(httpServer, webSocketServer);
    return pspServerContext;
  }

  private PSPServerContext(HttpServer httpServer, WsServerToUse webSocketServer) {
    this.wsServerToUse = webSocketServer;
    this.httpServer = httpServer;
  }

  @SneakyThrows
  public void startPSPServerContext() {
    httpServer.start(); // WsServerStart cause of binding automatically
    final long finishTime = System.currentTimeMillis() + WAITING_TIME_IN_MILLIS;
    while (!httpServer.isStarted() && System.currentTimeMillis() <= finishTime) {
      log.debug("waiting for Server starts ..");
    }
  }

  public boolean isStarted() {
    return httpServer.isStarted();
  }

  @SneakyThrows
  public void shutDownNow() {
    httpServer.shutdownNow();
    final long finishTime = System.currentTimeMillis() + WAITING_TIME_IN_MILLIS;
    while (httpServer.isStarted() && System.currentTimeMillis() <= finishTime) {
      log.debug("waiting for Servers shutting down ...");
    }
  }
}
