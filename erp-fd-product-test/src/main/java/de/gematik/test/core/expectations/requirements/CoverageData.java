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

package de.gematik.test.core.expectations.requirements;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoverageData {

  private final String testcaseId;
  private final Map<Requirement, Integer> passed;
  private final Map<Requirement, Integer> failed;

  public CoverageData(String testcaseId) {
    this.testcaseId = testcaseId;
    this.passed = new HashMap<>();
    this.failed = new HashMap<>();
  }

  public void add(Requirement req, boolean hasPassed) {
    Map<Requirement, Integer> counter;
    if (hasPassed) {
      counter = this.passed;
    } else {
      counter = this.failed;
    }
    addRequirementTo(counter, req);
  }

  private void addRequirementTo(Map<Requirement, Integer> counter, Requirement req) {
    var num = counter.getOrDefault(req, 0);
    num = num + 1;
    counter.put(req, num);
  }
}
