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

package de.gematik.test.erezept.fhir.builder.dgmp;

import static de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem.DOSIEREINHEIT;

import de.gematik.bbriccs.fhir.builder.ElementBuilder;
import de.gematik.test.erezept.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.fhir.valuesets.BmpDosiereinheit;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.Timing;

public class DosageDgMPBuilder extends ElementBuilder<DosageDgMP, DosageDgMPBuilder> {

  private String text;
  private SimpleQuantity dosegeAndRate;
  private BigDecimal value;
  private Timing timing;

  public static DosageDgMPBuilder dosageBuilder(String unit, BmpDosiereinheit code) {
    val builder = dosageBuilder();
    SimpleQuantity simpleQuantity = new SimpleQuantity();
    simpleQuantity.setCode(code.getCode()).setUnit(unit);
    builder.dosegeAndRate = simpleQuantity;
    return builder;
  }

  public static DosageDgMPBuilder dosageBuilder() {
    return new DosageDgMPBuilder();
  }

  public DosageDgMPBuilder text(String text) {
    this.text = text;
    return this;
  }

  public DosageDgMPBuilder timing(int frequency, int period, Timing.UnitsOfTime periodUnit) {
    val repeat = new Timing.TimingRepeatComponent();
    repeat.setFrequency(frequency).setPeriod(period).setPeriodUnit(periodUnit);
    this.timing = new Timing().setRepeat(repeat);
    return this;
  }

  @Override
  public DosageDgMP build() {
    val dosage = new DosageDgMP();
    Optional.ofNullable(text).ifPresent(dosage::setText);
    Optional.ofNullable(dosegeAndRate)
        .ifPresent(
            dAR ->
                dosage.addDoseAndRate(
                    new org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent()
                        .setDose(dAR.setSystem(DOSIEREINHEIT.getCanonicalUrl()))));

    Optional.ofNullable(value)
        .ifPresent(
            va ->
                dosage.getDoseAndRate().stream()
                    .findFirst()
                    .ifPresent(dDaR -> dDaR.getDoseQuantity().setValue(va)));
    Optional.ofNullable(timing).ifPresent(dosage::setTiming);
    return dosage;
  }

  public DosageDgMPBuilder value(BigDecimal value) {
    this.value = value;
    return this;
  }
}
