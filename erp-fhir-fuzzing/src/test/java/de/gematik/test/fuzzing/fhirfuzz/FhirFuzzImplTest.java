/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.fuzzing.fhirfuzz;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class FhirFuzzImplTest extends ParsingTest {
  private static final String TESTSTRING = "very short TestSTRING";
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  private static FhirFuzzImpl fhirFuzz;
  private Bundle bundle;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzerContext = new FuzzerContext(fuzzConfig);
    fhirFuzz = new FhirFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    bundle = new Bundle();
  }

  @Test
  void getContext() {
    assertNotNull(fhirFuzz.getContext());
  }

  @Test
  void shouldFuzzIdentifier() {
    assertFalse(bundle.hasIdentifier());
    fhirFuzz.fuzz(bundle);
    assertTrue(bundle.hasIdentifier());
    fhirFuzz.fuzz(bundle);
    Identifier identifier = new Identifier();
    val teststring = "123.345.5678";
    bundle.setIdentifier(identifier.setSystem(teststring));
    fuzzConfig.setPercentOfAll(0.00f);
    fhirFuzz.fuzz(bundle);
    assertNotEquals(teststring, bundle.getIdentifier().getSystem());
  }

  @Test
  void shouldFuzzId() {
    assertFalse(bundle.hasId());
    fhirFuzz.fuzz(bundle);
    assertTrue(bundle.hasId());
    fhirFuzz.fuzz(bundle);
    val teststring = "123.345.5678";
    bundle.setId(teststring);
    fuzzConfig.setPercentOfAll(0.00f);
    fhirFuzz.fuzz(bundle);
    assertNotEquals(teststring, bundle.getId());
    assertTrue(
        fuzzerContext.getOperationLogs().stream()
            .map(entry -> entry.toString().contains(("set ID in Bundle:")))
            .findAny()
            .isPresent());
  }

  @Test
  void shouldFuzzType() {
    assertFalse(bundle.hasType());
    fhirFuzz.fuzz(bundle);
    assertTrue(bundle.hasType());
    val type = fuzzerContext.getRandomOneOfClass(Bundle.BundleType.class, Bundle.BundleType.NULL);
    val typeAsString = type.toString();
    bundle.setType(type);
    fuzzConfig.setPercentOfAll(0.00f);
    fhirFuzz.fuzz(bundle);
    assertNotEquals(typeAsString, bundle.getType().toString());
    assertTrue(
        fuzzerContext.getOperationLogs().stream()
            .map(entry -> entry.toString().contains(("fuzz Type in Bundle:")))
            .findAny()
            .isPresent());
  }

  @Test
  void shouldFuzzMeta() {
    assertFalse(bundle.hasMeta());
    fhirFuzz.fuzz(bundle);
    assertTrue(bundle.hasMeta());
    assertNotNull(bundle.getMeta());
    fhirFuzz.fuzz(bundle);
    val meta =
        fuzzerContext
            .getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext))
            .generateRandom();
    bundle.setMeta(meta.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    fhirFuzz.fuzz(bundle);
    assertNotEquals(meta.getProfile(), bundle.getMeta().getProfile());
    assertTrue(
        fuzzerContext.getOperationLogs().stream()
            .map(entry -> entry.toString().contains(("set Meta in Bundle:")))
            .findAny()
            .isPresent());
  }

  @Test
  void shouldFuzzLanguage() {
    assertFalse(bundle.hasLanguage());
    fhirFuzz.fuzz(bundle);
    assertTrue(bundle.hasLanguage());
    fhirFuzz.fuzz(bundle);
    val lang = fuzzerContext.getStringFuzz().generateRandom(150);
    bundle.setLanguage(lang);
    fuzzConfig.setPercentOfAll(0.00f);
    fhirFuzz.fuzz(bundle);
    assertNotEquals(lang, bundle.getLanguage());
    assertTrue(
        fuzzerContext.getOperationLogs().stream()
            .map(entry -> entry.toString().contains(("set Language in Bundle:")))
            .findAny()
            .isPresent());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzUntilInvalidTrackValidation() {
    val bundle = KbvErpBundleBuilder.faker().build();
    fhirFuzz.getContext().getFuzzConfig().setUseAllMutators(true);
    fhirFuzz.getContext().getFuzzConfig().setUsedPercentOfMutators(0.002f);
    fhirFuzz.getContext().getFuzzConfig().setPercentOfAll(100f);
    fhirFuzz.getContext().getFuzzConfig().setPercentOfEach(100f);
    assertTrue(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());
    fhirFuzz.fuzzTilInvalid(bundle, parser);
    assertFalse(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzUntilInvalidTrackCounter() {
    val bundle = KbvErpBundleBuilder.faker().build();
    fhirFuzz.getContext().getFuzzConfig().setUseAllMutators(false);
    fhirFuzz.getContext().getFuzzConfig().setUsedPercentOfMutators(0.002f);
    fhirFuzz.getContext().getFuzzConfig().setPercentOfAll(0f);
    fhirFuzz.getContext().getFuzzConfig().getDetailSetup().clear();
    fhirFuzz.getContext().getFuzzConfig().setPercentOfEach(0f);

    val mockPrarser = mock(FhirParser.class);
    when(mockPrarser.encode(any(), any())).thenReturn("is´so");
    when(mockPrarser.isValid(anyString())).thenReturn(true);
    assertTrue(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());
    fhirFuzz.fuzzTilInvalid(bundle, mockPrarser);
    verify(mockPrarser, times(25)).isValid(anyString());
  }
}
