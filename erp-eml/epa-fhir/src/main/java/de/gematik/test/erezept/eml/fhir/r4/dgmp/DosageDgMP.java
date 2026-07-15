/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.eml.fhir.r4.dgmp;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.PrimitiveType;

/**
 * Implementation Guide: <a href="https://ig.fhir.de/igs/medication/index.html">Medication IG DE</a>
 */
@Slf4j
@DatatypeDef(name = "Dosage")
@SuppressWarnings({"java:S110"})
public class DosageDgMP extends Dosage {

  @Generated
  @Override
  public int hashCode() {
    val repeat = this.getTiming() != null ? this.getTiming().getRepeat() : null;
    int result =
        Objects.hash(
            this.getText(),
            repeat != null ? repeat.getCount() : null,
            repeat != null ? repeat.getCountMax() : null,
            repeat != null ? repeat.getDuration() : null,
            repeat != null ? repeat.getDurationMax() : null,
            repeat != null ? repeat.getDurationUnit() : null,
            repeat != null ? repeat.getFrequency() : null,
            repeat != null ? repeat.getFrequencyMax() : null,
            repeat != null ? repeat.getPeriod() : null,
            repeat != null ? repeat.getPeriodMax() : null,
            repeat != null ? repeat.getPeriodUnit() : null,
            repeat != null ? repeat.getOffset() : null,
            repeat != null ? repeat.getBounds() : null);
    // dayOfWeek
    if (repeat != null && repeat.getDayOfWeek() != null) {
      result =
          31 * result
              + repeat.getDayOfWeek().stream()
                  .filter(Objects::nonNull)
                  .map(org.hl7.fhir.r4.model.Enumeration::getValue)
                  .collect(Collectors.toSet())
                  .hashCode();
    }
    // timeOfDay
    if (repeat != null && repeat.getTimeOfDay() != null) {
      result =
          31 * result
              + repeat.getTimeOfDay().stream()
                  .filter(Objects::nonNull)
                  .map(PrimitiveType::getValue)
                  .collect(Collectors.toSet())
                  .hashCode();
    }
    // when
    if (repeat != null && repeat.getWhen() != null) {
      result =
          31 * result
              + repeat.getWhen().stream()
                  .filter(Objects::nonNull)
                  .map(org.hl7.fhir.r4.model.Enumeration::getValue)
                  .collect(Collectors.toSet())
                  .hashCode();
    }
    // doseAndRate
    if (this.getDoseAndRate() != null) {
      result =
          31 * result
              + this.getDoseAndRate().stream()
                  .filter(Objects::nonNull)
                  .map(Object::hashCode)
                  .collect(Collectors.toSet())
                  .hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object dosageDgMP) {
    if (!(dosageDgMP instanceof Dosage dosageDgMP1)) return false;

    val repeatLeft = this.getTiming() != null ? this.getTiming().getRepeat() : null;
    val repeatRight = dosageDgMP1.getTiming() != null ? dosageDgMP1.getTiming().getRepeat() : null;

    if (repeatLeft == repeatRight) return true;

    boolean equals =
        compareDoseAndRateListIgnoreOrder(dosageDgMP1.getDoseAndRate(), this.getDoseAndRate());
    equals &= Objects.equals(this.getText(), dosageDgMP1.getText());

    equals &= Objects.equals(repeatLeft.getCount(), repeatRight.getCount());
    equals &= Objects.equals(repeatLeft.getCountMax(), repeatRight.getCountMax());
    equals &= Objects.equals(repeatLeft.getDuration(), repeatRight.getDuration());
    equals &= Objects.equals(repeatLeft.getDurationMax(), repeatRight.getDurationMax());
    equals &= Objects.equals(repeatLeft.getDurationUnit(), repeatRight.getDurationUnit());
    equals &= Objects.equals(repeatLeft.getFrequency(), repeatRight.getFrequency());
    equals &= Objects.equals(repeatLeft.getFrequencyMax(), repeatRight.getFrequencyMax());
    equals &= Objects.equals(repeatLeft.getPeriod(), repeatRight.getPeriod());
    equals &= Objects.equals(repeatLeft.getPeriodMax(), repeatRight.getPeriodMax());
    equals &= Objects.equals(repeatLeft.getPeriodUnit(), repeatRight.getPeriodUnit());
    equals &= Objects.equals(repeatLeft.getOffset(), repeatRight.getOffset());

    // Compare dayOfWeek (List<Enumeration<Timing.DayOfWeek>>)
    equals &= compareEnumListIgnoreOrder(repeatLeft.getDayOfWeek(), repeatRight.getDayOfWeek());

    // Compare timeOfDay (List<TimeType>)
    equals &=
        compareListIgnoreOrder(
            repeatLeft.getTimeOfDay(), repeatRight.getTimeOfDay(), PrimitiveType::getValue);

    // Compare when (List<Enumeration<Timing.EventTiming>>)
    equals &= compareEnumListIgnoreOrder(repeatLeft.getWhen(), repeatRight.getWhen());

    // Compare bounds (Type)
    equals &= compareDeep(repeatLeft.getBounds(), repeatRight.getBounds(), true);

    return equals;
  }

  private <T, K> boolean compareListIgnoreOrder(
      List<T> left, List<T> right, Function<T, K> keyExtractor) {
    if (left == right) return true;
    if (left == null || right == null) return false;
    Set<K> leftSet =
        left.stream().filter(Objects::nonNull).map(keyExtractor).collect(Collectors.toSet());
    Set<K> rightSet =
        right.stream().filter(Objects::nonNull).map(keyExtractor).collect(Collectors.toSet());
    return leftSet.equals(rightSet);
  }

  private <T extends Enum<T>> boolean compareEnumListIgnoreOrder(
      List<? extends org.hl7.fhir.r4.model.Enumeration<T>> left,
      List<? extends org.hl7.fhir.r4.model.Enumeration<T>> right) {
    if (left == right) return true;
    if (left == null || right == null) return false;
    Set<T> leftSet =
        left.stream()
            .filter(Objects::nonNull)
            .map(org.hl7.fhir.r4.model.Enumeration::getValue)
            .collect(Collectors.toSet());
    Set<T> rightSet =
        right.stream()
            .filter(Objects::nonNull)
            .map(org.hl7.fhir.r4.model.Enumeration::getValue)
            .collect(Collectors.toSet());
    return leftSet.equals(rightSet);
  }

  private boolean compareDoseAndRateListIgnoreOrder(
      List<Dosage.DosageDoseAndRateComponent> left, List<Dosage.DosageDoseAndRateComponent> right) {
    if (left == right) return true;
    if (left == null || right == null) return false;
    if (left.size() != right.size()) return false;
    // Vergleiche die Listeninhalte unabhängig von der Reihenfolge
    // Nutze compareDeep für die Komponenten
    List<Dosage.DosageDoseAndRateComponent> rightCopy = right.stream().collect(Collectors.toList());
    for (Dosage.DosageDoseAndRateComponent l : left) {
      boolean found = false;
      for (int i = 0; i < rightCopy.size(); i++) {
        if (compareDeep(l, rightCopy.get(i), true)) {
          rightCopy.remove(i);
          found = true;
          break;
        }
      }
      if (!found) return false;
    }
    return rightCopy.isEmpty();
  }

  public static DosageDgMP fromDosage(Dosage adaptee) {
    if (adaptee instanceof DosageDgMP dosageDgMP) {
      return dosageDgMP;
    } else {
      val dosageDgMp = new DosageDgMP();
      adaptee.copyValues(dosageDgMp);
      return dosageDgMp;
    }
  }
}
