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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HisInsuranceType implements Question<Boolean> {

  private final InsuranceTypeDe expectedInsuranceType;

  private HisInsuranceType(InsuranceTypeDe expected) {
    this.expectedInsuranceType = expected;
  }

  @Override
  public Boolean answeredBy(final Actor actor) {
    val baseDate = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    return baseDate.getPatientInsuranceType().equals(expectedInsuranceType);
  }

  public static HisInsuranceType equalsExpected(String insuranceType) {
    return equalsExpected(InsuranceTypeDe.fromCode(insuranceType));
  }

  public static HisInsuranceType equalsExpected(InsuranceTypeDe insuranceType) {
    return new HisInsuranceType(insuranceType);
  }
}
