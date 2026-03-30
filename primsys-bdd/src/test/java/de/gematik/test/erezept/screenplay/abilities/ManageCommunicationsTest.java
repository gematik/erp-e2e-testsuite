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

package de.gematik.test.erezept.screenplay.abilities;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ManageCommunicationsTest extends ErpFhirBuildingTest {

  @Test
  void shouldHaveInitializedCommunicationStacks() {
    val ability = ManageCommunications.sheExchanges();
    assertTrue(ability.getSentCommunications().isEmpty());
    assertTrue(ability.getExpectedCommunications().isEmpty());
  }

  @ParameterizedTest
  @MethodSource
  void shouldTeardown(Resource resource, int returnCode) {
    OnStage.setTheStage(new Cast() {});

    val actor = OnStage.theActor("Alice");
    val ability = spy(ManageCommunications.sheExchanges());
    actor.can(ability);

    val com =
        ErxCommunicationBuilder.forDispenseRequest("Hello World")
            .basedOn(TaskId.random(), AccessCode.random())
            .flowType(PrescriptionFlowType.FLOW_TYPE_160)
            .receiver(TelematikID.random().getValue())
            .build();
    val exc = ExchangedCommunication.from(com).withActorNames("Alice", "Bob");
    ability.getSentCommunications().append(exc);

    val erpClient = mock(ErpClient.class);
    val erpClientAbility = UseTheErpClient.with(erpClient);
    actor.can(erpClientAbility);

    val mockResponse =
        ErpResponse.forPayload(resource, EmptyResource.class)
            .withStatusCode(returnCode)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(erpClient.request(any(CommunicationDeleteCommand.class))).thenReturn(mockResponse);

    OnStage.drawTheCurtain();

    verify(ability, times(1)).tearDown();
  }

  static Stream<Arguments> shouldTeardown() {
    return Stream.of(
        Arguments.of(createOperationOutcome(), 404), Arguments.of(new EmptyResource(), 204));
  }
}
