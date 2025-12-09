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

package de.gematik.test.erezept.fhir.r4.eu;

import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem;
import lombok.Getter;
import org.hl7.fhir.r4.model.Coding;

@Getter
public class EuHealthcareFacilityType {

  private final String system;
  private final String code;
  private final String display;

  public EuHealthcareFacilityType(String code, String display) {
    this.system = ErpWorkflowCodeSystem.PROFESSION_OID.getCanonicalUrl();
    this.code = code;
    this.display = display;
  }

  public static EuHealthcareFacilityType getDefault() {
    return new EuHealthcareFacilityType("1.2.276.0.76.4.54", "Öffentliche Apotheke");
  }

  public Coding asCoding() {
    return new Coding().setSystem(system).setCode(code).setDisplay(display);
  }
}
