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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class ICommunicationTypeTest {

  @Test
  void shouldGetCommunicationTypeFromUrl() {
    List<ICommunicationType<?>> types = Arrays.asList(CommunicationType.values());
    types.forEach(
        type -> {
          val url = type.getTypeUrl();
          assertEquals(type, ICommunicationType.fromUrl(url));
        });
  }

  @ParameterizedTest(name = "[{index}] -> Create CommunicationType from StructureDefinition {0}")
  @EnumSource(
      value = ErpWorkflowStructDef.class,
      names = {"COM_INFO_REQ", "COM_DISP_REQ", "COM_REPLY", "COM_REPRESENTATIVE"})
  void shouldGetNewCommunicationTypesFromUrl(ErpWorkflowStructDef structDef) {
    val url = structDef.getCanonicalUrl();
    assertDoesNotThrow(() -> ICommunicationType.fromUrl(url));
  }

  @Test
  void shouldThrowOnInvalidFromUrl() {
    assertThrows(InvalidCommunicationType.class, () -> ICommunicationType.fromUrl("invalid_url"));
  }
}
