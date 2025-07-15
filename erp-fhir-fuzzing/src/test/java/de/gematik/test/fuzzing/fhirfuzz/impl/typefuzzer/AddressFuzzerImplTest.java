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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.IdFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.StringFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.UrlFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.PeriodFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class AddressFuzzerImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static AddressFuzzerImpl addressFuzzer;
  private static IdFuzzerImpl idFuzzer;
  private static UrlFuzzImpl urlFuzz;
  private static StringFuzzImpl stringFuzz;
  private Address address;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    addressFuzzer = new AddressFuzzerImpl(fuzzerContext);
    idFuzzer = fuzzerContext.getIdFuzzer();
    urlFuzz = fuzzerContext.getUrlFuzz();
    stringFuzz = fuzzerContext.getStringFuzz();
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    address = new Address();
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(addressFuzzer.getContext());
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertNotNull(addressFuzzer.generateRandom());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzType() {
    assertFalse(address.hasType());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasType());

    val test =
        fuzzerContext.getRandomOneOfClass(Address.AddressType.class, Address.AddressType.NULL);
    address.setType(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getType());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(address.hasText());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasText());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setText(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getText());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzLine() {
    assertFalse(address.hasLine());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasLine());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = List.of(new StringType(fuzzerContext.getStringFuzz().generateRandom(150)));
    address.setLine(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test.get(0).getValue(), address.getLine().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzCity() {
    assertFalse(address.hasCity());
    fuzzConfig.setPercentOfAll(00.0f);
    addressFuzzer.fuzz(address);
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setCity(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getCountry());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDistrict() {
    assertFalse(address.hasDistrict());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasDistrict());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setDistrict(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getDistrict());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzState() {
    assertFalse(address.hasState());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasState());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setState(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getState());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPostal() {
    assertFalse(address.hasPostalCode());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasPostalCode());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setPostalCode(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getPostalCode());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzCountry() {
    assertFalse(address.hasCountry());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasCountry());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test = stringFuzz.generateRandom(150);
    address.setCountry(test);
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test, address.getCountry());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPeriod() {
    assertFalse(address.hasPeriod());
    addressFuzzer.fuzz(address);
    assertTrue(address.hasPeriod());
    fuzzConfig.setPercentOfAll(100.0f);
    addressFuzzer.fuzz(address);
    val test =
        fuzzerContext
            .getTypeFuzzerFor(Period.class, () -> new PeriodFuzzerImpl(fuzzerContext))
            .generateRandom();
    address.setPeriod(test.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    addressFuzzer.fuzz(address);
    assertNotEquals(test.getEnd().getTime(), address.getPeriod().getEnd().getTime());
  }
}
