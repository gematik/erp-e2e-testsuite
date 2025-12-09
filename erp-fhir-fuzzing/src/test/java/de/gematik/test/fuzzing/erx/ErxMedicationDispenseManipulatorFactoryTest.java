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

package de.gematik.test.fuzzing.erx;

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationKombiPkgFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErxMedicationDispenseManipulatorFactoryTest extends ErpFhirParsingTest {

  private ErxMedicationDispense dispense;
  private ErxMedicationDispense medDSP;

  @BeforeEach
  void setUp() {
    dispense = createMedicationDispense();
    medDSP = getMedicationDispense();
  }

  @Test
  void privateConstructorShouldThrowAssertionError() {
    assertTrue(
        PrivateConstructorsUtil.isUtilityConstructor(ErxMedicationDispenseManipulatorFactory.class),
        "The private constructor should throw an AssertionError ");
  }

  @Test
  void setWhenHandedOverToSecondPrecisionTest() {
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getDateTimeManipulatorsWhenHandedOver();

    val manipulator = findManipulator(manipulators, "Set DateTime with second precision");
    assertNotNull(manipulator, "Manipulator for setting second precision should exist");

    manipulator.getParameter().accept(dispense);

    assertNotNull(
        dispense.getWhenHandedOver(), "WhenHandedOver should not be null after manipulation");
    assertEquals(
        TemporalPrecisionEnum.SECOND,
        dispense.getWhenHandedOverElement().getPrecision(),
        "Temporal precision should be SECOND");
  }

  @Test
  void setWhenHandedOverToFutureTest() {
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getDateTimeManipulatorsWhenHandedOver();

    val manipulator = findManipulator(manipulators, "Set DateTime to future (1 year later)");
    assertNotNull(manipulator, "Manipulator for setting future date should exist");

    manipulator.getParameter().accept(dispense);

    assertNotNull(
        dispense.getWhenHandedOver(), "WhenHandedOver should not be null after manipulation");
    assertTrue(
        dispense.getWhenHandedOver().after(new Date()), "WhenHandedOver should be in the future");
  }

  @Test
  void setWhenPreparedAfterWhenHandedOverTest() {
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getDateTimeManipulatorsWhenPrepared();

    val manipulator =
        findManipulator(manipulators, "Set WhenPrepared DateTime after WhenHandedOver");
    assertNotNull(manipulator, "Manipulator to set WhenPrepared after WhenHandedOver should exist");

    val whenHandedOver = new Date();
    dispense.setWhenHandedOver(whenHandedOver);

    manipulator.getParameter().accept(dispense);

    assertNotNull(dispense.getWhenPrepared(), "WhenPrepared should not be null after manipulation");
    assertTrue(
        dispense.getWhenPrepared().after(whenHandedOver),
        "WhenPrepared should be set to a date after WhenHandedOver");
  }

  @Test
  void setWhenHandedOverWithUnsupportedTemporalPrecisionTest() {
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getDateTimeManipulatorsWhenHandedOver();

    val manipulator =
        findManipulator(manipulators, "Set DateTime with unsupported TemopralPrecision");
    assertNotNull(
        manipulator, "Manipulator for setting unsupported TemporalPrecision should exist");

    manipulator.getParameter().accept(dispense);

    assertNotNull(
        dispense.getWhenHandedOver(), "WhenHandedOver should not be null after manipulation");
    assertEquals(TemporalPrecisionEnum.MILLI, dispense.getWhenHandedOverElement().getPrecision());
  }

  @Test
  void allManipulatorsExistTest() {
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getAllMedicationDispenseManipulators();

    assertNotNull(manipulators);
    assertFalse(manipulators.isEmpty(), "Manipulator list should not be empty");

    manipulators.forEach(
        manipulator -> {
          assertNotNull(manipulator.getName(), "Manipulator name should not be null");
          assertNotNull(manipulator.getParameter(), "Manipulator parameter should not be null");
        });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "shorten PrescriptionId-System to trigger Slice-Validation @ ErpFD",
        "set TelematikID instead of PrescriptionId-System to trigger Slice-Validation @ ErpFD",
        "switch PrescriptionId-System to trigger Slice-Validation @ ErpFD"
      })
  void shouldSetWrongPrescriptionId(String manipulatorDescription) {
    val manipulators = ErxMedicationDispenseManipulatorFactory.getSystemManipulator();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for " + manipulatorDescription);

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful());
    manipulator.getParameter().accept(medDSP);
    assertFalse(ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful());
    assertThrows(
        BuilderException.class,
        () -> medDSP.getPrescriptionId(),
        "No Fitting PrescriptionId-System contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "shorten actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
        "set SNOMED_SCT instead of Actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
        "set BSNR instead of Actors Telematik-Id-System to trigger Slice-Validation @ ErpFD",
        "switch Actors Telematik-Id-System to trigger Slice-Validation @ ErpFD"
      })
  void shouldSetWrongTelematikId(String manipulatorDescription) {
    val manipulators = ErxMedicationDispenseManipulatorFactory.getSystemManipulator();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for " + manipulatorDescription);

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful());
    manipulator.getParameter().accept(medDSP);
    assertFalse(ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful());
    assertNotEquals(
        "https://gematik.de/fhir/sid/telematik-id",
        medDSP.getPerformer().stream()
            .findFirst()
            .orElseThrow()
            .getActor()
            .getIdentifier()
            .getSystem(),
        "No Fitting Telematik-Id-System contained");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "switch KVID-System to trigger Slice-Validation @ ErpFD",
        "shorten KVID-System to trigger Slice-Validation @ ErpFD",
        "set Accident-System instead of KVID-System to trigger Slice-Validation @ ErpFD"
      })
  void shouldSetWrongKVID(String manipulatorDescription) {
    val manipulators = ErxMedicationDispenseManipulatorFactory.getSystemManipulator();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for wrong KVID should exist");

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful());
    manipulator.getParameter().accept(medDSP);
    val res = ValidatorUtil.encodeAndValidate(parser, medDSP).isSuccessful();
    assertFalse(res);
    assertNotEquals(
        "KVNR",
        medDSP.getSubject().getIdentifier().getSystem(),
        "No Fitting KVID-System contained");
  }

  /**
   * Utility method to create a medication dispense instance.
   *
   * @return A newly created ErxMedicationDispense object
   */
  private ErxMedicationDispense createMedicationDispense() {
    val md = new ErxMedicationDispense();
    md.setWhenPrepared(new Date());
    return md;
  }

  private ErxMedicationDispense getMedicationDispense() {
    return ErxMedicationDispenseBuilder.forKvnr(KVNR.random())
        .medication(new GemErpMedicationKombiPkgFaker().fake())
        .performerId(TelematikID.random())
        .prescriptionId(PrescriptionId.random())
        .build();
  }

  /**
   * Utility method to find a manipulator by name.
   *
   * @param manipulators List of NamedEnvelope objects
   * @param name The name of the manipulator to find
   * @return The NamedEnvelope if found, otherwise null
   */
  private NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> findManipulator(
      List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> manipulators, String name) {
    return manipulators.stream().filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
  }
}
