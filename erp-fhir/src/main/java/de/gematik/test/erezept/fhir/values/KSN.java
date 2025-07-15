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

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

/** Krankenhausstandortnummer */
public class KSN extends SemanticValue<String, DeBasisProfilNamingSystem> {

  private KSN(String value) {
    super(DeBasisProfilNamingSystem.STANDORTNUMMER, value);
  }

  @Override
  public Identifier asIdentifier() {
    val baseIdentifier = super.asIdentifier();
    baseIdentifier
        .getType()
        .addCoding(
            new Coding()
                .setCode("KSN")
                .setSystem(KbvCodeSystem.BASE_IDENTIFIER_TYPE.getCanonicalUrl()));
    return baseIdentifier;
  }

  public static KSN from(String value) {
    return new KSN(value);
  }

  public static KSN random() {
    val faker = GemFaker.getFaker();
    return from(faker.regexify("[0-9]{9}"));
  }
}
