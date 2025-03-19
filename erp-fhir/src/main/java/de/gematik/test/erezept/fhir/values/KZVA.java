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
 */

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;

public class KZVA extends SemanticValue<String, DeBasisProfilNamingSystem> {

  private KZVA(String value) {
    super(DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID, value);
  }

  @Override
  public Identifier asIdentifier() {
    val id = super.asIdentifier();
    id.getType().addCoding(IdentifierTypeDe.KZVA.asCoding());
    return id;
  }

  public static KZVA from(String value) {
    return new KZVA(value);
  }

  public static KZVA random() {
    val faker = GemFaker.getFaker();
    return from(faker.regexify("[0-9]{9}"));
  }
}
