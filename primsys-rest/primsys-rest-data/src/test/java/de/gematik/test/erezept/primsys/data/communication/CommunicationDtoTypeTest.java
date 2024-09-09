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

package de.gematik.test.erezept.primsys.data.communication;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

import de.gematik.test.erezept.fhir.resources.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.ICommunicationType;
import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class CommunicationDtoTypeTest {

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldThrowOnInvalidNames(String name) {
    assertThrows(InvalidCodeValueException.class, () -> CommunicationDtoType.fromString(name));
  }

  static Stream<Arguments> shouldThrowOnInvalidNames() {
    return Stream.of("ABC", "").map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapFromTypeNames(String name, CommunicationDtoType expected) {
    val actual = assertDoesNotThrow(() -> CommunicationDtoType.fromString(name));
    assertEquals(expected, actual);
  }

  static Stream<Arguments> shouldMapFromTypeNames() {
    return Stream.of(
        of("INFO_REQ", CommunicationDtoType.INFO_REQ),
        of("COM_INFO_REQ", CommunicationDtoType.INFO_REQ),
        of("info_req", CommunicationDtoType.INFO_REQ),
        of("DISP_REQ", CommunicationDtoType.DISP_REQ),
        of("REPLY", CommunicationDtoType.REPLY),
        of("COM_REPLY", CommunicationDtoType.REPLY),
        of("COM_DISP_REQ", CommunicationDtoType.DISP_REQ),
        of("REPRESENTATIVE", CommunicationDtoType.REPRESENTATIVE),
        of("change_REQuest", CommunicationDtoType.CHANGE_REQ),
        of("COM_change_REPLY", CommunicationDtoType.CHANGE_REPLY));
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapFromFhirTypes(ICommunicationType<?> type, CommunicationDtoType expected) {
    val actual = assertDoesNotThrow(() -> CommunicationDtoType.fromString(type.name()));
    assertEquals(expected, actual);
  }

  static Stream<Arguments> shouldMapFromFhirTypes() {
    return Stream.of(
        of(CommunicationType.INFO_REQ, CommunicationDtoType.INFO_REQ),
        of(CommunicationType.DISP_REQ, CommunicationDtoType.DISP_REQ),
        of(CommunicationType.REPRESENTATIVE, CommunicationDtoType.REPRESENTATIVE),
        of(ChargeItemCommunicationType.CHANGE_REQ, CommunicationDtoType.CHANGE_REQ),
        of(ChargeItemCommunicationType.CHANGE_REPLY, CommunicationDtoType.CHANGE_REPLY));
  }
}
