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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ErpWorkflowVersionTest {

  @ParameterizedTest
  @MethodSource
  void getDefaultVersionViaCurrentDate(ErpWorkflowVersion version, LocalDate testDate) {
    try (MockedStatic<LocalDate> mockedStatic =
        Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
      mockedStatic.when(LocalDate::now).thenReturn(testDate);
      val defaultVersion = ErpWorkflowVersion.getDefaultVersion();
      assertEquals(version, defaultVersion);
    }
  }

  static Stream<Arguments> getDefaultVersionViaCurrentDate() {
    return Stream.of(
        Arguments.of(ErpWorkflowVersion.V1_1_1, LocalDate.of(2022, 1, 1)),
        Arguments.of(ErpWorkflowVersion.V1_2_0, LocalDate.of(2024, 8, 1)),
        Arguments.of(ErpWorkflowVersion.V1_2_0, LocalDate.of(2024, 10, 31)),
        Arguments.of(ErpWorkflowVersion.V1_3_0, LocalDate.of(2024, 11, 1)),
        Arguments.of(ErpWorkflowVersion.V1_3_0, LocalDate.of(2025, 1, 15)));
  }

  @Test
  void getDefaultVersionViaSystemProperty() {
    val propertyName = CustomProfiles.GEM_ERP_WORKFLOW.getName();
    Arrays.stream(ErpWorkflowVersion.values())
        .forEach(
            version -> {
              System.setProperty(propertyName, version.getVersion());
              val defaultVersion = ErpWorkflowVersion.getDefaultVersion();
              assertEquals(version, defaultVersion);
            });
  }

  @AfterEach
  void cleanProperties() {
    val propertyName = CustomProfiles.GEM_ERP_WORKFLOW.getName();
    System.clearProperty(propertyName);
  }
}
