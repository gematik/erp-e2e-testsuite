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
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicInsuranceCoverageInfo implements InsuranceCoverageInfo {

  private final String name;
  private final String iknr;
  private final VersicherungsArtDeBasis insuranceType;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getIknr() {
    return this.iknr;
  }

  @Override
  public VersicherungsArtDeBasis getInsuranceType() {
    return this.insuranceType;
  }

  public static IntermediateBuilder named(String name) {
    return new IntermediateBuilder(name);
  }

  public static InsuranceCoverageInfo random() {
    return random(GemFaker.fakerValueSet(Wop.class));
  }

  public static InsuranceCoverageInfo random(Wop wop) {
    return new DynamicInsuranceCoverageInfo(
        GemFaker.insuranceName(wop),
        IKNR.random().getValue(),
        GemFaker.fakerValueSet(VersicherungsArtDeBasis.class));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class IntermediateBuilder {
    private final String name;

    public FinalBuilder ofType(VersicherungsArtDeBasis type) {
      return new FinalBuilder(name, type);
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class FinalBuilder {
    private final String name;
    private final VersicherungsArtDeBasis type;

    public InsuranceCoverageInfo withIknr(String iknr) {
      return new DynamicInsuranceCoverageInfo(this.name, iknr, type);
    }
  }
}
