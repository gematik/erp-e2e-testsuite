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

package de.gematik.test.fuzzing.fhirfuzz.impl;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class ListFuzzerImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  private static ListFuzzerImpl<Coding> listFuzzer;

  Coding coding;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    val codingTypeFuzzer =
        fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext));
    listFuzzer = new ListFuzzerImpl<>(fuzzerContext, codingTypeFuzzer);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    coding = new Coding();
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(listFuzzer.getContext());
  }

  @RepeatedTest(REPETITIONS)
  void fuzz() {
    val codingTypeFuzzer =
        fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext));
    LinkedList<Coding> cod = new LinkedList<>();
    cod.add(codingTypeFuzzer.generateRandom());
    cod.add(codingTypeFuzzer.generateRandom());
    cod.add(codingTypeFuzzer.generateRandom());
    cod.add(codingTypeFuzzer.generateRandom());
    listFuzzer.fuzz(cod);
    assertNotNull(cod);
  }
}
