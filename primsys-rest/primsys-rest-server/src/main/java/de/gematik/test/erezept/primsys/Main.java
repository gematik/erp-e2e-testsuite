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

import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.primsys.exceptions.PrimSysErrorPageGenerator;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.smartcard.SmartcardFactory;
import java.net.URI;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/** Main class. */
@Slf4j
public class Main {
  // Base URI the Grizzly HTTP server will listen on
  public static final String BASE_URI = "http://0.0.0.0:9095/";

  /** Starts Grizzly HTTP server exposing JAX-RS resources defined in this application. */
  @SneakyThrows
  public static void startGrizzly() {
    // create a resource config that scans for JAX-RS resources and providers
    val rc = ResourceConfig.forApplicationClass(PrimSysApplication.class);
    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    val server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);

    // prevent Grizzly from leaking internal information to the client
    server.getServerConfiguration().setDefaultErrorPageGenerator(new PrimSysErrorPageGenerator());

    Runtime.getRuntime().addShutdownHook(new GrizzlyServerShutdownHook(server));
    server.start();

    log.info("======================");
    log.info("Press CTRL+C to exit..");
    Thread.currentThread().join();
  }

  /**
   * Main method.
   *
   * @param args CLI arguments
   */
  @SuppressWarnings("java:S4823")
  public static void main(String[] args) {
    val sca = SmartcardFactory.getArchive();
    PrimSysRestFactory factory;

    if (args.length > 0) {
      // if a parameter is given, handle this one as the path to the config file
      val configFile = Path.of(args[0]).toAbsolutePath();
      log.info(format("Initialize REST Service with Config from {0}", configFile));
      factory =
          ConfigurationReader.forPrimSysConfiguration()
              .configFile(configFile)
              .wrappedBy(dto -> PrimSysRestFactory.fromDto(dto, sca));
    } else {
      log.info("Initialze REST Service with default Config");
      factory =
          ConfigurationReader.forPrimSysConfiguration()
              .wrappedBy(dto -> PrimSysRestFactory.fromDto(dto, sca));
    }

    log.info("======== PrimSys REST ========");
    ActorContext.init(factory);
    startGrizzly();
  }
}
