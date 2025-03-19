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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import java.util.Objects;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

/** <a href="https://de.wikipedia.org/wiki/Betriebsst%C3%A4ttennummer">Betriebsstättennummer</a> */
public class BSNR extends SemanticValue<String, KbvNamingSystem> {

  private static final HL7CodeSystem CODE_SYSTEM = HL7CodeSystem.HL7_V2_0203;
  private static final String CODE = IdentifierTypeDe.BSNR.getCode();

  public BSNR(String value) {
    super(KbvNamingSystem.BASE_BSNR, value);
  }

  public static BSNR random() {
    return new BSNR(GemFaker.fakerBsnr());
  }

  public static BSNR from(String value) {
    return new BSNR(value);
  }

  public Identifier asIdentifier() {
    val id = super.asIdentifier();
    id.getType().addCoding(new Coding().setCode(CODE).setSystem(CODE_SYSTEM.getCanonicalUrl()));
    return id;
  }

  public static HL7CodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static String getCodeSystemUrl() {
    return CODE_SYSTEM.getCanonicalUrl();
  }

  public static String getCode() {
    return CODE;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final BSNR bsnr = (BSNR) o;
    return Objects.equals(getValue(), bsnr.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public String toString() {
    return format("BSNR: {0}", this.getValue());
  }
}
