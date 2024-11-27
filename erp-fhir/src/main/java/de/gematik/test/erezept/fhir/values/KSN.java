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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

/** Krankenhausstandortnummer */
public class KSN extends Value<String> {

  private KSN(String value) {
    super(DeBasisNamingSystem.STANDORTNUMMER, value);
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
