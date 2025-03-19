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

import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface PSPClient {

  boolean hasMessage();

  /**
   * method to pop the oldest PspMessage
   *
   * @return Optional PSPMessage
   */
  Optional<PspMessage> consumeOldest();

  /**
   * method to pop the oldest PspMessage possible to define a busy waiting until a message received
   *
   * @param millisToWait
   * @return Optional PSPMessage
   */
  Optional<PspMessage> consumeOldest(int millisToWait);

  /**
   * clear local client-messageQueue
   *
   * @return true
   */
  boolean clearQueue();

  /** call connected Server to clear the Messages saved with the own TelematikId */
  void clearQueueOnServer();

  int getQueueLength();

  /** call connected server to transmit all stored messages with the client telematikId */
  void callServerStoredMessages();

  String getId();

  /**
   * generate the connection to server
   *
   * @param i time to block
   * @param timeUnit
   * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied,
   *     and the thread is interrupted.
   */
  void connectBlocking(int i, TimeUnit timeUnit) throws InterruptedException;

  boolean isConnected();

  void close();
}
