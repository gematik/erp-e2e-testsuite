/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.fhir.extensions.kbv;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import lombok.Getter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;

@Getter
public class ProductionInstruction {

  private final KbvItaErpStructDef strucDef;
  private final String display;

  private ProductionInstruction(KbvItaErpStructDef strucDef, String display) {
    this.display = display;
    this.strucDef = strucDef;
  }

  public static ProductionInstruction random() {
    return random(GemFaker.getFaker().chuckNorris().fact());
  }

  public static ProductionInstruction random(String display) {
    if (GemFaker.fakerBool()) {
      return asPackaging(display);
    } else {
      return asCompounding(display);
    }
  }

  public static ProductionInstruction asPackaging(String freitext) {
    return new ProductionInstruction(KbvItaErpStructDef.PACKAGING, freitext);
  }

  public static ProductionInstruction asCompounding(String freitext) {
    return new ProductionInstruction(KbvItaErpStructDef.COMPOUNDING_INSTRUCTION, freitext);
  }

  public Extension asExtension() {
    return new Extension(strucDef.getCanonicalUrl(), new StringType(display));
  }
}
