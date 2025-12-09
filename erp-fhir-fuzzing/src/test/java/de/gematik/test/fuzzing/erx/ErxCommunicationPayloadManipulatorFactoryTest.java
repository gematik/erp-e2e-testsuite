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

import static de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef.PRESCRIPTION_TYPE_12;
import static de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef.SUPPLY_OPTIONS_TYPE;
import static de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem.FLOW_TYPE_12;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErxCommunicationPayloadManipulatorFactoryTest extends ErpFhirBuildingTest {

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<ErxCommunicationPayloadManipulatorFactory> constructor =
        ErxCommunicationPayloadManipulatorFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @Test
  void shouldManipulateCommunicationReplyPayload() {
    val manipulator =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators();

    val mapper = new ObjectMapper().configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);
    manipulator.forEach(
        m -> {
          val cRM =
              new CommunicationReplyMessage(
                  1,
                  SupplyOptionsType.createDefault().getLabel(),
                  GemFaker.fakerCommunicationInfoReqMessage(),
                  "http://www.asdasd.de",
                  "12345678",
                  "123ewrßiokx-mclöv90ß-2331");

          val comRepl =
              ErxCommunicationBuilder.asReply(cRM).basedOn("test", "Test2").receiver("!").build();
          m.getParameter().accept(comRepl);
          val cont = comRepl.getPayloadFirstRep().getContent();
          val stringCont = cont.castToString(cont).getValue();
          assertThrows(
              JacksonException.class,
              () -> mapper.readValue(stringCont, CommunicationReplyMessage.class));
        });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, that changes Extension URL from PrescType to TelematikId",
        "Systems Manipulator, that changes Extension URL from PrescType to old PrescriptionType"
            + " Version",
      })
  void shouldManipulateExtensionUrl(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationDspRequestSystemsManipulators();
    val cRM =
        new CommunicationDisReqMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            List.of("http://www.asdasd.de"),
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val dispRequest =
        ErxCommunicationBuilder.forDispenseRequest(cRM)
            .flowType(PrescriptionFlowType.FLOW_TYPE_200)
            .basedOn(TaskId.random().getValue(), "Test2")
            .receiver("!")
            .build();
    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for wrong FlowType should exist");
    manipulator.getParameter().accept(dispRequest);
    assertNotEquals(
        PRESCRIPTION_TYPE_12.getCanonicalUrl(),
        dispRequest.getExtension().stream().findFirst().orElseThrow().getUrl(),
        "No Fitting ExtensionUrl contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to"
            + " TelematikId",
        "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to"
            + " PrescriptionType",
        "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to Older"
            + " FLowTypeVersion",
        "Systems Manipulator, that changes Extension.valueCoding.system from FlowType to old"
            + " PrescriptionType Version"
      })
  void shouldManipulateExtensionValueCodingSystem(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationDspRequestSystemsManipulators();
    val cRM =
        new CommunicationDisReqMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            List.of("http://www.asdasd.de"),
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val dispRequest =
        ErxCommunicationBuilder.forDispenseRequest(cRM)
            .flowType(PrescriptionFlowType.FLOW_TYPE_200)
            .basedOn(TaskId.random().getValue(), "Test2")
            .receiver("!")
            .build();
    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for wrong FlowType should exist");
    manipulator.getParameter().accept(dispRequest);
    assertNotEquals(
        FLOW_TYPE_12.getCanonicalUrl(),
        dispRequest.getExtension().stream()
            .findFirst()
            .orElseThrow()
            .getValue()
            .castToCoding(dispRequest.getExtension().get(0).getValue())
            .getSystem(),
        "No Fitting Extension.valueCoding.system contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, that changes Recipient-System to PrescriptionType",
      })
  void shouldManipulateRecipientSystemInDispRequest(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationDspRequestSystemsManipulators();
    val cRM =
        new CommunicationDisReqMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            List.of("http://www.asdasd.de"),
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val dispRequest =
        ErxCommunicationBuilder.forDispenseRequest(cRM)
            .flowType(PrescriptionFlowType.FLOW_TYPE_200)
            .basedOn(TaskId.random().getValue(), "Test2")
            .receiver("!")
            .build();
    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for wrong FlowType should exist");
    manipulator.getParameter().accept(dispRequest);
    assertNotEquals(
        TelematikID.random().getSystem(),
        dispRequest.getRecipientFirstRep().getIdentifier().getSystem(),
        "No Fitting Extension.valueCoding.system contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, who set TelematikId-System into recipient.identifier.system",
      })
  void shouldManipulateRecipientSystem(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationReplySystemsManipulators();
    val cRM =
        new CommunicationReplyMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            "http://www.asdasd.de",
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val comRepl =
        ErxCommunicationBuilder.asReply(cRM).basedOn("test", "Test2").receiver("!").build();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator);
    manipulator.getParameter().accept(comRepl);
    assertNotEquals(
        KVNR.random().getSystem(),
        comRepl.getRecipient().stream().findFirst().orElseThrow().getIdentifier().getSystem(),
        "No Fitting FlowType-System contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, who set payload.extension.url into sender-System",
      })
  void shouldManipulateTelematikIdSystem(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationReplySystemsManipulators();
    val cRM =
        new CommunicationReplyMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            "http://www.asdasd.de",
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val comRepl =
        ErxCommunicationBuilder.asReply(cRM).basedOn("test", "Test2").receiver("!").build();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator);
    manipulator.getParameter().accept(comRepl);
    assertNotEquals(
        TelematikID.random().getSystem(),
        comRepl.getSender().getIdentifier().getSystem(),
        "No Fitting FlowType-System contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Systems Manipulator, who set TelematikId into payload.extension.url",
      })
  void shouldManipulatePyloadSystem(String manipulatorDescription) {
    val manipulators =
        ErxCommunicationPayloadManipulatorFactory.getCommunicationReplySystemsManipulators();
    val cRM =
        new CommunicationReplyMessage(
            1,
            SupplyOptionsType.createDefault().getLabel(),
            GemFaker.fakerCommunicationInfoReqMessage(),
            "http://www.asdasd.de",
            "12345678",
            "123ewrßiokx-mclöv90ß-2331");
    val comRepl =
        ErxCommunicationBuilder.asReply(cRM).basedOn("test", "Test2").receiver("!").build();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator);
    manipulator.getParameter().accept(comRepl);
    assertNotEquals(
        SUPPLY_OPTIONS_TYPE.getCanonicalUrl(),
        comRepl.getPayloadFirstRep().getExtensionFirstRep().getUrl(),
        "No Fitting FlowType-System contained");
  }

  /**
   * Utility method to find a manipulator by name.
   *
   * @param manipulators List of NamedEnvelope objects
   * @param name The name of the manipulator to find
   * @return The NamedEnvelope if found, otherwise null
   */
  private NamedEnvelope<FuzzingMutator<ErxCommunication>> findManipulator(
      List<NamedEnvelope<FuzzingMutator<ErxCommunication>>> manipulators, String name) {
    return manipulators.stream().filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
  }
}
