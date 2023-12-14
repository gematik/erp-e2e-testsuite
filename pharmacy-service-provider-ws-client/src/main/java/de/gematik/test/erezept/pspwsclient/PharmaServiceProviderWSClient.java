/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class PharmaServiceProviderWSClient extends org.java_websocket.client.WebSocketClient
    implements PSPClient {

  static final int WAIT_MILLIS = 1000;
  private static final String SYS_VAR_PROXY_HOST = "https.proxyHost";
  private static final String SYS_VAR_PROXY_PORT = "https.proxyPort";
  @Getter private static final boolean MESSAGE_ARRIVED = false;
  private static final int WAITING_TIME_IN_MILLIS = 2000;
  private final List<PspMessage> pspMessages;
  @Getter private final String id;
  @Getter private boolean isConnected = false;

  @SneakyThrows
  public PharmaServiceProviderWSClient(@NonNull String pspUrl, String id) {
    super(createUri(pspUrl, id));
    this.pspMessages = Collections.synchronizedList(new ArrayList<>());
    this.id = id;
    checkProxySysProp();
    log.info("SubscriptionService Url: {} with id {}", pspUrl, id);
  }

  @SneakyThrows
  public PharmaServiceProviderWSClient(
      @NonNull String pspUrl, String id, @Nullable String authorization) {
    this(pspUrl, id);
    if (authorization != null) {
      this.addHeader("X-Authorization", authorization);
      this.setSocketFactory(SSLContext.getDefault().getSocketFactory());
    }
  }

  private static URI createUri(String pspUrl, String id) {
    if (pspUrl.endsWith("/")) return URI.create(pspUrl + id);
    else return URI.create(pspUrl + "/" + id);
  }

  private void checkProxySysProp() {
    val proxyAddress = System.getProperty(SYS_VAR_PROXY_HOST);
    val proxyPort = Integer.parseInt(System.getProperty(SYS_VAR_PROXY_PORT, "3128"));
    if (proxyAddress != null) {
      log.info("proxy was set to address: " + proxyAddress + ":" + proxyPort);
      this.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort)));
    }
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    log.info("Connection opened {}", serverHandshake.getHttpStatus());
    if (serverHandshake.getContent() != null) {
      log.info(
          " Message: {}",
          new String(Objects.requireNonNull(serverHandshake.getContent()), StandardCharsets.UTF_8));
    }
    this.isConnected = true;
  }

  @Override
  public void onMessage(String message) {
    log.info(
        "Message from Server @ WebsocketTestClient {}: {}",
        this.getSocket().getInetAddress().getHostAddress(),
        message.length());
    if (message.equals("connected to WebSocketServerImpl")) {
      isConnected = true;
      return;
    }
    if (message.startsWith("{")) {
      val pspMessage = deserializePspMessage(message);
      if (pspMessage.isPresent()) {
        pspMessages.add(pspMessage.orElseThrow());
      }
    }
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    log.info("Connection closed; Http Status Code: {}, Message: {}", i, s);
    isConnected = false;
  }

  @Override
  public void onError(Exception e) {
    log.error("Exception: {}", e.fillInStackTrace().getMessage());
  }

  @Override
  @SneakyThrows
  public void close() {
    this.getSocket().close();
  }

  /////////////////////////
  ////// helper ///////////
  /////////////////////////

  private Optional<PspMessage> deserializePspMessage(String message) {
    PspMessage result = null;
    try {
      result = new ObjectMapper().readValue(message, PspMessage.class);
    } catch (JsonProcessingException e) {
      log.error(" ", e);
    }
    return Optional.ofNullable(result);
  }

  @Override
  public boolean hasMessage() {
    return !pspMessages.isEmpty();
  }

  /**
   * consumes the oldest 'unspecific' recipe as RecipeData from a queue sendet by backend
   *
   * @return RecipeData
   */
  @Override
  public Optional<PspMessage> consumeOldest() {
    PspMessage result = null;
    if (!pspMessages.isEmpty()) result = pspMessages.remove(pspMessages.size() - 1);
    return Optional.ofNullable(result);
  }

  /**
   * consumes the oldest 'unspecific' recipe as RecipeData from a queue sendet by backend
   *
   * @return RecipeData
   */
  @Override
  public Optional<PspMessage> consumeOldest(int millisToWait) {
    // save current time plus millisToWait
    final long finishTime = System.currentTimeMillis() + millisToWait;
    var result = consumeOldest();
    while (System.currentTimeMillis() <= finishTime && result.isEmpty()) result = consumeOldest();
    return result;
  }

  @Override
  public boolean clearQueue() {
    pspMessages.clear();
    return true;
  }

  @Override
  public void clearQueueOnServer() {
    send("Clear Queue Serverside");
  }

  @Override
  public int getQueueLength() {
    return pspMessages.size();
  }

  @SneakyThrows
  @Override
  public void callServerStoredMessages() {
    this.send("call stored messages");
    // wait for serverResponse
    Thread.currentThread().join(WAIT_MILLIS);
  }

  @SneakyThrows
  @Override
  public void connectBlocking(int i, TimeUnit timeUnit) {
    super.connectBlocking(i, timeUnit);
    double x = .0;
    // save current time plus WAITING_TIME_IN_MILLIS
    final long finishTime = System.currentTimeMillis() + WAITING_TIME_IN_MILLIS * 5;
    while (!this.isConnected() && System.currentTimeMillis() <= finishTime) {
      log.info(format("wait for isNotConnected()... iterations in {0} sec.", x));
      Thread.currentThread().join(500);
      x = x + 0.5;
    }
  }
}
