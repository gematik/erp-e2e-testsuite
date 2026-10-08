/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ErpInteractionPoller {

  private final int maxRetries;
  private final int waitTimeInMilliSeconds;

  public <R extends Resource> ErpInteraction<R> poll(Supplier<ErpInteraction<R>> supplier) {
    return poll(supplier, r -> true);
  }

  public <R extends Resource> ErpInteraction<R> poll(
      Supplier<ErpInteraction<R>> supplier, Predicate<R> contentCheck) {

    int retries = 0;
    ErpInteraction<R> interaction = null;
    do {
      interaction = supplier.get();

      if (interaction.isOfExpectedType()) {
        R resource = interaction.getExpectedResponse();

        if (resource != null && contentCheck.test(resource)) {
          return interaction;
        }
      }
      retries++;

      try {
        Thread.sleep(waitTimeInMilliSeconds);
        log.info(
            format(
                "Retrying polling... Attempt {0}/{1} and slept for {2}ms",
                retries, maxRetries, retries * waitTimeInMilliSeconds));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Polling interrupted", e);
      }

    } while (retries < maxRetries);

    return interaction;
  }

  /**
   * Method returns a builder for creating an instance of ErpInteractionPoller with default setting
   * for maxRetries = 5 and waitTimeInMilliSeconds = 500. it is possible to override these default
   * values by calling the respective methods on the builder.
   *
   * <p>builder has to be closed by calling build() to get the polling service
   *
   * @return PollBuilder instance
   */
  public static PollBuilder builder() {
    return new PollBuilder();
  }

  public static class PollBuilder {
    private int maxRetries = 5;
    private int waitTimeInMilliSeconds = 500;

    public PollBuilder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public PollBuilder waitTimeInMilliSeconds(int waitTimeInMilliSeconds) {
      this.waitTimeInMilliSeconds = waitTimeInMilliSeconds;
      return this;
    }

    public ErpInteractionPoller build() {
      return new ErpInteractionPoller(maxRetries, waitTimeInMilliSeconds);
    }
  }
}
