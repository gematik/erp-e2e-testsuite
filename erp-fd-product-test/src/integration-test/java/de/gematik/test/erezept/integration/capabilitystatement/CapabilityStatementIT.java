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

package de.gematik.test.erezept.integration.capabilitystatement;

import static org.junit.jupiter.api.Assertions.assertFalse;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.ResponseOfGetCapabilityStatement;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.GetCapabilityStatementCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxCapabilityStatement;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("CapabilityStatement Feature Test")
@Tag("CapabilityStatementResource")
class CapabilityStatementIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Test
  @TestcaseId("ERP_CAPABILITYSTATEMENT_01")
  @DisplayName("Software-Version und Release Date anhand der Capability Statement Resource abrufen")
  @Tag("SoftwareVersionAndDate")
  void getCapabilityStatementAndVerifySoftwareVersionAndDate() {
    val request = new GetCapabilityStatementCommand();
    val question = new ResponseOfGetCapabilityStatement();

    ErpInteraction<ErxCapabilityStatement> response = sina.performs(question);

    ErxCapabilityStatement capabilityStatement = response.getExpectedResponse();

    String softwareVersion = String.valueOf(capabilityStatement.getSoftwareVersion());
    log.info("Software-Version: {}", softwareVersion);

    String softwareReleaseDate = String.valueOf(capabilityStatement.getSoftwareReleaseDate());
    log.info("Software-Release-Date: {}", softwareReleaseDate);

    assertFalse(softwareVersion.isEmpty(), "Software version should not be empty");

    System.setProperty("test.software.version", softwareVersion);
  }
}
