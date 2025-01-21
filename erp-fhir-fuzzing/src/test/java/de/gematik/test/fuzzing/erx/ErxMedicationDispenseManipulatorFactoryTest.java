/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.fuzzing.erx;

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseManipulatorFactoryTest {

  private ErxMedicationDispense dispense;

  @BeforeEach
  void setUp() {
    dispense = createMedicationDispense();
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
