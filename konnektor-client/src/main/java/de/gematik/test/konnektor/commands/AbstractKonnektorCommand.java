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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.exceptions.SOAPRequestException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractKonnektorCommand<R> implements KonnektorCommand<R> {

  static {
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
  }

  @FunctionalInterface
  public interface SOAPSupplier<T> {
    T execute()
        throws Exception; // NOSONAR we're gonna catch'em all and rethrow as SOAPRequestException
  }

  @FunctionalInterface
  public interface SOAPAction {
    void execute()
        throws Exception; // NOSONAR we're gonna catch'em all and rethrow as SOAPRequestException
  }

  /**
   * Executes a Supplier which makes a SOAP call and returns a return value
   *
   * @param supplier to be executed
   * @param <T> return Type of the SOAP call
   * @return the value produced by the SOAP call
   * @throws SOAPRequestException in case of any errors
   */
  protected final <T> T executeSupplier(SOAPSupplier<T> supplier) {
    Objects.requireNonNull(supplier);
    try {
      return supplier.execute();
    } catch (Exception e) {
      log.error(format("{0} failed with {1}", this.getClass().getSimpleName(), e.getMessage()));
      throw new SOAPRequestException(this.getClass(), e);
    }
  }

  /**
   * Executes an Action which makes a SOAP call and receives its return values via output-parameters
   *
   * @param action to be executed
   * @throws SOAPRequestException in case of any errors
   */
  protected final void executeAction(SOAPAction action) {
    Objects.requireNonNull(action);
    try {
      action.execute();
    } catch (Exception e) {
      log.error(format("{0} failed with {1}", this.getClass().getSimpleName(), e.getMessage()));
      throw new SOAPRequestException(this.getClass(), e);
    }
  }
}
