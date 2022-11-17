/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.builder.kbv.CoverageBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

class KbvErpBundleTest extends ParsingTest {

  private final String BASE_PATH_1_0_2 = "fhir/valid/kbv/1.0.2/bundle/";
  private final String BASE_PATH_1_1_0 = "fhir/valid/kbv/1.1.0/bundle/";

  @Test
  @SuppressWarnings({"java:S5961"})
  void testEncodingCodingSingleValidKbvBundle() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val expectedPrescriptionId = new PrescriptionId("160.100.000.000.037.28");
    val expectedEmergencyFee = false;
    val expectedInsuranceType = VersicherungsArtDeBasis.GKV;
    val expectedKvid = "K220635158";
    val expectedGivenName = "Ludger";
    val expectedLastName = "Königsstein";
    val expectedFamilyName = "Königsstein";
    val expectedBirthDate = new GregorianCalendar(1935, Calendar.JUNE, 22).getTime();
    val expectedCity = "Esens";
    val expectedPostal = "26427";
    val expectedStreet = "Blumenweg";
    val expectedCoverageIknr = "109719018";
    val expectedCoverageName = "AOK Nordost";
    val expectedCoverageKind = VersicherungsArtDeBasis.GKV;
    val expectedCoverageWop = Wop.NIEDERSACHSEN;
    val expectedCoverageState = VersichertenStatus.PENSIONER;
    val expectedCoveragePersonGroup = PersonGroup.NOT_SET;
    val expectedMedicationCategory = MedicationCategory.C_00;
    val expectedMedicationAmount = 0;
    val expectedQuantity = 1;

