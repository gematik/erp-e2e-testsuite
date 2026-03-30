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

package de.gematik.test.erezept.fhir.extensions.kbv;

import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;

@RequiredArgsConstructor
@Setter
@Getter
public class TeratogenicExtension {

  public static final String URL_OFF_LABEL = "Off-Label";
  public static final String URL_CHILDBEARING_WOMAN = "GebaerfaehigeFrau";
  public static final String URL_SAFETY_MEASURES = "EinhaltungSicherheitsmassnahmen";
  public static final String URL_INFO_MATERIALS = "AushaendigungInformationsmaterialien";
  public static final String URL_EXPERTISE = "ErklaerungSachkenntnis";

  private boolean offLabel = false;
  private boolean childbearingWoman = false;

  /**
   * See <a
   * href="https://simplifier.net/packages/kbv.ita.erp/1.4.0/files/3113162/~overview">-erp-angabeT-RezeptEinhaltungSicherheitsmassnahmenTrue</a>
   */
  private boolean einhaltungSicherheitsmassnahmen = true;

  /**
   * See <a
   * href="https://simplifier.net/packages/kbv.ita.erp/1.4.0/files/3113162/~overview">-erp-angabeT-RezeptAushaendigungInformationsmaterialienTrue</a>
   */
  private boolean aushaendigungInformationsmaterialien = true;

  /**
   * See <a
   * href="https://simplifier.net/packages/kbv.ita.erp/1.4.0/files/3113162/~overview">-erp-angabeT-RezeptErklaerungSachkenntnisTrue</a>
   */
  private boolean erklaerungSachkenntnis = true;

  public Extension asExtension() {
    val ret = new Extension(KbvItaErpStructDef.TERATOGENIC.getCanonicalUrl());
    ret.addExtension(new Extension(URL_OFF_LABEL, new BooleanType(offLabel)));
    ret.addExtension(new Extension(URL_CHILDBEARING_WOMAN, new BooleanType(childbearingWoman)));
    ret.addExtension(
        new Extension(URL_SAFETY_MEASURES, new BooleanType(einhaltungSicherheitsmassnahmen)));
    ret.addExtension(
        new Extension(URL_INFO_MATERIALS, new BooleanType(aushaendigungInformationsmaterialien)));
    ret.addExtension(new Extension(URL_EXPERTISE, new BooleanType(erklaerungSachkenntnis)));
    return ret;
  }
}
