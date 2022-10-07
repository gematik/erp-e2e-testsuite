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

package de.gematik.test.erezept.app.mobile.locators;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.exceptions.InvalidLocatorException;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@Data
public class LocatorDictionary {

  private static LocatorDictionary instance;
  private List<GenericLocator> locators;

  @SneakyThrows
  public static LocatorDictionary getInstance() {
    if (instance == null) {
      val is = LocatorDictionary.class.getClassLoader().getResourceAsStream("locators.json");
      instance = new ObjectMapper().readValue(is, LocatorDictionary.class);
    }

    return instance;
  }

  public Optional<GenericLocator> getOptionallyBySemanticName(@NonNull final String name) {
    return this.locators.stream()
        .filter(l -> l.getSemanticName().equalsIgnoreCase(name))
        .findFirst();
  }

  public GenericLocator getBySemanticName(@NonNull final String name) {
    return this.getOptionallyBySemanticName(name)
        .orElseThrow(() -> new InvalidLocatorException(name));
  }

  public GenericLocator getByIdentifier(@NonNull final String identifier) {
    return this.locators.stream()
        .filter(l -> l.getIdentifier().equalsIgnoreCase(identifier))
        .findFirst()
        .orElseThrow(() -> new InvalidLocatorException(identifier));
  }
}
