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

package de.gematik.test.erezept.toggle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;

public class CucumberFeatureParser {

  private final List<String> rawTags;

  private CucumberFeatureParser(List<String> rawTags) {
    this.rawTags = rawTags;
  }

  public boolean isFeatureActive(CucumberTag tag) {
    val ret = new AtomicReference<>(tag.getDefaultValue());
    rawTags.stream()
        .filter(rawTag -> rawTag.contains(tag.getTag()))
        .findFirst()
        .ifPresent(rawTag -> ret.set(!rawTag.startsWith("not")));
    return ret.get();
  }

  public static CucumberFeatureParser defaults() {
    return new CucumberFeatureParser(List.of());
  }

  public static CucumberFeatureParser fromString(String input) {
    val tags = input.split("and");
    return new CucumberFeatureParser(Arrays.stream(tags).map(String::trim).toList());
  }
}
