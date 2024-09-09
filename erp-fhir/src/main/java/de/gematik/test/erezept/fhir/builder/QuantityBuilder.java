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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonCodeSystem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuantityBuilder {

  private final Quantity quantity;

  public static QuantityBuilder asUcumPackage() {
    return asUcum("{Package}");
  }

  public static QuantityBuilder asUcum(String code) {
    val q = new Quantity();
    q.setSystem(CommonCodeSystem.UCUM.getCanonicalUrl()).setCode(code);
    return new QuantityBuilder(q);
  }

  public Quantity withValue(int amount) {
    return this.quantity.setValue(amount);
  }
}
