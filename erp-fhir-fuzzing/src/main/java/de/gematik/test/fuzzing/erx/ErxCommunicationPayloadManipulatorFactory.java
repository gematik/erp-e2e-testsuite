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

package de.gematik.test.fuzzing.erx;

import static de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef.*;
import static de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem.FLOW_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
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

  public static List<NamedEnvelope<FuzzingMutator<ErxCommunication>>>
      getCommunicationDspRequestSystemsManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxCommunication>>>();
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension URL from PrescType to TelematikId",
            c -> c.getExtension().get(0).setUrl(PRESCRIPTION_TYPE.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension URL from PrescType to old PrescriptionType"
                + " Version",
            c -> c.getExtension().get(0).setUrl(TelematikID.random().getSystemUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to old"
                + " PrescriptionType Version",
            c ->
                c.getExtension().stream()
                    .findFirst()
                    .orElseThrow()
                    .getValue()
                    .castToCoding(c.getExtension().get(0).getValue())
                    .setSystem(PRESCRIPTION_TYPE.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to"
                + " TelematikId",
            c ->
                c.getExtension()
                    .get(0)
                    .getValue()
                    .castToCoding(c.getExtension().get(0).getValue())
                    .setSystem(TelematikID.random().getSystemUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to"
                + " PrescriptionType",
            c ->
                c.getExtension()
                    .get(0)
                    .getValue()
                    .castToCoding(c.getExtension().get(0).getValue())
                    .setSystem(PRESCRIPTION_TYPE_12.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to Older"
                + " FLowTypeVersion",
            c ->
                c.getExtension()
                    .get(0)
                    .getValue()
                    .castToCoding(c.getExtension().get(0).getValue())
                    .setSystem(FLOW_TYPE.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, that changes Recipient-System to PrescriptionType",
            c ->
                c.getRecipient().stream()
                    .findFirst()
                    .orElseThrow()
                    .getIdentifier()
                    .setSystem(PRESCRIPTION_TYPE_12.getCanonicalUrl())));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<ErxCommunication>>>
      getCommunicationReplySystemsManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<ErxCommunication>>>();
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, who set TelematikId-System into recipient.identifier.system",
            c ->
                c.getRecipient().stream()
                    .findFirst()
                    .orElseThrow()
                    .getIdentifier()
                    .setSystem(c.getSender().getIdentifier().getSystem())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, who set payload.extension.url into sender-System",
            c ->
                c.getSender()
                    .getIdentifier()
                    .setSystem(c.getPayloadFirstRep().getExtensionFirstRep().getUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "Systems Manipulator, who set TelematikId into payload.extension.url",
            c ->
                c.getPayloadFirstRep()
                    .getExtensionFirstRep()
                    .setUrl(c.getSender().getIdentifier().getSystem())));

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
