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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;

@SuppressWarnings("java:S1192")
public record AccidentExtension(
    AccidentCauseType accidentCauseType, @Nullable Date accidentDay, @Nullable String workplace) {

  public Extension asExtension() {
    return asItaForExtension();
  }

  @Deprecated(forRemoval = true)
  private Extension asItaErpExtension() {
    val outerExtension = KbvItaErpStructDef.ACCIDENT.asExtension();
    fillOuterExtension(outerExtension, "unfallkennzeichen", "unfalltag", "unfallbetrieb");
    return outerExtension;
  }

  /**
   * From version 1.1.0 on this extension is profiled under kbv.ita.for with minor changes
   *
   * @return Extension adhering to version 1.1.0
   */
  private Extension asItaForExtension() {
    val outerExtension = KbvItaForStructDef.ACCIDENT.asExtension();
    fillOuterExtension(outerExtension, "Unfallkennzeichen", "Unfalltag", "Unfallbetrieb");
    return outerExtension;
  }

  private void fillOuterExtension(
      Extension outerExtension, String kennzeichenUrl, String dayUrl, String betriebUrl) {
    val unfallKennzeichenExtension = new Extension(kennzeichenUrl);
    unfallKennzeichenExtension.setValue(accidentCauseType.asCoding());
    outerExtension.addExtension(unfallKennzeichenExtension);

    if (accidentDay != null) {
      val dayExtension = new Extension(dayUrl);
      dayExtension.setValue(new DateType(accidentDay, TemporalPrecisionEnum.DAY));
      outerExtension.addExtension(dayExtension);
    }

    if (workplace != null) {
      val betriebExtension = new Extension(betriebUrl);
      betriebExtension.setValue(new StringType(workplace));
      outerExtension.addExtension(betriebExtension);
    }
  }

  @Override
  public String toString() {
    return this.accidentCauseType.getDisplay();
  }

  public static AccidentExtension faker() {
    return faker(GemFaker.fakerValueSet(AccidentCauseType.class));
  }

  public static AccidentExtension faker(AccidentCauseType type) {
    return switch (type) {
      case ACCIDENT -> accident();
      case ACCIDENT_AT_WORK -> accidentAtWork().atWorkplace();
      case OCCUPATIONAL_DISEASE -> occupationalDisease();
      default -> throw new BuilderException(format("AccidentType {0} is not allowed", type));
    };
  }

  public static AccidentExtension accident() {
    return accident(new Date());
  }

  /**
   * Creates a "normal" accident extension with the given date as the day of the accident
   *
   * @param accidentDay is the day of the accident
   * @return AccidentExtension
   */
  public static AccidentExtension accident(Date accidentDay) {
    return new AccidentExtension(AccidentCauseType.ACCIDENT, accidentDay, null);
  }

  public static AccidentAtWorkBuilder accidentAtWork() {
    return accidentAtWork(new Date());
  }

  public static AccidentAtWorkBuilder accidentAtWork(Date accidentDay) {
    return new AccidentAtWorkBuilder(accidentDay);
  }

  public static AccidentExtension occupationalDisease() {
    return new AccidentExtension(AccidentCauseType.OCCUPATIONAL_DISEASE, null, null);
  }

  public static AccidentExtension fromExtension(Extension extension) {
    val causeType =
        extension.getExtension().stream()
            .filter(ext -> ext.getUrl().equalsIgnoreCase("Unfallkennzeichen"))
            .map(ext -> ext.getValue().castToCoding(ext.getValue()))
            .map(coding -> AccidentCauseType.fromCode(coding.getCode()))
            .findFirst()
            .orElseThrow(
                () ->
                    new MissingFieldException(KbvErpMedicationRequest.class, "Unfallkennzeichen"));

    val accidentDay =
        extension.getExtension().stream()
            .filter(ext -> ext.getUrl().equalsIgnoreCase("Unfalltag"))
            .map(ext -> ext.getValue().castToDate(ext.getValue()).getValue())
            .findFirst()
            .orElse(null);

    val workplace =
        extension.getExtension().stream()
            .filter(ext -> ext.getUrl().equalsIgnoreCase("Unfallbetrieb"))
            .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
            .findFirst()
            .orElse(null);

    return new AccidentExtension(causeType, accidentDay, workplace);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    val that = (AccidentExtension) o;
    return accidentCauseType == that.accidentCauseType
        && Objects.equals(accidentDay, that.accidentDay)
        && Objects.equals(workplace, that.workplace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accidentCauseType, accidentDay, workplace);
  }

  public record AccidentAtWorkBuilder(Date accidentDay) {
    private static final AccidentCauseType type = AccidentCauseType.ACCIDENT_AT_WORK;

    public AccidentExtension atWorkplace() {
      return atWorkplace("Arbeitsplatz");
    }

    public AccidentExtension atWorkplace(String workplace) {
      return new AccidentExtension(type, accidentDay, workplace);
    }
  }
}
