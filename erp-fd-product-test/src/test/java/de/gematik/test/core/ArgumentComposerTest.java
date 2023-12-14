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

package de.gematik.test.core;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.exceptions.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ArgumentComposerTest {

  @Test
  void shouldCreateArgumentsStreamFromLists() {
    val arguments =
        ArgumentComposer.composeWith()
            .arguments("A", 0, 0.0)
            .arguments("B", 1, 0.1)
            .arguments("C", 2, 0.2)
            .create()
            .toList();
    val expectedRows = 3;
    assertEquals(expectedRows, arguments.size());

    val firstArgs = arguments.get(0).get();
    assertEquals("A", firstArgs[0]);
    assertEquals(0, firstArgs[1]);
    assertEquals(0.0, firstArgs[2]);

    val lastArgs = arguments.get(expectedRows - 1).get();
    assertEquals("C", lastArgs[0]);
    assertEquals(2, lastArgs[1]);
    assertEquals(0.2, lastArgs[2]);

    //    arguments.forEach(
    //        args -> System.out.println(format("Arguments: ({0}, {1}, {2})", args.get())));
  }

  @Test
  void shouldVerticallyCreateArgumentsStream() {
    val arguments =
        ArgumentComposer.composeWith()
            .arguments(List.of("A", "B", "C", "D"))
            .arguments(0, 1, 2, 3)
            .arguments(0.0, 0.1, 0.2, 0.3)
            .rotated()
            .toList();
    val expectedRows = 4;
    assertEquals(expectedRows, arguments.size());

    val firstArgs = arguments.get(0).get();
    assertEquals("A", firstArgs[0]);
    assertEquals(0, firstArgs[1]);
    assertEquals(0.0, firstArgs[2]);

    val lastArgs = arguments.get(expectedRows - 1).get();
    assertEquals("D", lastArgs[0]);
    assertEquals(3, lastArgs[1]);
    assertEquals(0.3, lastArgs[2]);

    //    arguments.forEach(
    //        args -> System.out.println(format("Arguments: ({0}, {1}, {2})", args.get())));
  }

  @Test
  void shouldThrowOnDifferentSizedInnerLists() {
    val b = ArgumentComposer.composeWith().arguments("A").arguments(0, 1);
    assertThrows(ArgumentBuilderException.class, b::create);
    assertThrows(ArgumentBuilderException.class, b::rotated);
  }

  @Test
  void shouldThrowOnEmptyInnerLists() {
    val b = ArgumentComposer.composeWith().arguments(List.of()).arguments();
    assertThrows(ArgumentBuilderException.class, b::create);
    assertThrows(ArgumentBuilderException.class, b::rotated);
  }

  @Test
  void shouldThrowOnEmptyOuterList() {
    val b1 = ArgumentComposer.composeWith().arguments(List.of());
    assertThrows(ArgumentBuilderException.class, b1::create);
    assertThrows(ArgumentBuilderException.class, b1::rotated);

    val b2 = ArgumentComposer.composeWith().arguments();
    assertThrows(ArgumentBuilderException.class, b2::create);
    assertThrows(ArgumentBuilderException.class, b2::rotated);

    val b3 = ArgumentComposer.composeWith();
    assertThrows(ArgumentBuilderException.class, b3::create);
    assertThrows(ArgumentBuilderException.class, b3::rotated);
  }

  @Test
  void shouldExtendExistingStream() {
    val existing =
        ArgumentComposer.composeWith()
            .arguments("A", 0, 0.0)
            .arguments("B", 1, 0.1)
            .arguments("C", 2, 0.2)
            .create();

    val arguments =
        ArgumentComposer.composeWith(existing)
            .prepend(TestEnum4.TEN, TestEnum3.ONE)
            .append("XYZ", "ZYX")
            .add(2, 888, 999)
            .create()
            .toList();
    val expectedRows = 3;
    assertEquals(expectedRows, arguments.size());
    arguments.forEach(
        args -> {
          assertEquals(9, args.get().length);
          assertEquals(TestEnum4.TEN, args.get()[0]);
          assertEquals(TestEnum3.ONE, args.get()[1]);
          assertEquals(888, args.get()[2]);
          assertEquals(999, args.get()[3]);
          // 4...6 contain the values from the original stream
          assertEquals("XYZ", args.get()[7]);
          assertEquals("ZYX", args.get()[8]);
        });
  }

  @Test
  void cartesian() {
    val existing =
        ArgumentComposer.composeWith()
            .arguments("A", 0, 0.0)
            .arguments("B", 1, 0.1)
            .arguments("C", 2, 0.2)
            .create();

    val arguments =
        ArgumentComposer.composeWith(existing)
            .multiply(List.of(TestEnum4.TEN, TestEnum4.ELEVEN))
            .create()
            .toList();

    val expectedRows = 3 * 2;
    assertEquals(expectedRows, arguments.size());

    val row1 = arguments.get(0).get();
    assertEquals(TestEnum4.TEN, row1[0]);
    assertEquals("A", row1[1]);
    assertEquals(0, row1[2]);
    assertEquals(0.0, row1[3]);

    val row4 = arguments.get(3).get();
    assertEquals(TestEnum4.ELEVEN, row4[0]);
    assertEquals("A", row4[1]);
    assertEquals(0, row4[2]);
    assertEquals(0.0, row4[3]);
  }

  @Test
  void cartesianWithEnum() {
    val existing =
        ArgumentComposer.composeWith()
            .arguments("A", 0, 0.0)
            .arguments("B", 1, 0.1)
            .arguments("C", 2, 0.2)
            .create();

    val arguments =
        ArgumentComposer.composeWith(existing).multiply(TestEnum4.class).create().toList();

    val expectedRows = 3 * TestEnum4.values().length;
    assertEquals(expectedRows, arguments.size());

    val row1 = arguments.get(0).get();
    assertEquals(TestEnum4.TEN, row1[0]);
    assertEquals("A", row1[1]);
    assertEquals(0, row1[2]);
    assertEquals(0.0, row1[3]);

    val row7 = arguments.get(6).get();
    assertEquals(TestEnum4.TWELVE, row7[0]);
    assertEquals("A", row7[1]);
    assertEquals(0, row7[2]);
    assertEquals(0.0, row7[3]);
  }

  @Test
  void shouldCreateCartesianProduct() {
    val l1 = List.of("A", "B", "C", "D");
    val l2 = List.of(0, 1, 2, 3);
    val expectedRows = l1.size() * l2.size();

    val cartesian = ArgumentComposer.composeWith(l1).multiply(l2).create().toList();

    assertEquals(expectedRows, cartesian.size());

    for (var i = 0; i < l1.size(); i++) {
      for (var j = 0; j < l2.size(); j++) {
        val cartesianIdx = (i * l2.size()) + j;
        val actualArgs = cartesian.get(cartesianIdx).get();

        assertEquals(2, actualArgs.length);
        val first = actualArgs[0];
        val second = actualArgs[1];
        assertEquals(l2.get(i), first);
        assertEquals(l1.get(j), second);
      }
    }

    //    cartesian.forEach(args -> System.out.println(format("Arguments: ({0}, {1})",
    // args.get())));
  }

  @Test
  void shouldCreateExtendedCartesianProduct() {
    val l1 = List.of("A", "B", "C", "D");
    val l2 = List.of(0, 1, 2, 3);
    val l3 = List.of(0.1, 0.2, 0.3);
    val l4 = List.of("abc", "def");
    val expectedRows = l1.size() * l2.size() * l3.size() * l4.size();
    val cartesian =
        ArgumentComposer.composeWith(l4).multiply(l3).multiply(l2).multiply(l1).create().toList();

    assertEquals(expectedRows, cartesian.size());

    val firstArgs = cartesian.get(0).get();
    assertEquals("A", firstArgs[0]);
    assertEquals(0, firstArgs[1]);
    assertEquals(0.1, firstArgs[2]);
    assertEquals("abc", firstArgs[3]);

    val lastArgs = cartesian.get(expectedRows - 1).get();
    assertEquals("D", lastArgs[0]);
    assertEquals(3, lastArgs[1]);
    assertEquals(0.3, lastArgs[2]);
    assertEquals("def", lastArgs[3]);

    //    cartesian.forEach(
    //        args -> System.out.println(format("Arguments: ({0}, {1}, {2}, {3})",
    //   args.get())));
  }

  @Test
  void cartesianProductWithEnum() {
    val l1 = List.of("A", "B", "C", "D");
    val expectedRows = l1.size() * PrescriptionFlowType.values().length;
    val cartesian =
        ArgumentComposer.composeWith(PrescriptionFlowType.class).multiply(l1).create().toList();
    assertEquals(expectedRows, cartesian.size());

    val firstArgs = cartesian.get(0).get();
    assertEquals("A", firstArgs[0]);
    assertEquals(PrescriptionFlowType.values()[0], firstArgs[1]);

    val lastArgs = cartesian.get(expectedRows - 1).get();
    assertEquals("D", lastArgs[0]);
    assertEquals(
        PrescriptionFlowType.values()[PrescriptionFlowType.values().length - 1], lastArgs[1]);
  }

  @Test
  void cartesianProductWithEnumMultiplyAppend() {
    val l1 = List.of("A", "B", "C", "D");
    val expectedRows = l1.size() * PrescriptionFlowType.values().length;
    val cartesian =
        ArgumentComposer.composeWith(PrescriptionFlowType.class)
            .multiplyAppend(l1)
            .create()
            .toList();
    assertEquals(expectedRows, cartesian.size());

    val firstArgs = cartesian.get(0).get();
    assertEquals("A", firstArgs[1]);
    assertEquals(PrescriptionFlowType.values()[0], firstArgs[0]);

    val lastArgs = cartesian.get(expectedRows - 1).get();
    assertEquals("D", lastArgs[1]);
    assertEquals(
        PrescriptionFlowType.values()[PrescriptionFlowType.values().length - 1], lastArgs[0]);
  }

  @Test
  void shouldExcludeFromCreateEnumList() {
    val l1 = ArgumentComposer.composeWith(TestEnum.class, TestEnum.A, TestEnum.D).create().toList();
    assertEquals(2, l1.size());
    assertEquals(TestEnum.B, l1.get(0).get()[0]);
    assertEquals(TestEnum.C, l1.get(1).get()[0]);
  }

  @Test
  void shouldCreateCartesianFromTwoEnums() {
    val cartesian =
        ArgumentComposer.composeWith(TestEnum2.class)
            .multiply(TestEnum.class)
            .create()
            .collect(Collectors.toList());
    val expectedRows = TestEnum.values().length * TestEnum2.values().length;
    assertEquals(expectedRows, cartesian.size());
  }

  @Test
  void shouldCreateCartesianFromEnums() {
    val cartesian =
        ArgumentComposer.composeWith(TestEnum4.class)
            .multiply(TestEnum3.class)
            .multiplyAppend(TestEnum2.class, TestEnum2.X)
            .multiplyAppend(TestEnum.class)
            .create()
            .toList();
    val expectedRows =
        TestEnum.values().length
            * (TestEnum2.values().length - 1) // excluded TestEnum2.X
            * TestEnum3.values().length
            * TestEnum4.values().length;
    assertEquals(expectedRows, cartesian.size());
  }

  @Test
  void shouldThrowOnMultiplyWithEmptyList() {
    val composer = ArgumentComposer.composeWith(TestEnum4.class);
    val emptyMultiplier = List.of();
    assertThrows(ArgumentBuilderException.class, () -> composer.multiply(emptyMultiplier));
  }

  @Test
  void shouldThrowOnMultiplyWithNull() {
    val composer = ArgumentComposer.composeWith(TestEnum4.class);
    List<String> emptyMultiplier = null;
    assertThrows(ArgumentBuilderException.class, () -> composer.multiply(emptyMultiplier));
  }

  enum TestEnum {
    A,
    B,
    C,
    D
  }

  enum TestEnum2 {
    X,
    Y,
    Z
  }

  enum TestEnum3 {
    ONE,
    TWO,
    THREE
  }

  enum TestEnum4 {
    TEN,
    ELEVEN,
    TWELVE
  }
}
