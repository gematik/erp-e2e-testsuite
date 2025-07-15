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

package de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff;

import static java.lang.String.format;

import de.gematik.test.erezept.pharmacyserviceprovider.serviceprovider.WsServerToUse;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import de.gematik.test.erezept.pspwsclient.dataobjects.WsMessageHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.SimpleWebSocket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

@Slf4j
public class GrizzlyWsServerApp extends WebSocketApplication implements WsServerToUse {

  private final PrescriptionMessageQueue prescriptionMessageQueue;
  private final ConcurrentMap<String, WebSocket> socketsMap = new ConcurrentHashMap<>();
  private final WsMessageHandler wsMessageHandler;

  public GrizzlyWsServerApp() {
    this.prescriptionMessageQueue = new PrescriptionMessageQueue();
    this.wsMessageHandler = new WsMessageHandler();
  }

  @Override
  public WebSocket createSocket(
      ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
    val idOfClient = requestPacket.getRequestURI().substring(1);
    val websocket = new SimpleWebSocket(handler, listeners);
    log.info("createSocket. has new Socket with id: " + idOfClient);
    socketsMap.put(idOfClient, websocket);
    return websocket;
  }

  @Override
  public void onClose(WebSocket socket, DataFrame frame) {
    this.remove(socket);
    socket.close();
    socketsMap.entrySet().stream()
        .filter(entrySet -> entrySet.getValue().equals(socket))
        .map(Map.Entry::getKey)
        .forEach(socketsMap::remove);
    log.info("onClose");
  }

  @Override
  public void onConnect(WebSocket socket) {
    log.info(" @ GrizzlyWsServerApp.onConnect()");
    this.add(socket);
    socket.send("connected to WebSocketServerImpl");
  }

  @Override
  public void onMessage(WebSocket webSocket, String s) {
    log.info(format("@ GrizzlyWsServerApp.onMessage() webSocket: %s, String: %s%n", webSocket, s));
    val key = getkeyOf(webSocket);
    if (s.equals("call stored messages")) {
      log.info(" call + messageQueueFetched(webSocket) with key: {}", key);
      this.messageQueueFetched(webSocket, key);
    }
    if (s.equals("Clear Queue Serverside")) {
      log.info("Socket {} called to clear Queue", key);
      this.prescriptionMessageQueue.removePrescriptions(key);
    }
  }

  private String getkeyOf(WebSocket webSocket) {
    return socketsMap.entrySet().stream()
        .filter(entrySet -> entrySet.getValue().equals(webSocket))
        .findFirst()
        .orElseThrow()
        .getKey();
  }

  @Override
  public void onMessage(WebSocket socket, byte[] bytes) {
    log.info("no onMessage(WebSocket socket, byte[] bytes) implemented");
  }

  @Override
  public boolean hasSpecificWsConnected(String wsId) {
    return this.socketsMap.containsKey(wsId);
  }

  /**
   * @param receiver specified connected WsSocket
   * @param message Message to connected clients
   */
  public void send(String receiver, String message) {
    var socket = this.socketsMap.get(receiver);
    if (socket != null) socket.send(message);
    else log.info("no receiver ");
  }

  public void send(String telemId, PspMessage pspMessage) {
    log.info(
        "@Send teleId: {}, pspMessage.getBlob().lenght: {}", telemId, pspMessage.getBlob().length);
    if (pspMessage.getBlob().length < 1) {
      throw new WebApplicationException(
          "No payload has arrived", Response.status(404).entity("blob == null or empty").build());
    }
    if (telemId == null) {
      throw new WebApplicationException(
          "no fitted receiver connected cause NO  Telematik Id received",
          Response.status(420)
              .entity("no fitted receiver connected cause NO Telematik Id")
              .build());

    } else {
      generateJSONResponse(pspMessage);
    }
  }

  private void generateJSONResponse(PspMessage pspMessage) {

    pspMessage.setNote(format("arrived @ %s", pspMessage.getDeliveryOption()));

    if (!hasSpecificWsConnected(pspMessage.getClientId())) {
      log.info(
          "!getInstance().connectionIsActive() not connected!"
              + " getPspMessageMessageQueueList().add(pspMessage) for "
              + pspMessage.getClientId());
      this.prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage);
      throw new WebApplicationException(
          format(
              "no fitted receiver connected @ specific TelematikId: %s", pspMessage.getClientId()),
          Response.status(200)
              .entity(
                  format(
                      "no fitted receiver connected @ specific TelematikId: %s",
                      pspMessage.getClientId()))
              .build());
    } else {
      this.send(pspMessage.getClientId(), wsMessageHandler.encodeToJson(pspMessage));
    }
  }

  private void sendMessageQue(WebSocket ws, List<PspMessage> prescriptionsForSpecificID) {
    for (PspMessage pspMessage : prescriptionsForSpecificID) {
      ws.send(wsMessageHandler.encodeToJson(pspMessage));
    }
  }

  private void messageQueueFetched(WebSocket webSocket, String socketId) {
    List<PspMessage> prescriptionsForSpecificID =
        prescriptionMessageQueue.popPrescriptionWith(socketId);
    log.info("prescriptionsForSpecificID.isEmpty()" + prescriptionsForSpecificID.isEmpty());
    if (prescriptionsForSpecificID.isEmpty()) {
      log.info(
          format(
              "prescriptionsForSpecificID:  %s  .isEmpty(): %s and long: %s",
              socketId, true, prescriptionsForSpecificID.size()));
      if (prescriptionMessageQueue.hasSavedMessages()) {
        this.sendMessageQue(webSocket, prescriptionsForSpecificID);
      }
    }
    this.sendMessageQue(webSocket, prescriptionsForSpecificID);
  }
}
