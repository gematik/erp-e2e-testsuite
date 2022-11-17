/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.fhir.exceptions.InvalidCommunicationType;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ICommunicationTypeTest {

  @Test
  void shouldGetCommunicationTypeFromUrl() {
    List<ICommunicationType<?>> types = Arrays.asList(CommunicationType.values());
    types.forEach(
        type -> {
          val url = type.getTypeUrl();
          assertEquals(type, ICommunicationType.fromUrl(url));
        });
  }

  @Test
  void fromUrlshouldThrowOnInvalidUrl() {
    assertThrows(InvalidCommunicationType.class, () -> ICommunicationType.fromUrl("invalid_url"));
  }
}
