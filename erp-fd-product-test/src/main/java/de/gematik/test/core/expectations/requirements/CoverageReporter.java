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

package de.gematik.test.core.expectations.requirements;

import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.json.JSONArray;
import org.json.JSONObject;

public class CoverageReporter {

  private static CoverageReporter instance;

  private final List<CoverageData> coverageData;
  private CoverageData currentCounter;

  private CoverageReporter() {
    this.coverageData = new LinkedList<>();
  }

  public static CoverageReporter getInstance() {
    if (instance == null) {
      instance = new CoverageReporter();
    }

    return instance;
  }

  public void startTestcase(String testcaseId) {
    this.currentCounter = new CoverageData(testcaseId);
    this.coverageData.add(currentCounter);
  }

  public JSONObject finishTestcase() {
    return serializeData(currentCounter);
  }

  public JSONObject serializeReport() {
    val testcases = new JSONArray();
    coverageData.forEach(data -> testcases.put(serializeData(data)));

    return new JSONObject().put("testcases", testcases);
  }

  private JSONObject serializeData(CoverageData data) {
    val passed = new JSONArray();
    data.getPassed()
        .forEach(
            (req, num) -> {
              val entry = new JSONObject();
              entry
                  .put("covered", num)
                  .put("description", req.getDescription())
                  .put("id", req.getId());
              passed.put(entry);
            });

    val failed = new JSONArray();
    data.getFailed()
        .forEach(
            (req, num) -> {
              val entry = new JSONObject();
              entry
                  .put("covered", num)
                  .put("description", req.getDescription())
                  .put("id", req.getId());
              failed.put(entry);
            });

    val testcaseData = new JSONObject();
    testcaseData.put("passed", passed).put("failed", failed).put("testcase", data.getTestcaseId());
    return testcaseData;
  }

  /**
   * simply increment the number in which the requirement was covered
   *
   * @param req to increment the coverage for
   * @param passed define if requirement was passed
   */
  public void add(Requirement req, boolean passed) {
    currentCounter.add(req, passed);
  }
}
