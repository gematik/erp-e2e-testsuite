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

package de.gematik.test.erezept.actions.trezept;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.List;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class VerifyTRegisterCarbonCopyTest {

  @Test
  void shouldFailWhenLogsAreNull() {
    Actor actor = Actor.named("doctor");
    VerifyTRegisterCarbonCopy task = VerifyTRegisterCarbonCopy.from(null, null, null, null);
    AssertionError ex = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertEquals("No carbon copy found in T-Register", ex.getMessage());
  }

  @Test
  void shouldFailWhenLogsAreEmpty() {
    Actor actor = Actor.named("doctor");
    VerifyTRegisterCarbonCopy task = VerifyTRegisterCarbonCopy.from(List.of(), null, null, null);
    AssertionError ex = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertEquals("No carbon copy found in T-Register", ex.getMessage());
  }

  @Test
  void shouldEnterOptionalBlockWhenMedDispenseBundlePresent() {
    Actor actor = Actor.named("doctor");

    TRegisterLog log = mock(TRegisterLog.class, RETURNS_DEEP_STUBS);

    when(log.request().bodyAsString()).thenReturn("{}");

    ErxMedicationDispenseBundle bundle = mock(ErxMedicationDispenseBundle.class);
    when(bundle.getDispensePairBy(any())).thenReturn(List.of());

    VerifyTRegisterCarbonCopy task =
        VerifyTRegisterCarbonCopy.from(List.of(log), null, bundle, false);

    assertThrows(Exception.class, () -> task.performAs(actor));
  }

  @Test
  void shouldPassLogsCheckWhenLogsAreNotNullAndNotEmpty() {
    Actor actor = Actor.named("doctor");

    TRegisterLog log = mock(TRegisterLog.class, RETURNS_DEEP_STUBS);

    when(log.request().bodyAsString()).thenReturn("{}");

    VerifyTRegisterCarbonCopy task =
        VerifyTRegisterCarbonCopy.from(List.of(log), null, null, false);

    assertDoesNotThrow(() -> task.performAs(actor));
  }

  @Test
  void shouldVerifyAccessTokenInTRegisterWhenAuthCheckIsEnabled() {
    Actor actor = Actor.named("doctor");

    TRegisterMockDownloadRequest request =
        new TRegisterMockDownloadRequest("166.000.000.000.001.39");

    request.headers().add(new HttpHeader("Authorization", "Bearer token"));

    TRegisterLog log =
        new TRegisterLog(System.currentTimeMillis(), "id-1", "166.000.000.000.001.39", request);

    VerifyTRegisterCarbonCopy task = VerifyTRegisterCarbonCopy.from(List.of(log), null, null, true);

    assertDoesNotThrow(() -> task.performAs(actor));
  }
}