    val fileName = expectedID + ".xml";
    val originalEncoding = EncodingType.fromString(fileName);
    val flippedEncoding = EncodingUtil.flipEncoding(originalEncoding);

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(expectedID, kbvBundle.getLogicalId());
    assertEquals(
        KbvItaErpStructDef.BUNDLE.getVersionedUrl(KbvItaErpVersion.V1_0_2),
        kbvBundle.getFullMetaProfile());
    assertEquals(KbvItaErpStructDef.BUNDLE.getCanonicalUrl(), kbvBundle.getMetaProfile());
    assertEquals(expectedID, kbvBundle.getReference().getReference());
    assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, kbvBundle.getFlowType());
    assertEquals(expectedEmergencyFee, kbvBundle.hasEmergencyServicesFee());
    assertEquals(expectedInsuranceType, kbvBundle.getInsuranceType());
    assertEquals(expectedKvid, kbvBundle.getKvid());
    assertEquals(expectedGivenName, kbvBundle.getPatientGivenName());
    assertEquals(expectedFamilyName, kbvBundle.getFamilyName());
    assertEquals(expectedLastName, kbvBundle.getPatientFamilyName());
    assertEquals(expectedBirthDate, kbvBundle.getPatientBirthDate());
    assertEquals(expectedCity, kbvBundle.getPatientAddressCity());
    assertEquals(expectedPostal, kbvBundle.getPatientAddressPostalCode());
    assertEquals(expectedStreet, kbvBundle.getPatientAddressStreet());
    assertEquals(expectedCoverageIknr, kbvBundle.getCoverageIknr());
    assertEquals(expectedCoverageName, kbvBundle.getCoverageName());
    assertEquals(expectedCoverageKind, kbvBundle.getCoverageKind());
    assertEquals(expectedCoverageWop, kbvBundle.getCoverageWop().orElseThrow());
    assertEquals(expectedCoverageState, kbvBundle.getCoverageState());
    assertEquals(expectedCoveragePersonGroup, kbvBundle.getCoveragePersonGroup());
    assertEquals(expectedMedicationCategory, kbvBundle.getMedicationCategory());
    assertEquals(expectedMedicationAmount, kbvBundle.getMedicationAmount());
    assertEquals(expectedQuantity, kbvBundle.getDispenseQuantity());
    assertFalse(kbvBundle.isMultiple());
    assertFalse(
        kbvBundle
            .getAssignerOrganization()
            .isPresent()); // GKV Prescription does not have an assigner Organization

    val composition = kbvBundle.getComposition();
    val compProfile = composition.getMeta().getProfile().get(0).asStringValue();
    val expectedProfile = KbvItaErpStructDef.COMPOSITION.getVersionedUrl(KbvItaErpVersion.V1_0_2);
    assertEquals(expectedProfile, compProfile);

    // flip the encoding and check again
    val flippedContent = parser.encode(kbvBundle, flippedEncoding);
    val flippedKbvBundle = parser.decode(KbvErpBundle.class, flippedContent);

    assertEquals(kbvBundle.getLogicalId(), flippedKbvBundle.getLogicalId());
  }

  @Test
  void encodeSinglePkvKbvBundle() {
    val expectedID = "sdf6s75f-d959-43f0-8ac4-sd6f7sd6";
    val fileName = expectedID + ".xml";

    val expectedIknr = IKNR.from("168141347");
    val expectedName = "Bayerische Beamtenkrankenkasse";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    assertTrue(parser.isValid(content));
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    // PKV Prescription must have an assigner Organization
    assertTrue(kbvBundle.getAssignerOrganization().isPresent());
    val assignerOrg = kbvBundle.getAssignerOrganization().orElseThrow();
    assertEquals(
        PrescriptionFlowType.FLOW_TYPE_160,
        kbvBundle.getFlowType()); // which is actually wrong: PKV must have 200; but we're encoding
    // correctly
    assertEquals(expectedIknr, assignerOrg.getIknr());
    assertEquals(expectedName, assignerOrg.getName());
    kbvBundle.getKvid();
  }

  @Test
  void shouldChangePrescriptionId() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val expectedPrescriptionId = new PrescriptionId("160.100.000.000.037.28");
    val randomPrescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_160);
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
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
    when(mockPrescriptionId.getSystemAsString()).thenReturn("hello_world");

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    kbvBundle.setPrescriptionId(mockPrescriptionId);
    assertThrows(MissingFieldException.class, kbvBundle::getPrescriptionId);
  }

  @Test
  void testEncodingCodingSingleValidKbvBundleWitzPzn() {
    val expectedID = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val expectedPzn = "03507952";
    val expectedName = "Novaminsulfon 500 mg Lichtenstein 100 ml Tropf. N3";
    val expectedDosage = "bis zu 4mal täglich je 20-40 Tropfen";
    val expectedNumerator = "100 ml";

    assertEquals(expectedPzn, kbvBundle.getMedicationPzn());
    assertEquals(expectedName, kbvBundle.getMedicationName());
    assertEquals(expectedDosage, kbvBundle.getDosageInstruction());
    assertTrue(kbvBundle.isSubstitutionAllowed());
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

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.INGREDIENT, kbvBundle.getMedication().getMedicationType().orElseThrow());
    assertEquals("Ramipril", kbvBundle.getMedication().getIngredientText().orElseThrow());
    assertEquals("100 Stück", kbvBundle.getMedication().getAmountNumeratorString().orElseThrow());
    assertEquals("5 mg", kbvBundle.getMedication().getIngredientStrengthString().orElseThrow());
  }

  @Test
  void testEncodingSingleKbvBundleCompounding() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.COMPOUNDING, kbvBundle.getMedication().getMedicationType().orElseThrow());

    assertEquals("500 ml", kbvBundle.getMedication().getAmountNumeratorString().orElseThrow());
  }

  @Test
  void testEncodingSingleKbvBundleFreetext() {
    val expectedID = "4863d1fb-dc26-4680-bb35-018610d1749d";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.FREETEXT, kbvBundle.getMedication().getMedicationType().orElseThrow());
    assertFalse(kbvBundle.getMedication().getAmountNumeratorString().isPresent());
  }

  @Test
  void shouldEncodeSingleCompounding110Bundle() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertEquals(
        MedicationType.COMPOUNDING, kbvBundle.getMedication().getMedicationType().orElseThrow());
    assertFalse(kbvBundle.getMedication().getAmountNumeratorString().isPresent());
  }

  @Test
  void shouldFailOnMissingMedication() {
    val expectedID = "dae573db-54e3-4cb8-880d-0a46bea8aea1";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_0 + fileName);
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

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    assertTrue(kbvBundle.getPayorTypeOptional().isEmpty());
    assertThrows(MissingFieldException.class, kbvBundle::getPayorType);
  }

  @Test
  void testChangeOfDates() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";

    val fileName = expectedID + ".xml";
    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
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
  void testChangeOfCoverage() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";

    val fileName = expectedID + ".xml";
    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);

    IntStream.range(0, 5)
        .forEach(
            idx -> {
              val kbvBundle = parser.decode(KbvErpBundle.class, content);
              val newCoverage = CoverageBuilder.faker().build();

              val oldCoverage = kbvBundle.getCoverage();
              val oldIknr = oldCoverage.getPayorFirstRep().getIdentifier().getValue();
              val oldName = oldCoverage.getPayorFirstRep().getDisplay();

              kbvBundle.changeCoverage(newCoverage);
              val nc2 = kbvBundle.getCoverage();
              val newIknr = nc2.getPayorFirstRep().getIdentifier().getValue();
              val newName = nc2.getPayorFirstRep().getDisplay();

              // make sure the names are different: coverage has been changed
              assertNotEquals(oldIknr, newIknr);
              assertNotEquals(oldName, newName);

              // make sure the KBV Bundle is still valid
              val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
              assertTrue(result.isSuccessful());
            });
  }

  @Test
  void shouldReplaceOriginalPatient() {
    val expectedID = "sdf6s75f-d959-43f0-8ac4-sd6f7sd6";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);

    val originalPatient =
        kbvBundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
            .findFirst()
            .orElseThrow(() -> new MissingFieldException(kbvBundle.getClass(), "Patient"));

    val kbvPatient1 = kbvBundle.getPatient();
    val kbvPatient2 = kbvBundle.getPatient();
    assertNotEquals(originalPatient, kbvPatient1);
    assertEquals(kbvPatient1, kbvPatient2);

    // now check if deep copy
    kbvPatient1.addExtension("abc", new StringType("xyz"));
    assertEquals(1, kbvPatient2.getExtensionsByUrl("abc").size());
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
              KbvErpBundleBuilder.faker("X123456789", new PrescriptionId("160.002.362.150.600.45"))
                  .build();

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

  @RequiredArgsConstructor
  static class ConsumerExpectation<T> {
    private final Consumer<T> consumer;
    private final String name;
    private final boolean expectedResult;

    public static <T> ConsumerExpectation<T> from(
        Consumer<T> consumer, String name, boolean expectedResult) {
      return new ConsumerExpectation<>(consumer, name, expectedResult);
    }

    public void accept(T t) {
      consumer.accept(t);
    }
  }
}
