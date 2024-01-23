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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

class MetaFuzzerImplTest {

  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  private static MetaFuzzerImpl metaFuzzer;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    metaFuzzer = new MetaFuzzerImpl(fuzzerContext);
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMetaVersion() {
    val meta = new Meta();
    var teststring = "12387973214ß0819230987231";
    meta.setVersionId(teststring);
    metaFuzzer.fuzz(meta);
    assertFalse(meta.getVersionId().isEmpty());
    assertNotEquals(teststring, meta.getVersionId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldSetSource() {
    val meta = new Meta();
    var testString = "http://www.aroundTheWorld/aroundTheWorld/aroundThew/";
    meta.setSource(testString);
    metaFuzzer.fuzz(meta);
    val log =
        fuzzerContext.getOperationLogs().stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    assertNotEquals(testString, meta.getSource());
    assertTrue(log.contains("Source"));
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzProfileRef() {
    val meta = new Meta();
    var testString = "http://www.aroundTheWorld/aroundTheWorld/aroundThew/";
    var testString2 = "https://www.aroundTheJupiter/aroundTheJupiterWorld/aroundThew/";
    var testString3 = "http://www.aroundTheSun/aroundTheSun/aroundThe/";
    CanonicalType canonical = new CanonicalType(testString);
    CanonicalType canonical2 = new CanonicalType(testString2);
    CanonicalType canonical3 = new CanonicalType(testString3);
    List<CanonicalType> profiles = new ArrayList<>();
    profiles.add(canonical);
    profiles.add(canonical2);
    profiles.add(canonical3);
    meta.setProfile(profiles);
    val teststring = profiles.get(0).getValue();
    metaFuzzer.fuzz(meta);
    val log =
        fuzzerContext.getOperationLogs().stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    assertNotEquals(teststring, meta.getProfile().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMetaTags() {
    val meta = new Meta();
    var testString = "@NewBee";
    var testString2 = "@Doctor";
    var testString3 = "@RepeatedTest(REPETITIONS)er";
    Coding coding =
        new Coding(
            "http//codingsys1", testString, "Very Useless but @ The Moment needful to test it");
    Coding coding2 =
        new Coding(
            "http//codingsys2", testString2, "Very Useless but @ The Moment needful to test it");
    Coding coding3 =
        new Coding(
            "http//codingsys3", testString3, "Very Useless but @ The Moment needful to test it");
    List<Coding> c = new ArrayList<>();
    c.add(coding);
    c.add(coding2);
    c.add(coding3);
    meta.setTag(c);
    metaFuzzer.fuzz(meta);
    val log =
        fuzzerContext.getOperationLogs().stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    assertNotEquals(testString, meta.getTag().get(0).getCode());
  }
}
