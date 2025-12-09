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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

@Getter
@RequiredArgsConstructor
public enum IsoCountryCode implements FromValueSet {
  BE("BE", "Belgium"),
  DE("DE", "Germany"),
  NL("NL", "Netherlands"),
  LI("LI", "Liechtenstein"),
  SM("SM", "San Marino"),
  ES("ES", "Spain"),
  AT("AT", "Austria"),
  BG("BG", "Bulgaria"),
  HR("HR", "Croatia"),
  CY("CY", "Cyprus"),
  CZ("CZ", "Czech Republic"),
  DK("DK", "Denmark"),
  EE("EE", "Estonia"),
  FI("FI", "Finland"),
  FR("FR", "France"),
  GR("GR", "Greece"),
  HU("HU", "Hungary"),
  IE("IE", "Ireland"),
  IT("IT", "Italy"),
  LV("LV", "Latvia"),
  LT("LT", "Lithuania"),
  LU("LU", "Luxembourg"),
  MT("MT", "Malta"),
  PL("PL", "Poland"),
  PT("PT", "Portugal"),
  RO("RO", "Romania"),
  SK("SK", "Slovakia"),
  SI("SI", "Slovenia"),
  SE("SE", "Sweden"),
  // for test only,
  CH("CH", "Switzerland"),
  ZW("ZW", "Zimbabwe");

  public static final CommonCodeSystem CODE_SYSTEM = CommonCodeSystem.ISO_3166;
  private final String code;
  private final String display;

  @Override
  public CommonCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(CODE_SYSTEM.getCanonicalUrl(), this.asCoding());
  }

  public static IsoCountryCode fromCode(String code) {
    return Arrays.stream(IsoCountryCode.values())
        .filter(iCC -> iCC.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(IsoCountryCode.class, code));
  }
}
