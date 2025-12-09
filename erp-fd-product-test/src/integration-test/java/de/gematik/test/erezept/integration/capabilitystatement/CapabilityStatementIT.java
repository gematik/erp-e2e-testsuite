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

import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_CONFIG;
import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.ResponseOfGetCapabilityStatement;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.PatientActor;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.model.buildinfo.BuildInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("CapabilityStatement Feature Test")
@Tag("CapabilityStatementResource")
@Tag("Smoketest")
class CapabilityStatementIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Test
  @TestcaseId("ERP_CAPABILITYSTATEMENT_01")
  @DisplayName("Software-Version und Release Date anhand der Capability Statement Resource abrufen")
  @Tag("SoftwareVersionAndDate")
  void getCapabilityStatementAndVerifySoftwareVersionAndDate() {
    val response = sina.asksFor(ResponseOfGetCapabilityStatement.request());
    sina.attemptsTo(Verify.that(response).isFromExpectedType());

    val capabilityStatement = response.getExpectedResponse();

    val softwareName = capabilityStatement.getSoftwareName();
    assertFalse(softwareName.isEmpty());
    BuildInfo.section("Fachdienst").setProperty("Software", softwareName);

    val softwareVersion = capabilityStatement.getSoftwareVersion();
    assertFalse(softwareVersion.isEmpty(), "Software version should not be empty");
    BuildInfo.section("Fachdienst").setProperty("Software Version", softwareVersion);

    val softwareReleaseDate = capabilityStatement.getSoftwareReleaseDate();
    BuildInfo.section("Fachdienst").setProperty("Software Release", softwareReleaseDate);

    // write the configured FHIR versions
    val tsSection = BuildInfo.section("Testsuite FHIR Konfiguration");
    val conf =
        ProfilesConfigurator.getConfiguration(ERP_FHIR_PROFILES_CONFIG, ERP_FHIR_PROFILES_TOGGLE);
    conf.getDefaultProfile()
        .getProfiles()
        .forEach(p -> tsSection.setProperty(p.getName(), p.getVersion()));
  }
}
