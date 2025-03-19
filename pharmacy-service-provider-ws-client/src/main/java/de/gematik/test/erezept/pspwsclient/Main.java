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
 */

package de.gematik.test.erezept.pspwsclient;

import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  static PharmaServiceProviderWSClient clientEndPoint;
  static final String URL_TO_GRIZZLY_WS = "ws://localhost:9095/";
  static final String APO_ID = "123";

  /**
   * main starts wsClient
   *
   * <ol>
   *   <li>arg[0] = url
   *   <li>arg[1] = ApoId
   *   <li>arg[2] = authorisation
   * </ol>
   *
   * @param args
   */
  @SneakyThrows
  public static void main(String[] args) {
    String authorisation = null;
    String url = URL_TO_GRIZZLY_WS;
    String id = APO_ID;
    if (args.length > 0) url = args[0];
    if (args.length > 1) id = args[1];
    if (args.length > 2) authorisation = args[2];
    // generate client
    clientEndPoint = new PharmaServiceProviderWSClient(url, id, authorisation);
    // connect to client
    clientEndPoint.connectBlocking(10, TimeUnit.SECONDS);
    Thread.currentThread().join(500);
    log.info("clientEndPoint.isConnected() ? : " + clientEndPoint.isConnected());
    // keep client alive
    Thread.currentThread().join(0);
  }
}
