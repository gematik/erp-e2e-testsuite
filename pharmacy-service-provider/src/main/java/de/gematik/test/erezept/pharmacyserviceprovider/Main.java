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

import de.gematik.test.erezept.pharmacyserviceprovider.serviceprovider.ServiceProvider;
import de.gematik.test.erezept.pharmacyserviceprovider.serviceprovider.WsServerToUse;
import de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff.GrizzlyWsServerApp;
import java.net.URI;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

@Slf4j
public class Main {

  public static final double VERSIONNUMBER = 1.1;
  // Base URI the Grizzly HTTP server will listen on
  public static final String BASE_URI = "http://0.0.0.0:9095/";

  @SneakyThrows
  public static void main(String[] args) {
    val servers = generatePSPServers();
    servers.startPSPServerContext();
    Thread.currentThread().join(0);
  }

  @SneakyThrows
  public static PSPServerContext generatePSPServers() {
    HttpServer httpServer = createGrizzly();
    WsServerToUse wsServer;
    wsServer = bindGrizzlyWsServer(httpServer);
    return PSPServerContext.initialize(httpServer, wsServer);
  }

  /** bind grizzly WS Server to Grizzzly http Server */
  public static WsServerToUse bindGrizzlyWsServer(HttpServer server) {
    WsServerToUse webSocketServer = new GrizzlyWsServerApp();
    final WebSocketAddOn addon = new WebSocketAddOn();
    for (NetworkListener listener : server.getListeners()) {
      listener.registerAddOn(addon);
      WebSocketEngine.getEngine().register("", "/", (WebSocketApplication) webSocketServer);
    }
    return webSocketServer;
  }

  /** Starts Grizzly HTTP server exposing JAX-RS resources defined in this application. */
  public static HttpServer createGrizzly() {
    // create a resource config that scans for JAX-RS resources and providers
    val rc = ResourceConfig.forApplicationClass(ServiceProvider.class);
    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    val server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
    log.info("Application version: " + VERSIONNUMBER);
    log.info("Press CTRL+C to exit..");
    return server;
  }
}
