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

import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import de.gematik.test.erezept.primsys.utils.Strings;
import javax.annotation.Nullable;

public class NullableEnumMapper {

  private NullableEnumMapper() {
    throw new AssertionError();
  }

  public interface IValueSetMapper<T extends IValueSet> {
    T map(String value);
  }

  @Nullable
  public static <T extends IValueSet> T mapNullable(
      @Nullable final String value, IValueSetMapper<T> mapper) {
    if (!Strings.isNullOrEmpty(value)) {
      return mapper.map(value);
    } else {
      return null;
    }
  }
}
