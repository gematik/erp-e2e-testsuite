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

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KbvErpBundleTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_1_0 = "fhir/valid/kbv/1.1.0/bundle/";

  @Test
  @SuppressWarnings({"java:S5961"})
  void testEncodingCodingSingleValidKbvBundle() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val expectedPrescriptionId = PrescriptionId.from("160.100.000.000.037.28");
    val expectedMedicationRequestId = "43c2b7ae-ad11-4387-910a-e6b7a3c38d4f";
    val expectedInsuranceType = InsuranceTypeDe.GKV;
    val expectedKvnr = "K220635158";
    val expectedGivenName = "Ludger";
    val expectedLastName = "Königsstein";
    val expectedFamilyName = "Königsstein";
    val expectedBirthDate = new GregorianCalendar(1935, Calendar.JUNE, 22).getTime();
    val expectedCity = "Esens";
    val expectedPostal = "26427";
    val expectedStreet = "Blumenweg";
    val expectedCoverageIknr = "109719018";
    val expectedCoverageName = "AOK Nordost";
    val expectedCoverageKind = InsuranceTypeDe.GKV;
    val expectedCoverageWop = Wop.NIEDERSACHSEN;
    val expectedCoverageState = VersichertenStatus.PENSIONER;
    val expectedCoveragePersonGroup = PersonGroup.NOT_SET;
    val expectedMedicationCategory = MedicationCategory.C_00;
    val expectedMedicationAmount = 0;
    val expectedQuantity = 1;

    val fileName = expectedID + ".xml";
    val originalEncoding = EncodingType.fromString(fileName);
    val flippedEncoding = originalEncoding.flipEncoding();

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(expectedID, kbvBundle.getLogicalId());
    assertEquals(expectedID, kbvBundle.asReference().getReference());
    assertNotNull(kbvBundle.getDescription());
    assertEquals(
        KbvItaErpStructDef.BUNDLE.getVersionedUrl(KbvItaErpVersion.V1_1_0),
        kbvBundle.getFullMetaProfile());
    assertEquals(KbvItaErpStructDef.BUNDLE.getCanonicalUrl(), kbvBundle.getMetaProfile());

    assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, kbvBundle.getFlowType());
    assertEquals(expectedMedicationRequestId, kbvBundle.getMedicationRequest().getLogicalId());
    assertFalse(kbvBundle.getMedicationRequest().hasEmergencyServiceFee());
    assertEquals(expectedInsuranceType, kbvBundle.getPatient().getInsuranceType());
    assertEquals(expectedKvnr, kbvBundle.getPatient().getKvnr().getValue());
    assertEquals(expectedGivenName, kbvBundle.getPatient().getGivenName());
    assertEquals(expectedFamilyName, kbvBundle.getPatient().getFamilyName());
    assertEquals(expectedLastName, kbvBundle.getPatient().getFamilyName());
    assertEquals(expectedBirthDate, kbvBundle.getPatient().getBirthDate());
    assertEquals(expectedCity, kbvBundle.getPatient().getAddressCity());
    assertEquals(expectedPostal, kbvBundle.getPatient().getAddressPostalCode());
    assertEquals(expectedStreet, kbvBundle.getPatient().getAddressStreet());
    assertEquals(expectedCoverageIknr, kbvBundle.getCoverage().getIknrOrThrow().getValue());
    assertEquals(expectedCoverageName, kbvBundle.getCoverage().getName());
    assertEquals(expectedCoverageKind, kbvBundle.getCoverage().getInsuranceKind());
    assertTrue(kbvBundle.getCoverage().hasInsuranceKind());
    assertFalse(kbvBundle.getCoverage().hasPayorType());
    assertEquals(expectedCoverageWop, kbvBundle.getCoverage().getWop().orElseThrow());
    assertEquals(expectedCoverageState, kbvBundle.getCoverage().getInsurantState());
    assertEquals(expectedCoveragePersonGroup, kbvBundle.getCoverage().getPersonGroup());
    assertEquals(expectedMedicationCategory, kbvBundle.getMedication().getCategoryFirstRep());
    assertEquals(expectedMedicationAmount, kbvBundle.getMedication().getPackagingSizeOrEmpty());
    assertEquals(expectedQuantity, kbvBundle.getMedicationRequest().getDispenseQuantity());
    assertFalse(kbvBundle.getMedicationRequest().isMultiple());

    val medication = kbvBundle.getMedication();
    assertEquals(StandardSize.N1, medication.getStandardSize());
    assertNotNull(medication.getDescription());
    assertEquals(KbvItaErpVersion.V1_1_0, medication.getVersion());
    assertTrue(medication.getPackagingSize().isEmpty());
    assertTrue(medication.getPackagingUnit().isEmpty());

    val composition = kbvBundle.getComposition();
    val compProfile = composition.getMeta().getProfile().get(0).asStringValue();
    val expectedProfile = KbvItaErpStructDef.COMPOSITION.getVersionedUrl(KbvItaErpVersion.V1_1_0);
    assertEquals(expectedProfile, compProfile);

    // flip the encoding and check again
    val flippedContent = parser.encode(kbvBundle, flippedEncoding);
    val flippedKbvBundle = parser.decode(KbvErpBundle.class, flippedContent);

    assertEquals(kbvBundle.getLogicalId(), flippedKbvBundle.getLogicalId());
  }

  @ParameterizedTest(name = "Find 'Emergency Fee' in MedicationRequest")
  @ValueSource(strings = {BASE_PATH_1_1_0})
  void shouldFindEmergencyFee(String base) {
    val expectedId = "690a7f01-058e-492a-b1dc-d6d8c8a30a59";
    val fileName = expectedId + ".xml";

    val content = ResourceLoader.readFileFromResource(base + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val medicationRequest = kbvBundle.getMedicationRequest();
    assertTrue(medicationRequest.hasEmergencyServiceFee());
  }

  @ParameterizedTest(name = "Find 'BVG' in MedicationRequest")
  @ValueSource(strings = {BASE_PATH_1_1_0})
  void shouldFindBvg(String base) {
    val expectedId = "aea2f4c5-675a-4d76-ab9b-7994c80b64ec";
    val fileName = expectedId + ".xml";

    val content = ResourceLoader.readFileFromResource(base + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val medicationRequest = kbvBundle.getMedicationRequest();
    assertTrue(medicationRequest.isBvg());
  }

  @Test
  void shouldGetMedicalOrganization() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    assertDoesNotThrow(kbvBundle::getMedicalOrganization);
    assertNotNull(kbvBundle.getMedicalOrganization());
  }

  @Test
  void shouldEncodeSinglePkvKbvBundle() {
    val expectedID = "328ad940-3fff-11ed-b878-0242ac120002";
    val fileName = expectedID + ".xml";

    val expectedIknr = IKNR.asSidIknr("123456789");
    val expectedName = "Allianz Private Krankenversicherung";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    // which is actually wrong: PKV must have 200; but we're encoding the example correctly
    // this is due to the fact that the example has a 160.xx prescription ID
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, kbvBundle.getFlowType());

    val coverage = kbvBundle.getCoverage();
    assertEquals(expectedIknr, coverage.getIknrOrThrow());
    assertEquals(expectedName, coverage.getName());
    assertDoesNotThrow(() -> kbvBundle.getPatient().getKvnr());
  }

  @Test
  void shouldChangePrescriptionId() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val expectedPrescriptionId = PrescriptionId.from("160.100.000.000.037.28");
    val randomPrescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_160);
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

    kbvBundle.setPrescriptionId(randomPrescriptionId);
    assertEquals(randomPrescriptionId, kbvBundle.getPrescriptionId());
  }

  @Test
  void shouldThrowOnInvalidPrescriptionIdSystem() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val fileName = expectedID + ".xml";

    val mockPrescriptionId = mock(PrescriptionId.class);
    when(mockPrescriptionId.getSystemUrl()).thenReturn("hello_world");

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    kbvBundle.setPrescriptionId(mockPrescriptionId);
    assertThrows(MissingFieldException.class, kbvBundle::getPrescriptionId);
  }

  @Test
  void testEncodingCodingSingleValidKbvBundleWitzPzn() {
    val expectedID = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val expectedPzn = "03507952";
    val expectedName = "Novaminsulfon 500 mg Lichtenstein 100 ml Tropf. N3";
    val expectedDosage = "bis zu 4mal täglich je 20-40 Tropfen";
    val expectedNumerator = "100 ml";

    assertTrue(kbvBundle.getMedication().getPznOptional().isPresent());
    assertEquals(expectedPzn, kbvBundle.getMedication().getPznFirstRep());
    assertEquals(expectedName, kbvBundle.getMedication().getMedicationName());
    assertEquals(expectedDosage, kbvBundle.getMedicationRequest().getDosageInstructionAsText());
    assertTrue(kbvBundle.getMedicationRequest().isSubstitutionAllowed());
    assertFalse(kbvBundle.getMedication().getMedicationType().isPresent());
    assertFalse(kbvBundle.getMedication().getIngredientText().isPresent());
    assertFalse(kbvBundle.getMedication().getIngredientStrengthString().isPresent());
    assertTrue(kbvBundle.getMedication().getAmountNumeratorString().isPresent());
    assertEquals(
        expectedNumerator, kbvBundle.getMedication().getAmountNumeratorString().orElseThrow());
  }

  @Test
  void testEncodingSingleKbvBundleIngredient() {
    val expectedID = "9c85a2a5-92ee-4a57-83cb-ba90a0df2a21";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.INGREDIENT, kbvBundle.getMedication().getMedicationType().orElseThrow());
    assertEquals("Ramipril", kbvBundle.getMedication().getIngredientText().orElseThrow());
    assertEquals("100 Stück", kbvBundle.getMedication().getAmountNumeratorString().orElseThrow());
    assertEquals("5 mg", kbvBundle.getMedication().getIngredientStrengthString().orElseThrow());
  }

  @Test
  void shouldEncodeSingleKbvBundleCompounding() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.COMPOUNDING, kbvBundle.getMedication().getMedicationType().orElseThrow());

    assertEquals("500 ml", kbvBundle.getMedication().getAmountNumeratorString().orElseThrow());
  }

  @Test
  void testEncodingSingleKbvBundleFreetext() {
    val expectedID = "4863d1fb-dc26-4680-bb35-018610d1749d";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.FREETEXT, kbvBundle.getMedication().getMedicationType().orElseThrow());
    assertFalse(kbvBundle.getMedication().getAmountNumeratorString().isPresent());
  }

  @Test
  void shouldEncodeSingleCompounding110Bundle() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    assertEquals(KbvItaErpVersion.V1_1_0, kbvBundle.getMedication().getVersion());

    val medication = kbvBundle.getMedication();
    medication
        .getPackagingSize()
        .ifPresentOrElse(
            amount -> assertEquals("500", amount),
            () -> fail("Expected Packaging Amount not found"));
    medication
        .getPackagingUnit()
        .ifPresentOrElse(
            unit -> assertEquals("ml", unit), () -> fail("Expected Packaging Unit not found"));

    val medicationRequest = kbvBundle.getMedicationRequest();
    assertFalse(medicationRequest.isBvg());
    assertFalse(medicationRequest.hasEmergencyServiceFee());

    assertEquals(MedicationType.COMPOUNDING, medication.getMedicationType().orElseThrow());
    assertTrue(medication.getAmountNumeratorString().isPresent());
  }

  @Test
  void shouldFailOnMissingMedication() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    // invalidate
    val medications =
        kbvBundle.getEntry().stream()
            .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Medication))
            .toList();
    kbvBundle.getEntry().removeAll(medications);

    assertThrows(MissingFieldException.class, kbvBundle::getMedication);
  }

  @Test
  void shouldFailOnMissingPayorType() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val coverage = kbvBundle.getCoverage();

    assertTrue(coverage.getPayorType().isEmpty());
  }

  @Test
  void testChangeOfDates() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";

    val fileName = expectedID + ".xml";
    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val origAuthoredOn = kbvBundle.getAuthoredOn();
    val origCompositionDate = kbvBundle.getCompositionDate();
    val origTimestamp = kbvBundle.getTimestamp();
    val origLastUpdate = kbvBundle.getMeta().getLastUpdated();

    kbvBundle.setAllDates();
    assertTrue(origAuthoredOn.before(kbvBundle.getAuthoredOn()));
    assertTrue(origCompositionDate.before(kbvBundle.getCompositionDate()));
    assertTrue(origTimestamp.before(kbvBundle.getTimestamp()));
    assertTrue(origLastUpdate.before(kbvBundle.getMeta().getLastUpdated()));
  }

  @Test
  void shouldProvideOriginalPatientAsKbvErpPatient() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val originalPatient =
        kbvBundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
            .findFirst()
            .orElseThrow(() -> new MissingFieldException(kbvBundle.getClass(), "Patient"));

    val kbvPatient1 = kbvBundle.getPatient();
    val kbvPatient2 = kbvBundle.getPatient();
    assertEquals(originalPatient, kbvPatient1);
    assertEquals(kbvPatient1, kbvPatient2);

    // now check if deep copy
    kbvPatient1.addExtension("abc", new StringType("xyz"));
    assertEquals(1, kbvPatient2.getExtensionsByUrl("abc").size());
  }

  @Test
  void shouldDecodeWithoutExpectedType() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val bundle = parser.decode(content);
    assertEquals(ResourceType.Bundle, bundle.getResourceType());
    assertEquals(KbvErpBundle.class, bundle.getClass());
  }

  @Test
  void shouldCopyWithBundleEntries() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(KbvErpMedicationRequest.class, kbvBundle.getMedicationRequest().getClass());

    val copy = new KbvErpBundle();
    kbvBundle.copyValues(copy);
    assertEquals(KbvErpMedicationRequest.class, copy.getMedicationRequest().getClass());
  }

  @Test
  void shouldFailOnInappropriateExtensions() {
    val extension =
        new Extension("https://test.erp.gematik.de").setValue(new StringType("Just a Testvalue"));

    List<ConsumerExpectation<KbvErpBundle>> extensionConsumer =
        List.of(
            ConsumerExpectation.from(
                (bundle) -> bundle.getComposition().addExtension(extension),
                "Extension in Composition",
                false),
            ConsumerExpectation.from(
                (bundle) -> bundle.getMedication().addExtension(extension),
                "Extension in Medication",
                false),
            ConsumerExpectation.from(
                (bundle) -> bundle.getMedicationRequest().addExtension(extension),
                "Extension in MedicationRequest",
                false),
            ConsumerExpectation.from(
                (bundle) -> bundle.getPatient().addExtension(extension),
                "Extension in Patient",
                false),
            ConsumerExpectation.from(
                (bundle) -> bundle.getCoverage().addExtension(extension),
                "Extension in Coverage",
                false));

    extensionConsumer.forEach(
        c -> {
          val kbvBundle =
              KbvErpBundleFaker.builder()
                  .withKvnr(KVNR.random())
                  .withPrescriptionId(PrescriptionId.from("160.002.362.150.600.45"))
                  .fake();

          c.accept(kbvBundle);

          val encoded = parser.encode(kbvBundle, EncodingType.XML);
          val result = parser.validate(encoded);
          assertEquals(
              c.expectedResult,
              result.isSuccessful(),
              format(
                  "Inappropriate {0} must result in {1} for KbvBundle in Version {2}",
                  c.name, c.expectedResult, kbvBundle.getMetaProfileVersion()));
        });
  }

  @Test
  @SuppressWarnings({"java:S5961"})
  void testEncodingCodingSingleValidKbvBundleAsMedicationCompoundingAndCheckPZN() {
    val expectedID = "a409358a-da34-11eb-8d19-0242ac130003";
    val expectedPZN = "10206346";

    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    assertTrue(kbvBundle.getMedication().getPznOptional().isPresent());
    val medication = kbvBundle.getMedication();
    assertEquals(expectedPZN, medication.getPznOptional().get().getValue());
    assertEquals(
        "Aluminiumchlorid-Hexahydrat", medication.getIngredientTextOptional().orElseThrow());
    assertTrue(medication.getDarreichungsform().isEmpty());
  }

  @Test
  void shouldGetMedicationRequests() {
    val bundle = KbvErpBundleFaker.builder().fake();
    assertDoesNotThrow(bundle::getMedicationRequests);
  }

  @Test
  void shouldGetFirstMedicationRequest() {
    val bundle = KbvErpBundleFaker.builder().fake();
    assertDoesNotThrow(bundle::getFirstMedicationRequest);
  }

  @Test
  void shouldHandleMissingMedicationRequest() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    kbvBundle
        .getEntry()
        .removeIf(
            entry -> entry.getResource().getResourceType().equals(ResourceType.MedicationRequest));
    assertTrue(kbvBundle.getMedicationRequestOptional().isEmpty());
    assertThrows(MissingFieldException.class, kbvBundle::getMedicationRequest);
  }

  @Test
  void shouldFetchSupplyRequest() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    kbvBundle.getEntry().add(new BundleEntryComponent().setResource(new SupplyRequest()));
    assertTrue(kbvBundle.getSupplyRequest().isPresent());
  }

  @RequiredArgsConstructor
  static class ConsumerExpectation<T> {
    private final Consumer<T> consumer;
    private final String name;
    private final boolean expectedResult;

    public void accept(T t) {
      consumer.accept(t);
    }

    public static <T> ConsumerExpectation<T> from(
        Consumer<T> consumer, String name, boolean expectedResult) {
      return new ConsumerExpectation<>(consumer, name, expectedResult);
    }
  }
}
