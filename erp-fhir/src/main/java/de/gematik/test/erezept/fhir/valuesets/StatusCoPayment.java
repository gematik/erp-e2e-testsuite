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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

@Getter
@RequiredArgsConstructor
public enum StatusCoPayment implements FromValueSet {
  STATUS_0("0", "von Zuzahlungspflicht nicht befreit / gebührenpflichtig"),
  STATUS_1("1", "von Zuzahlungspflicht befreit / gebührenfrei"),
  STATUS_2("2", "künstliche Befruchtung (Regelung nach § 27a SGB V)"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.STATUS_CO_PAYMENT;

  private final String code;
  private final String display;

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Coding asCoding(KbvCodeSystem system) {
    val coding = new Coding();
    coding.setCode(this.getCode());
    coding.setSystem(system.getCanonicalUrl());
    return coding;
  }

  public Extension asExtension() {
    return this.asExtension(KbvItaErpStructDef.STATUS_CO_PAYMENT, this.getCodeSystem());
  }

  public Extension asExtension(WithStructureDefinition<?> structDef, KbvCodeSystem system) {
    return new Extension(structDef.getCanonicalUrl(), this.asCoding(system));
  }

  public static StatusCoPayment fromCode(String code) {
    return Arrays.stream(StatusCoPayment.values())
        .filter(scp -> scp.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(StatusCoPayment.class, code));
  }
}
