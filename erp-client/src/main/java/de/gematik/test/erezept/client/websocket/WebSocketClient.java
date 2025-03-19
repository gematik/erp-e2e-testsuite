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

package de.gematik.test.erezept.client.websocket;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

  private String subscriptionId;
  private CountDownLatch countDownLatch;
  @Getter private boolean isBound;
  private boolean hasNewPing = false;
  @Getter private int countPings = 0;

  @SneakyThrows
  public WebSocketClient(@NonNull String subscriptionServiceUrl, @NonNull String authorization) {
    super(URI.create(subscriptionServiceUrl));
    this.addHeader("Authorization", authorization);
    this.setSocketFactory(SSLContext.getDefault().getSocketFactory());

    log.info("SubscriptionService Url: {}", subscriptionServiceUrl);
    log.info("Header Authorization: {}", authorization);
  }

  @Override
  public void send(String text) {
    super.send(text);
    log.info("Send Message: {}", text);
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    log.info("Connection opened {}", serverHandshake.getHttpStatus());
    if (serverHandshake.getContent() != null) {
      log.info(
          "Message: {}",
          new String(Objects.requireNonNull(serverHandshake.getContent()), StandardCharsets.UTF_8));
    }
  }

  @Override
  public void onMessage(String message) {
    checkBound(message);
    checkPing(message);
    log.info(
        "Message from Server {}: {}", this.getSocket().getInetAddress().getHostAddress(), message);
  }

  private void checkPing(String message) {
    if (message.startsWith("ping") && message.endsWith(subscriptionId)) {
      this.countPings++;
      this.hasNewPing = true;
    }
  }

  private void checkBound(String message) {
    if (message.startsWith("bound: ") && message.endsWith(subscriptionId)) {
      this.isBound = true;
      countDownLatch.countDown();
    }
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    log.info("Connection closed; Http Status Code: {}, Message: {}", i, s);
    if (countDownLatch != null && countDownLatch.getCount() > 0) {
      countDownLatch.countDown();
    }
  }

  @Override
  public void onError(Exception e) {
    log.error("Exception: {}", e.fillInStackTrace().getMessage());
  }

  /**
   * bind can block the calling thread until server sent the bound message.
   *
   * @param subscriptionId which is to be bound
   * @return @see CountDownLatch
   */
  public CountDownLatch bind(@NonNull String subscriptionId) {
    if (isOpen()) {
      this.subscriptionId = subscriptionId;
      this.countDownLatch = new CountDownLatch(1);

      this.send("bind: " + subscriptionId);
      return countDownLatch;
    }
    throw new WebsocketNotConnectedException();
  }

  public boolean hasNewPing() {
    if (hasNewPing) {
      hasNewPing = false;
      return true;
    }
    return false;
  }

  @Override
  protected void onSetSSLParameters(SSLParameters sslParameters) {}
}
