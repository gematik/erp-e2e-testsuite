/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class PatientBuilderTest {

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void buildGkvPatientWithFaker() {
    val patient = PatientBuilder.faker("X123123123", IdentifierTypeDe.GKV).build();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.GKV, insuranceKind);
  }

  @Test
  public void buildPkvPatientWithFaker() {
    val patient = PatientBuilder.faker("X123123123", IdentifierTypeDe.PKV).build();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.PKV, insuranceKind);
  }

  @Test
  public void shouldFailOnEmptyPatientBuilder() {
    val pb = PatientBuilder.builder();
    assertThrows(BuilderException.class, pb::build);
  }

  @Test
  public void shouldFailOnPkvPatientWithoutAssigner() {
    val pb =
        PatientBuilder.faker("X123123123", IdentifierTypeDe.GKV); // GKV Faker won't set assigner!
    pb.kvIdentifierDe("X123123123", IdentifierTypeDe.PKV); // setting PKV without assigner
    assertThrows(BuilderException.class, pb::build);
  }
}
