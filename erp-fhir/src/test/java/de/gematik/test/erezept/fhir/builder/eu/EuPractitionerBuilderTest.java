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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

class EuPractitionerBuilderTest extends ErpFhirParsingTest {
  private final Identifier id =
      new Identifier().setValue("unknownDoctor Type").setSystem("https://www.unknownSystem.eu");

  @Test
  void shouldFailWhileMissingIdenfier() {
    val practitioner = EuPractitionerBuilder.buildPractitioner();
    assertThrows(BuilderException.class, practitioner::build);
  }

  @Test
  void shouldBuildSimpleDocCorrect() {
    val doctor = EuPractitionerBuilder.buildSimplePractitioner();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, doctor).isSuccessful());
  }

  @Test
  void shouldGetEuPractitionerFromEuPractitioner() {
    val doctor = EuPractitionerBuilder.buildSimplePractitioner();
    val euDoc = EuPractitioner.fromPractitioner(doctor);
    assertTrue(ValidatorUtil.encodeAndValidate(parser, euDoc).isSuccessful());
  }

  @Test
  void shouldBuildEuPractitionerCorrect() {
    val practitioner = EuPractitionerBuilder.buildPractitioner().identifier(id).build();
    assertNotNull(practitioner);
    assertNotNull(practitioner.getMeta().getProfile());
    assertNotNull(practitioner.getName().get(0).getFamily());
    assertNotNull(practitioner.getIdentifier());
    assertNotNull(practitioner.getIdentifier().get(0).getValue());
    assertEquals("unknownDoctor Type", practitioner.getIdentifier().get(0).getValue());
    assertEquals("https://www.unknownSystem.eu", practitioner.getIdentifier().get(0).getSystem());
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldSetNameCorrect() {
    val name =
        new HumanName()
            .setFamily("Bone")
            .setGiven(List.of(new StringType("Drill"), new StringType("Ad")));
    val pract = EuPractitionerBuilder.buildPractitioner(name).identifier(id).build();

    assertNotNull(pract.getName().get(0).getFamily());
    assertEquals("Bone", pract.getName().get(0).getFamily());
  }

  @Test
  void shouldSetVersionCorrect() {
    val practitioner =
        EuPractitionerBuilder.buildPractitioner().identifier(id).version(EuVersion.V1_0).build();
    assertTrue(practitioner.getMeta().getProfile().get(0).asStringValue().contains("|1.0"));
    val result = ValidatorUtil.encodeAndValidate(parser, practitioner);
    assertTrue(result.isSuccessful());
  }
}
