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

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;

class ErxCapabilityStatementTest {

  @Test
  void shouldReturnSoftwareVersionWhenPresent() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    CapabilityStatementSoftwareComponent software = new CapabilityStatementSoftwareComponent();
    software.setVersion("1.18.0");
    cs.setSoftware(software);

    assertEquals("1.18.0", cs.getSoftwareVersion());
  }

  @Test
  void shouldThrowExceptionWhenSoftwareVersionMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    assertThrows(MissingFieldException.class, cs::getSoftwareVersion);
  }

  @Test
  void shouldReturnSoftwareNameWhenPresent() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    CapabilityStatementSoftwareComponent software = new CapabilityStatementSoftwareComponent();
    software.setName("E-Rezept");
    cs.setSoftware(software);

    assertEquals("E-Rezept", cs.getSoftwareName());
  }

  @Test
  void shouldThrowExceptionWhenSoftwareNameMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    cs.setSoftware(new CapabilityStatementSoftwareComponent());

    assertThrows(MissingFieldException.class, cs::getSoftwareName);
  }

  @Test
  void shouldThrowExceptionWhenSoftwareNameComponentMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    assertThrows(MissingFieldException.class, cs::getSoftwareName);
  }

  @Test
  void shouldReturnSoftwareReleaseDateWhenPresent() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    CapabilityStatementSoftwareComponent software = new CapabilityStatementSoftwareComponent();
    software.setReleaseDateElement(new DateTimeType("2025-06-03T09:19:19.000+00:00"));
    cs.setSoftware(software);

    assertEquals("2025-06-03T09:19:19.000+00:00", cs.getSoftwareReleaseDate());
  }

  @Test
  void shouldThrowExceptionWhenSoftwareReleaseDateMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    cs.setSoftware(new CapabilityStatementSoftwareComponent());

    assertThrows(MissingFieldException.class, cs::getSoftwareReleaseDate);
  }

  @Test
  void shouldThrowExceptionWhenSoftwareReleaseDateComponentMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    assertThrows(MissingFieldException.class, cs::getSoftwareReleaseDate);
  }

  @Test
  void shouldReturnImplementationDescriptionWhenPresent() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    CapabilityStatementImplementationComponent impl =
        new CapabilityStatementImplementationComponent();
    impl.setDescription("Test Implementation");
    cs.setImplementation(impl);

    assertEquals("Test Implementation", cs.getImplementationDescription());
  }

  @Test
  void shouldThrowExceptionWhenImplementationDescriptionMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();
    cs.setImplementation(new CapabilityStatementImplementationComponent());

    assertThrows(MissingFieldException.class, cs::getImplementationDescription);
  }

  @Test
  void shouldThrowExceptionWhenImplementationComponentMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();

    assertThrows(MissingFieldException.class, cs::getImplementationDescription);
  }

  @Test
  void shouldReturnNullFhirVersionWhenMissing() {
    ErxCapabilityStatement cs = new ErxCapabilityStatement();

    assertNull(cs.getFhirVersion());
  }
}
