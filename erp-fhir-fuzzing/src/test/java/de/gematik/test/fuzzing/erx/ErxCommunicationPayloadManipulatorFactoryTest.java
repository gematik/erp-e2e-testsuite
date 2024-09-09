/*
 * Copyright 2024 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxCommunicationPayloadManipulatorFactoryTest {

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
              ErxCommunicationBuilder.builder()
                  .basedOnTask("test", "Test2")
                  .recipient("!")
                  .buildReply(cRM);
          m.getParameter().accept(comRepl);
          val cont = comRepl.getPayloadFirstRep().getContent();
          val stringCont = cont.castToString(cont).getValue();
          assertThrows(
              JacksonException.class,
              () -> mapper.readValue(stringCont, CommunicationReplyMessage.class));
        });
  }
}
