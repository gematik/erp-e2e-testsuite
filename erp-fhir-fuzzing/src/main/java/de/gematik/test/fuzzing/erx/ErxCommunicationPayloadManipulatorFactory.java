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

package de.gematik.test.fuzzing.erx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;

public class ErxCommunicationPayloadManipulatorFactory {

  private ErxCommunicationPayloadManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static List<NamedEnvelope<FuzzingMutator<ErxCommunication>>>
      getCommunicationPayloadManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxCommunication>>>();
    manipulators.add(
        NamedEnvelope.of(
            "Version Manipulator, that changes Int -> String",
            c -> {
              val mapper = new ObjectMapper();
              val msg = getMsg(c, mapper);
              val versionContent = msg.get("version");
              ((ObjectNode) msg).set("version", new TextNode(versionContent.asText()));
              writeMsg(mapper, msg, c);
            }));
    return manipulators;
  }

  @SneakyThrows
  private static void writeMsg(ObjectMapper mapper, JsonNode msg, ErxCommunication com) {
    val cont = com.getPayloadFirstRep().getContent();
    val stringCont = cont.castToString(cont);
    stringCont.setValue(mapper.writeValueAsString(msg));
  }

  @SneakyThrows
  private static JsonNode getMsg(ErxCommunication com, ObjectMapper mapper) {
    return mapper.readTree(com.getMessage());
  }
}
