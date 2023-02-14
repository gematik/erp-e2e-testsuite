/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.rest.data.util;

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.val;
import org.junit.Test;

public class NullableEnumMapperTest {

  @Test
  public void shouldMapValidValueSetCodes() {
    val actualWop = NullableEnumMapper.mapNullable("72", Wop::fromCode);
    assertEquals(Wop.BERLIN, actualWop);

    val countryGer = NullableEnumMapper.mapNullable("D", Country::fromCode);
    assertEquals(Country.D, countryGer);
  }

  @Test
  public void shouldReturnNullOnNullCode() {
    val nullDarreichung = NullableEnumMapper.mapNullable(null, Darreichungsform::fromCode);
    assertNull(nullDarreichung);

    val nullMedicationCategory = NullableEnumMapper.mapNullable(null, MedicationCategory::fromCode);
    assertNull(nullMedicationCategory);
  }

  @Test()
  public void shouldThrowInvalidValueSetException() {
    assertThrows(
        InvalidValueSetException.class,
        () -> NullableEnumMapper.mapNullable("TEST", StandardSize::fromCode));

    assertThrows(
        InvalidValueSetException.class,
        () -> NullableEnumMapper.mapNullable("TEST", VersichertenStatus::fromCode));
  }
}
