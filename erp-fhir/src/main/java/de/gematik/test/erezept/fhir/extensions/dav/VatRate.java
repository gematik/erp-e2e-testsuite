/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.extensions.dav;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import lombok.Getter;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;

@Getter
@DatatypeDef(name = "Extension")
@SuppressWarnings({"java:S110"})
public class VatRate extends Extension {

  public static final AbdaErpBasisStructDef STRUCTURE_DEFINITION = AbdaErpBasisStructDef.MWST_SATZ;

  private VatRate(float primitiveValue) {
    super(STRUCTURE_DEFINITION.getCanonicalUrl());
    this.setValue(new DecimalType(String.format(java.util.Locale.US, "%.02f", primitiveValue)));
  }

  @Override
  public String primitiveValue() {
    return this.getValue().primitiveValue();
  }

  public float floatValue() {
    return this.getValue().castToDecimal(this.getValue()).getValue().floatValue();
  }

  public static VatRate defaultRate() {
    return from(19.0f);
  }

  public static VatRate from(float value) {
    return new VatRate(value);
  }
}
