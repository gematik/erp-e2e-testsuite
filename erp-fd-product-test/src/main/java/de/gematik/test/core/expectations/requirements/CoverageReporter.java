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
 */

package de.gematik.test.core.expectations.requirements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;

public class CoverageReporter {

  private static CoverageReporter instance;

  private final ObjectMapper mapper;
  private final List<CoverageData> coverageData;
  private CoverageData currentCounter;

  private CoverageReporter() {
    this.coverageData = new LinkedList<>();
    this.mapper = new ObjectMapper();
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

  public ObjectNode finishTestcase() {
    return serializeData(currentCounter);
  }

  private ObjectNode serializeReport() {
    val testcases = this.mapper.createArrayNode();
    coverageData.forEach(data -> testcases.add(serializeData(data)));

    return this.mapper.createObjectNode().set("testcases", testcases);
  }

  @SneakyThrows
  public void writeToFile(FileWriter fw) {
    val json = this.mapper.writeValueAsString(this.serializeReport());
    fw.write(json);
  }

  private ObjectNode serializeData(CoverageData data) {
    val passed = this.mapper.createArrayNode();
    data.getPassed()
        .forEach(
            (req, num) -> {
              val entry = this.mapper.createObjectNode();
              entry
                  .put("covered", num)
                  .put("description", req.getDescription())
                  .put("id", req.getId());
              passed.add(entry);
            });

    val failed = this.mapper.createArrayNode();
    data.getFailed()
        .forEach(
            (req, num) -> {
              val entry = this.mapper.createObjectNode();
              entry
                  .put("covered", num)
                  .put("description", req.getDescription())
                  .put("id", req.getId());
              failed.add(entry);
            });

    val testcaseData = mapper.createObjectNode();
    testcaseData.set("passed", passed);
    testcaseData.set("failed", failed);
    testcaseData.put("testcase", data.getTestcaseId());
    return testcaseData;
  }

  /**
   * increment the number in which the requirement was covered
   *
   * @param req to increment the coverage for
   * @param passed define if requirement was passed
   */
  public void add(Requirement req, boolean passed) {
    currentCounter.add(req, passed);
  }
}
