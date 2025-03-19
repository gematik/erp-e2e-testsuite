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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.UseHapiFuzzer;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public abstract class ErpAction<R extends Resource> implements Question<ErpInteraction<R>> {

  private static final ObjectWriter writer =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .writerWithDefaultPrettyPrinter();

  protected final ErpInteraction<R> performCommandAs(ICommand<R> cmd, Actor actor) {
    applyFuzzer(cmd, actor);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val response = erpClient.request(cmd);
    return new ErpInteraction<>(response);
  }

  /**
   * @deprecated only for PoC purposes, won't be required once a fuzzing ErpClient-Decorator is
   *     implemented
   * @param actor
   * @param resource
   * @param encoding
   * @return
   */
  @Deprecated
  protected final String encode(Actor actor, Resource resource, EncodingType encoding) {
    applyFuzzer(resource, actor);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    return erpClient.encode(resource, encoding);
  }

  /**
   * Transparent approach to apply a fuzzer to a command: By that we don't need to bother about
   * fuzzing on each Task/Question separately but can apply it here for every request.
   *
   * <p>TODO: However, a better approach would be to have a separate class (or Decorator) for the
   * ErpClient
   *
   * @param cmd to be fuzzed before executing against the FD
   * @param actor who is performing the request
   */
  private void applyFuzzer(ICommand<R> cmd, Actor actor) {
    cmd.getRequestBody().ifPresent(body -> applyFuzzer(body, actor));
  }

  private void applyFuzzer(Resource resource, Actor actor) {
    Optional.ofNullable(actor.usingAbilityTo(UseHapiFuzzer.class))
        .ifPresent(
            fuzzingAbility -> {
              val fuzzLog = fuzzingAbility.fuzz(resource);
              try {
                val content = writer.writeValueAsString(fuzzLog);
                Serenity.recordReportData()
                    .withTitle(format("Fuzzing of {0}", resource.getClass().getSimpleName()))
                    .andContents(content);
              } catch (JsonProcessingException e) {
                log.warn("unable to serialize fuzzing log", e);
              }
            });
  }
}
