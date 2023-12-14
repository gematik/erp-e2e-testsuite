/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.primsys.data.MvoDto;
import java.util.Date;
import lombok.val;

public class MvoDataMapper extends DataMapper<MvoDto, MultiplePrescriptionExtension> {

  public MvoDataMapper(MvoDto dto) {
    super(dto);
  }

  protected void complete() {
    ensure(dto::getStartDate, dto::setStartDate, Date::new);

    if (dto.getNumerator() == null || dto.getDenominator() == null) {
      // if any of them is missing replace both to ensure the ratio is valid!
      val faked = GemFaker.mvo(true);
      dto.setNumerator(faked.getNumerator());
      dto.setDenominator(faked.getDenominator());
    }
  }

  @Override
  public MultiplePrescriptionExtension convertInternal() {
    val builder =
        MultiplePrescriptionExtension.asMultiple(dto.getNumerator(), dto.getDenominator())
            .starting(dto.getStartDate());

    if (dto.getEndDate() != null) {
      return builder.validUntil(dto.getEndDate());
    } else {
      return builder.withoutEndDate();
    }
  }

  public boolean isValid() {
    return !(dto.getNumerator() == null
        || dto.getNumerator() < 1
        || dto.getDenominator() == null
        || dto.getDenominator() < 2
        || dto.getNumerator() > 4
        || dto.getDenominator() > 4
        || dto.getNumerator() > dto.getDenominator()
        || dto.getStartDate() == null);
  }

  public static MvoDataMapper from(MvoDto dto) {
    return new MvoDataMapper(dto);
  }

  public static MvoDataMapper random() {
    return from(randomDto());
  }

  public static MvoDto randomDto() {
    val faked = GemFaker.mvo(true);
    val dto = new MvoDto();

    dto.setDenominator(faked.getDenominator());
    dto.setNumerator(faked.getNumerator());
    faked.getStart().ifPresent(dto::setStartDate);
    faked.getEnd().ifPresent(dto::setEndDate);

    return dto;
  }
}
