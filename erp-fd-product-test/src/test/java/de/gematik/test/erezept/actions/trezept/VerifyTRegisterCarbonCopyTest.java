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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.Date;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerifyTRegisterCarbonCopyTest extends ErpFhirParsingTest {

  @BeforeEach
  void setupCoverageReporter() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

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
  void shouldVerifyCarbonCopyWhenMedDispenseBundleIsPresent() {
    val actor = actorWithParser();
    val prescriptionId = PrescriptionId.from("166.100.000.000.001.39");
    val bundle = mock(ErxMedicationDispenseBundle.class);
    when(bundle.getDispensePairBy(prescriptionId)).thenReturn(correctMedicationDispensePair());
    val task =
        VerifyTRegisterCarbonCopy.from(List.of(carbonCopyLog()), prescriptionId, bundle, false);

    assertDoesNotThrow(() -> task.performAs(actor));
    verify(bundle).getDispensePairBy(prescriptionId);
  }

  @Test
  void shouldFailWhenMedDispenseBundleDoesNotMatchCarbonCopy() {
    val actor = actorWithParser();
    val prescriptionId = PrescriptionId.from("166.100.000.000.001.39");
    val bundle = mock(ErxMedicationDispenseBundle.class);
    when(bundle.getDispensePairBy(prescriptionId)).thenReturn(incorrectMedicationDispensePair());
    val task =
        VerifyTRegisterCarbonCopy.from(List.of(carbonCopyLog()), prescriptionId, bundle, false);
    val error = assertThrows(AssertionError.class, () -> task.performAs(actor));
    verify(bundle).getDispensePairBy(prescriptionId);
    assertTrue(
        error
            .getMessage()
            .contains("[Die PZN in der CarbonCopy stimmt mit der PZN der Medication überein]"));
  }

  @Test
  void shouldFailWhenNoMedicationDispensePairCanBeFound() {
    val actor = actorWithParser();
    val prescriptionId = PrescriptionId.from("166.100.000.000.001.39");
    val bundle = mock(ErxMedicationDispenseBundle.class);
    when(bundle.getDispensePairBy(prescriptionId)).thenReturn(List.of());
    val task =
        VerifyTRegisterCarbonCopy.from(List.of(carbonCopyLog()), prescriptionId, bundle, false);

    val error = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertEquals(
        "No MedicationDispense and GemErpMedication pair found to verify CarbonCopies PZN",
        error.getMessage());
    verify(bundle).getDispensePairBy(prescriptionId);
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

  @Test
  void shouldFailWhenCarbonCopyPayloadCannotBeDecoded() {
    val actor = actorWithParser();
    val request = HttpBRequest.method(HttpRequestMethod.GET).withPayload("not-json");
    val log =
        new TRegisterLog(System.currentTimeMillis(), "id-1", "166.100.000.000.001.39", request);
    val bundle = mock(ErxMedicationDispenseBundle.class);
    val task =
        VerifyTRegisterCarbonCopy.from(
            List.of(log), PrescriptionId.from("166.100.000.000.001.39"), bundle, false);

    assertThrows(Exception.class, () -> task.performAs(actor));
  }

  private Actor actorWithParser() {
    val actor = Actor.named("doctor");
    val useTheErpClient = mock(UseTheErpClient.class, RETURNS_DEEP_STUBS);
    when(useTheErpClient.getFhir()).thenReturn(parser);
    actor.can(useTheErpClient);
    return actor;
  }

  private TRegisterLog carbonCopyLog() {
    val content =
        ResourceLoader.readFileFromResource("TPrescription/Parameters-TRP-Carbon-Copy.json");
    val request = HttpBRequest.method(HttpRequestMethod.GET).withPayload(content);
    return new TRegisterLog(System.currentTimeMillis(), "id-1", "166.100.000.000.001.39", request);
  }

  private List<Pair<ErxMedicationDispense, GemErpMedication>> correctMedicationDispensePair() {
    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("19201712"), "Pomalidomid Accord 1 mg 21 x 1 Hartkapseln")
            .withDarreichungsform(Darreichungsform.HKP)
            .fake();

    return List.of(
        Pair.of(
            medicationDispense(PrescriptionId.from("166.100.000.000.001.39"), medication),
            medication));
  }

  private List<Pair<ErxMedicationDispense, GemErpMedication>> incorrectMedicationDispensePair() {
    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("11111111"), "Fruchtgummies die extra weichen")
            .withDarreichungsform(Darreichungsform.KDA)
            .fake();

    return List.of(
        Pair.of(
            medicationDispense(PrescriptionId.from("166.100.111.000.111.11"), medication),
            medication));
  }

  private ErxMedicationDispense medicationDispense(
      PrescriptionId prescriptionId, GemErpMedication medication) {
    return ErxMedicationDispenseBuilder.forKvnr(KVNR.from(""))
        .version(ErpWorkflowVersion.V1_6)
        .performerId(TelematikID.random())
        .prescriptionId(prescriptionId)
        .status("completed")
        .whenPrepared(new Date())
        .whenHandedOver(new Date())
        .batch("123456", new Date())
        .wasSubstituted(true)
        .medication(medication)
        .build();
  }
}
