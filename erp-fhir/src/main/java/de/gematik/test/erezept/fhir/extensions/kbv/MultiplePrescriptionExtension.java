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

package de.gematik.test.erezept.fhir.extensions.kbv;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

/** Mehrfachverordnung MVO */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MultiplePrescriptionExtension {

  private final boolean isMultiple;

  private Ratio ratio;
  @Nullable private Date start;
  @Nullable private Date end;
  private MultiplePrescriptionIdExtension id;

  public Optional<Date> getStart() {
    return Optional.ofNullable(start);
  }

  public Optional<Date> getEnd() {
    return Optional.ofNullable(end);
  }

  public int getNumerator() {
    return ratio.getNumerator().getValue().intValue();
  }

  public int getDenominator() {
    return ratio.getDenominator().getValue().intValue();
  }

  public Extension asExtension() {
    return asExtension(KbvItaErpVersion.getDefaultVersion());
  }

  public Extension asExtension(KbvItaErpVersion kbvItaErpVersion) {
    val outerExt = new Extension(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl());
    val kennzeichenExt = new Extension("Kennzeichen", new BooleanType(isMultiple));

    val innerExtensions = new LinkedList<Extension>();
    innerExtensions.add(kennzeichenExt);
    if (isMultiple) {
      val numExtension = new Extension("Nummerierung", ratio);

      val period =
          new Period()
              .setStartElement(new DateTimeType(start, TemporalPrecisionEnum.DAY))
              .setEndElement(new DateTimeType(end, TemporalPrecisionEnum.DAY));
      val periodExt = new Extension("Zeitraum", period);

      innerExtensions.add(numExtension);
      innerExtensions.add(periodExt);

      if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) >= 0) {
        innerExtensions.add(id.asExtension());
      }
    }

    outerExt.setExtension(innerExtensions);
    return outerExt;
  }

  public static MultiplePrescriptionExtension asNonMultiple() {
    return new MultiplePrescriptionExtension(false);
  }

  public static Builder asMultiple(int numerator, int denominator) {
    return new Builder(
        new Ratio()
            .setDenominator(new Quantity(denominator))
            .setNumerator(new Quantity(numerator)));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final Ratio ratio;
    private Date start;
    @Nullable private MultiplePrescriptionIdExtension id;

    public Builder withId(MultiplePrescriptionIdExtension id) {
      this.id = id;
      return this;
    }

    public Builder withId(String id) {
      this.id = MultiplePrescriptionIdExtension.withId(id);
      return this;
    }

    public Builder fromNow() {
      return starting(new Date());
    }

    public Builder starting(int days) {
      val cal = Calendar.getInstance();
      cal.add(Calendar.DATE, days);
      return starting(cal.getTime());
    }

    public Builder starting(Date start) {
      this.start = start;
      return this;
    }

    /**
     * In this case the MVO is automatically valid for 365 days beginning with the start date see
     * A_22634
     *
     * @return MultiplePrescriptionExtension
     */
    public MultiplePrescriptionExtension withoutEndDate() {
      return withoutEndDate(true);
    }

    public MultiplePrescriptionExtension withoutEndDate(boolean autoStart) {
      return validUntil(null, autoStart);
    }

    public MultiplePrescriptionExtension validForDays(int amount) {
      return validForDays(amount, true);
    }

    public MultiplePrescriptionExtension validForDays(int amount, boolean autoStart) {
      val cal = Calendar.getInstance();
      cal.add(Calendar.DATE, amount);
      val end = cal.getTime();
      return validUntil(end, autoStart);
    }

    public MultiplePrescriptionExtension validUntil(Date end) {
      return validUntil(end, true);
    }

    public MultiplePrescriptionExtension validUntil(Date end, boolean autoStart) {
      if (autoStart && start == null) {
        start = new Date();
      }
      return validThrough(start, end);
    }

    public MultiplePrescriptionExtension validThrough(int daysStart, int daysEnd) {
      val calStart = Calendar.getInstance();
      val calEnd = Calendar.getInstance();
      calStart.add(Calendar.DATE, daysStart);
      calEnd.add(Calendar.DATE, daysEnd);
      val startDate = calStart.getTime();
      val endDate = calEnd.getTime();
      return validThrough(startDate, endDate);
    }

    public MultiplePrescriptionExtension validThrough(@Nullable Date start, @Nullable Date end) {
      val ret = new MultiplePrescriptionExtension(true);
      ret.id = id != null ? id : MultiplePrescriptionIdExtension.randomId();
      ret.ratio = ratio;
      ret.start = start;
      ret.end = end;
      return ret;
    }

    public Builder withRandomId() {
      this.id = MultiplePrescriptionIdExtension.randomId();
      return this;
    }
  }
}
