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

import static de.gematik.test.erezept.fhir.profiles.definitions.GemErpTPrescStructDef.MEDICATION_DISPENSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.compare.api.ResourceComparator;
import de.gematik.bbriccs.fhir.compare.trans.StructureMapsTransformer;
import de.gematik.bbriccs.fhir.compare.trans.StructureMapsTransformerFactory;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.abilities.UseResourceComparator;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.trezept.TRegisterLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VerifyTRegisterCarbonCopyUsingStructureMapsTest extends ErpFhirParsingTest {

  @Test
  void shouldFailWhenLogsAreNull() {
    Actor actor = Actor.named("doctor");
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            null,
            PrescriptionId.random(),
            new ErxMedicationDispenseBundle(),
            false,
            new ErxTask(),
            new KbvErpBundle(),
            new PharmacyActor(""));
    AssertionError ex = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertEquals("No carbon copy found in T-Register", ex.getMessage());
  }

  @Test
  void shouldFailWhenLogsAreEmpty() {
    Actor actor = Actor.named("doctor");
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            List.of(), null, null, false, null, null, null);
    AssertionError ex = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertEquals("No carbon copy found in T-Register", ex.getMessage());
  }

  @Test
  void shouldPassWhenMedicationDispenseBundleIsAbsent() {
    val actor = actorWithComparator();
    val pharmacy = pharmacyWithParser();
    val log = carbonCopyLog();
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            List.of(log),
            PrescriptionId.from("166.100.000.000.001.39"),
            null,
            false,
            null,
            null,
            pharmacy);

    assertDoesNotThrow(() -> task.performAs(actor));
  }

  @Test
  void shouldFailWhenReferencedCarbonCopyListIsEmpty() {
    val actor = actorWithComparator();
    val pharmacy = pharmacyWithParser();
    val medDispenseBundle = mock(ErxMedicationDispenseBundle.class);
    val prescriptionId = PrescriptionId.from("166.100.000.000.001.39");
    when(medDispenseBundle.getDispensePairBy(prescriptionId)).thenReturn(List.of());
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            List.of(carbonCopyLog()),
            prescriptionId,
            medDispenseBundle,
            false,
            null,
            null,
            pharmacy);

    assertThrows(IndexOutOfBoundsException.class, () -> task.performAs(actor));
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldCompareCarbonCopyGeneratedByStructureMapsWhenMedicationDispenseBundleIsPresent() {
    val comparator = mock(ResourceComparator.class);
    val actor = actorWithComparator(comparator);
    val pharmacy = pharmacyWithParser();
    val medDispenseBundle = mock(ErxMedicationDispenseBundle.class);
    val prescriptionId = PrescriptionId.from("166.100.000.000.001.39");
    val medication = new GemErpMedication();
    medication.setId("Medication/gem-medication-id");
    val kbvMedication = new KbvErpMedication();
    kbvMedication.setId("Medication/kbv-medication-id");
    val kbvErpBundle = mock(KbvErpBundle.class);
    when(kbvErpBundle.getMedication()).thenReturn(kbvMedication);
    when(medDispenseBundle.getDispensePairBy(prescriptionId))
        .thenReturn(List.of(Pair.of(new ErxMedicationDispense(), medication)));
    val erxTask = new ErxTask();
    val carbonCopyByStructureMaps = new Parameters();
    val comparisonResult = new Parameters();
    val transformer = mock(StructureMapsTransformer.class);
    when(transformer.transform(anyList(), eq(Parameters.class), any(Parameters.class)))
        .thenReturn(carbonCopyByStructureMaps);
    when(comparator.compare(any(Resource.class), same(carbonCopyByStructureMaps)))
        .thenReturn(comparisonResult);
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            List.of(carbonCopyLog()),
            prescriptionId,
            medDispenseBundle,
            false,
            erxTask,
            kbvErpBundle,
            pharmacy);

    try (val transformerFactory = mockStatic(StructureMapsTransformerFactory.class)) {
      transformerFactory
          .when(() -> StructureMapsTransformerFactory.buildFor("T-Rezept"))
          .thenReturn(transformer);

      assertDoesNotThrow(() -> task.performAs(actor));

      transformerFactory.verify(() -> StructureMapsTransformerFactory.buildFor("T-Rezept"));
    }
    verify(medDispenseBundle).getDispensePairBy(prescriptionId);
    verify(kbvErpBundle).getMedication();
    val sourcesCaptor = ArgumentCaptor.forClass(List.class);
    val patchCaptor = ArgumentCaptor.forClass(Parameters.class);
    verify(transformer)
        .transform(sourcesCaptor.capture(), eq(Parameters.class), patchCaptor.capture());
    val sources = (List<Resource>) sourcesCaptor.getValue();
    assertEquals(4, sources.size());
    assertSame(erxTask, sources.get(0));
    assertSame(kbvErpBundle, sources.get(1));
    assertInstanceOf(Parameters.class, sources.get(2));
    assertInstanceOf(Bundle.class, sources.get(3));
    assertEquals("gem-medication-id", medication.getId());
    assertEquals("kbv-medication-id", kbvMedication.getId());
    assertExpectedStructureMapPatch(patchCaptor.getValue());
    verify(comparator).compare(any(Resource.class), same(carbonCopyByStructureMaps));
  }

  @Test
  void shouldFailWhenCarbonCopyPayloadCannotBeDecoded() {
    val actor = actorWithComparator();
    val pharmacy = pharmacyWithParser();
    val invalidRequest = HttpBRequest.method(HttpRequestMethod.GET).withPayload("not-json");
    val log =
        new TRegisterLog(
            System.currentTimeMillis(), "id-1", "166.100.000.000.001.39", invalidRequest);
    val task =
        VerifyTRegisterCarbonCopyUsingStructureMaps.from(
            List.of(log),
            PrescriptionId.from("166.100.000.000.001.39"),
            null,
            false,
            null,
            null,
            pharmacy);

    assertThrows(Exception.class, () -> task.performAs(actor));
  }

  @Test
  void shouldAddTypeCodingPatchOperation() {
    val patch = new Parameters();
    val target = "Parameters.parameter.where(name='rxDispensation')";

    invokePrivateHelper("addTypeCodingIn", patch, target, "test-system", "test-code");

    val operation = assertSingleOperationWith(patch, target, "type");
    val value = operation.getPart().get(3).getValue();
    assertInstanceOf(CodeableConcept.class, value);
    val coding = ((CodeableConcept) value).getCodingFirstRep();
    assertEquals("test-system", coding.getSystem());
    assertEquals("test-code", coding.getCode());
  }

  @Test
  void shouldAddIdentifierTypePatchOperation() {
    val patch = new Parameters();
    val target =
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource.identifier";

    invokePrivateHelper("addIdentifierTypeIn", patch, target, "test-system", "test-code");

    val operation = assertSingleOperationWith(patch, target, "type");
    val value = operation.getPart().get(3);
    val coding = value.getPart().get(0);
    assertEquals("value", value.getName());
    assertEquals("coding", coding.getName());
    assertInstanceOf(Coding.class, coding.getValue());
    assertEquals("test-system", ((Coding) coding.getValue()).getSystem());
    assertEquals("test-code", ((Coding) coding.getValue()).getCode());
  }

  @Test
  void shouldAddMetaProfilePatchOperation() {
    val patch = new Parameters();
    val target =
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseInformation').part.where(name='medicationDispense').resource";

    invokePrivateHelper("addMetaProfileIn", patch, target, MEDICATION_DISPENSE);

    val operation = assertSingleOperationWith(patch, target, "meta");
    val value = operation.getPart().get(3);
    val profile = value.getPart().get(0);
    assertEquals("value", value.getName());
    assertEquals("profile", profile.getName());
    assertInstanceOf(CanonicalType.class, profile.getValue());
    assertEquals(
        MEDICATION_DISPENSE.getCanonicalUrl(), ((CanonicalType) profile.getValue()).getValue());
  }

  @Test
  void shouldAddSignatureDatePatchOperation() {
    val patch = new Parameters();

    invokePrivateHelper("addSignatureDate", patch, "2026-04-01T08:23:12Z");

    val operation =
        assertSingleOperationWith(
            patch, "Parameters.parameter.where(name='rxPrescription')", "part");
    val value = operation.getPart().get(3);
    assertEquals("value", value.getName());
    assertEquals("name", value.getPart().get(0).getName());
    assertEquals("prescriptionSignatureDate", value.getPart().get(0).getValue().primitiveValue());
    assertEquals("value", value.getPart().get(1).getName());
    assertInstanceOf(InstantType.class, value.getPart().get(1).getValue());
    assertEquals("2026-04-01T08:23:12+00:00", value.getPart().get(1).getValue().primitiveValue());
  }

  private Actor actorWithComparator() {
    return actorWithComparator(mock(ResourceComparator.class));
  }

  private Actor actorWithComparator(ResourceComparator comparator) {
    val actor = Actor.named("doctor");
    actor.can(UseResourceComparator.with(comparator));
    return actor;
  }

  private PharmacyActor pharmacyWithParser() {
    val pharmacy = new PharmacyActor("pharmacy");
    val useTheErpClient = mock(UseTheErpClient.class, RETURNS_DEEP_STUBS);
    when(useTheErpClient.getFhir()).thenReturn(parser);
    pharmacy.can(useTheErpClient);
    return pharmacy;
  }

  private TRegisterLog carbonCopyLog() {
    val content =
        ResourceLoader.readFileFromResource("TPrescription/Parameters-TRP-Carbon-Copy.json");
    val request = HttpBRequest.method(HttpRequestMethod.GET).withPayload(content);
    return new TRegisterLog(System.currentTimeMillis(), "id-1", "166.100.000.000.001.39", request);
  }

  private Parameters.ParametersParameterComponent assertSingleOperationWith(
      Parameters patch, String expectedPath, String expectedName) {
    assertEquals(1, patch.getParameter().size());
    val operation = patch.getParameterFirstRep();
    assertEquals("operation", operation.getName());
    assertEquals(4, operation.getPart().size());
    assertPart(operation, 0, "type", CodeType.class, "add");
    assertPart(operation, 1, "path", StringType.class, expectedPath);
    assertPart(operation, 2, "name", StringType.class, expectedName);
    return operation;
  }

  private void assertPart(
      Parameters.ParametersParameterComponent operation,
      int index,
      String expectedName,
      Class<?> expectedType,
      String expectedValue) {
    val part = operation.getPart().get(index);
    assertEquals(expectedName, part.getName());
    assertInstanceOf(expectedType, part.getValue());
    assertEquals(expectedValue, part.getValue().primitiveValue());
  }

  private void assertExpectedStructureMapPatch(Parameters patch) {
    assertEquals(6, patch.getParameter().size());
    assertPatchOperation(
        patch.getParameter().get(0), "Parameters.parameter.where(name='rxPrescription')", "part");
    assertPatchOperation(
        patch.getParameter().get(1),
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseInformation').part.where(name='medicationDispense').resource",
        "meta");
    assertPatchOperation(
        patch.getParameter().get(2),
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource",
        "meta");
    assertPatchOperation(
        patch.getParameter().get(3),
        "Parameters.parameter.where(name='rxPrescription').part.where(name='medicationRequest').resource",
        "meta");
    assertPatchOperation(
        patch.getParameter().get(4),
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource.identifier",
        "type");
    assertPatchOperation(
        patch.getParameter().get(5),
        "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource.identifier",
        "type");
  }

  private void assertPatchOperation(
      Parameters.ParametersParameterComponent operation, String expectedPath, String expectedName) {
    assertEquals("operation", operation.getName());
    assertPart(operation, 0, "type", CodeType.class, "add");
    assertPart(operation, 1, "path", StringType.class, expectedPath);
    assertPart(operation, 2, "name", StringType.class, expectedName);
  }

  private void invokePrivateHelper(String methodName, Object... args) {
    try {
      val method = helperMethod(methodName, args);
      method.setAccessible(true);
      method.invoke(null, args);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private Method helperMethod(String methodName, Object[] args) throws NoSuchMethodException {
    return switch (methodName) {
      case "addTypeCodingIn", "addIdentifierTypeIn" -> VerifyTRegisterCarbonCopyUsingStructureMaps
          .class
          .getDeclaredMethod(
              methodName, Parameters.class, String.class, String.class, String.class);
      case "addMetaProfileIn" -> VerifyTRegisterCarbonCopyUsingStructureMaps.class
          .getDeclaredMethod(methodName, Parameters.class, String.class, WithSystem.class);
      case "addSignatureDate" -> VerifyTRegisterCarbonCopyUsingStructureMaps.class
          .getDeclaredMethod(methodName, Parameters.class, String.class);
      default -> throw new NoSuchMethodException(methodName + " with " + args.length + " args");
    };
  }
}
