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

package de.gematik.test.erezept.primsys;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.primsys.model.ActorContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;

/**
 * attributes to
 * https://stackoverflow.com/questions/44572276/what-is-the-proper-way-to-gracefully-shutdown-a-grizzly-server-embedded-with-j
 */
@Slf4j
public class GrizzlyServerShutdownHook extends Thread {

  public static final String THREAD_NAME = "Grizzly Server Shutdown Hook";

  public static final int GRACE_PERIOD = 60;
  public static final TimeUnit GRACE_PERIOD_TIME_UNIT = TimeUnit.SECONDS;

  private final HttpServer server;

  /**
   * @param server The server to shut down
   */
  public GrizzlyServerShutdownHook(HttpServer server) {
    this.server = server;
    setName(THREAD_NAME);
  }

  @Override
  public void run() {
    log.info("Running Grizzly Server Shutdown Hook.");
    log.info("Shutting down server.");
    GrizzlyFuture<HttpServer> future = server.shutdown(GRACE_PERIOD, GRACE_PERIOD_TIME_UNIT);

    try {
      log.info(
          format(
              "Waiting for server to shut down... Grace period is {0} {1}",
              GRACE_PERIOD, GRACE_PERIOD_TIME_UNIT));
      future.get();

      // this will remove cryptographic files in temp directory
      ActorContext.getInstance().shutdown();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error while shutting down server.", e);
      Thread.currentThread().interrupt();
    }

    log.info("Server stopped.");
  }
}
