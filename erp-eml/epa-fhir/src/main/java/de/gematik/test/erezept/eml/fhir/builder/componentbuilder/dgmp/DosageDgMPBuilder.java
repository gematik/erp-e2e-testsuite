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

package de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp;

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.DOSIEREINHEIT;
import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.UCUM;

import de.gematik.bbriccs.fhir.builder.ElementBuilder;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import java.math.BigDecimal;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DosageDgMPBuilder extends ElementBuilder<DosageDgMP, DosageDgMPBuilder> {

  @Nullable private final Quantity dosageAndRate;
  private String text;
  private BigDecimal value;
  private Timing timing;

  public static DosageDgMPBuilder dosageBuilder() {
    return new DosageDgMPBuilder(null);
  }

  public static DosageDgMPBuilder dosageBuilder(long value, BmpDosiereinheit code) {
    Quantity quantity = new Quantity();
    quantity.setUnit(code.getDisplay()).setCode(code.getCode());
    quantity.setValue(value);
    return new DosageDgMPBuilder(quantity);
  }

  /**
   * Erstellt einen neuen {@link DosageDgMPBuilder} mit einer vordefinierten {@link Quantity} für
   * Dosierung und Rate. der Wert des Systems ist fest auf 'http://unitsofmeasure.org'gesetzt
   *
   * @param value Der numerische Wert der Dosierung (z.B. 1, 2, 3).
   * @param unit Die Einheit der Dosierung (z.B. "mg", "ml").
   * @param code Der Code der Dosierungseinheit (z.B. "mg", "ml" oder ein UCUM-Code).
   * @return Ein konfigurierter {@link DosageDgMPBuilder} mit gesetzter Dosierung.
   */
  public static DosageDgMPBuilder dosageBuilder(long value, String unit, String code) {
    val quantity = new Quantity();
    quantity.setUnit(unit).setCode(code).setValue(value).setSystem(UCUM.getCanonicalUrl());
    return new DosageDgMPBuilder(quantity);
  }

  public DosageDgMPBuilder text(String text) {
    this.text = text;
    return this;
  }

  public DosageDgMPBuilder timing(Timing timing) {
    this.timing = timing;
    return this;
  }

  @Override
  public DosageDgMP build() {
    val dosage = new DosageDgMP();
    Optional.ofNullable(text).ifPresent(dosage::setText);
    Optional.ofNullable(dosageAndRate)
        .ifPresent(
            dAR ->
                dosage.addDoseAndRate(
                    new org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent()
                        .setDose(
                            dAR.setSystem(
                                !dAR.hasSystem()
                                    ? DOSIEREINHEIT.getCanonicalUrl()
                                    : dAR.getSystem()))));

    Optional.ofNullable(timing).ifPresent(dosage::setTiming);
    return dosage;
  }
}
