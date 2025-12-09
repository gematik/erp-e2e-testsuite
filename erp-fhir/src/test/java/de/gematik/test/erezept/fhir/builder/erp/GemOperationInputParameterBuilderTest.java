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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GemOperationInputParameterBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(
        PrivateConstructorsUtil.isUtilityConstructor(GemOperationInputParameterBuilder.class));
  }

  @Test
  void shouldBuildCloseOperationForDiGAMedicationDispense() {
    val medDispense =
        ErxMedicationDispenseDiGAFaker.builder()
            .withVersion(ErpWorkflowVersion.getDefaultVersion())
            .fake();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingDiGA()
            .with(medDispense)
            .version(ErpWorkflowVersion.getDefaultVersion())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildCloseOperationForMultipleDiGAMedicationDispense() {
    val version = ErpWorkflowVersion.V1_4;

    val medDispense = ErxMedicationDispenseDiGAFaker.builder().withVersion(version).fake();
    val medDispense2 = ErxMedicationDispenseDiGAFaker.builder().withVersion(version).fake();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingDiGA()
            .with(medDispense)
            .with(medDispense2)
            .version(version)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource("pharmaceuticalsParameterBuilderProvider")
  void shouldBuildInputOperationParametersForPharmaceuticalMedicationDispense(
      Supplier<GemDispenseCloseOperationPharmaceuticalsBuilder<?>> builderSupplier) {
    val version = ErpWorkflowVersion.V1_4;

    val medication = GemErpMedicationFaker.forPznMedication().withVersion(version).fake();
    val medDispense =
        ErxMedicationDispenseFaker.builder().withVersion(version).withMedication(medication).fake();

    val closeOperation =
        builderSupplier.get().version(version).with(medDispense, medication).build();

    assertTrue(parser.isValid(closeOperation));
  }

  @ParameterizedTest
  @MethodSource("pharmaceuticalsParameterBuilderProvider")
  void shouldBuildInputOperationParametersForSplittedPharmaceuticalMedicationDispense(
      Supplier<GemDispenseCloseOperationPharmaceuticalsBuilder<?>> builderSupplier) {
    val version = ErpWorkflowVersion.V1_4;

    val builder = builderSupplier.get();

    IntStream.range(0, 4)
        .forEach(
            i -> {
              val medication = GemErpMedicationFaker.forPznMedication().withVersion(version).fake();
              val medDispense =
                  ErxMedicationDispenseFaker.builder()
                      .withVersion(version)
                      .withMedication(medication)
                      .fake();

              builder.with(medDispense, medication);
            });

    val closeOperation = builder.version(version).build();
    assertTrue(parser.isValid(closeOperation));
  }

  @Test
  void shouldBuildEmptyCloseOperationForPharmaceuticals() {
    val version = ErpWorkflowVersion.getDefaultVersion();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingPharmaceuticals().version(version).build();

    assertTrue(parser.isValid(closeOperation));
  }

  @ParameterizedTest
  @MethodSource("dispenseParameterBuilderProvider")
  void shouldNotAllowEmptyDispenseOperation(Supplier<ResourceBuilder<?, ?>> builderSupplier) {
    val builder = builderSupplier.get();
    assertThrows(BuilderException.class, builder::build);
  }

  @ParameterizedTest
  @MethodSource("closeParameterBuilderProvider")
  void shouldAllowEmptyParametersForCloseOperation(
      Supplier<ResourceBuilder<?, ?>> builderSupplier) {
    val builder = builderSupplier.get();
    assertDoesNotThrow(builder::build);
  }

  static Stream<Arguments> closeParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forClosingPharmaceuticals,
            GemOperationInputParameterBuilder::forClosingDiGA)
        .map(Arguments::of);
  }

  static Stream<Arguments> dispenseParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forDispensingPharmaceuticals)
        .map(Arguments::of);
  }

  static Stream<Arguments> pharmaceuticalsParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forClosingPharmaceuticals,
            GemOperationInputParameterBuilder::forClosingPharmaceuticals)
        .map(Arguments::of);
  }
}
