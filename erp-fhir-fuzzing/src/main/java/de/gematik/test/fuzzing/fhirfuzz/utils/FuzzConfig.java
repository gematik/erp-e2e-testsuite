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

package de.gematik.test.fuzzing.fhirfuzz.utils;

import static java.text.MessageFormat.format;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Getter
@Setter
public class FuzzConfig {
  private String name;
  private Float usedPercentOfMutators;
  private Map<String, String> detailSetup;
  private Float percentOfAll;
  private Float percentOfEach;
  private Boolean useAllMutators;
  private int iterations;
  private String pathToPrintFile;
  private Boolean shouldPrintToFile;

  public static FuzzConfig getDefault() {
    val fuzzConf = new FuzzConfig();
    fuzzConf.name = "default";
    fuzzConf.usedPercentOfMutators = 40.00f;
    fuzzConf.percentOfAll = 20.00f;
    fuzzConf.percentOfEach = 10.00f;
    fuzzConf.useAllMutators = false;
    fuzzConf.iterations = 3;
    fuzzConf.shouldPrintToFile = false;
    fuzzConf.setDetailSetup(new HashMap<>());
    fuzzConf.getDetailSetup().put("KBV", "TRUE");
    fuzzConf.getDetailSetup().put("BreakRanges", "TRUE");
    return fuzzConf;
  }

  public static FuzzConfig getRandom() {
    val fuzzConf = new FuzzConfig();
    Random random = new SecureRandom();
    fuzzConf.setDetailSetup(new HashMap<>());
    fuzzConf.getDetailSetup().put("KBV", "false");
    fuzzConf.getDetailSetup().put("BreakRanges", "TRUE");
    fuzzConf.usedPercentOfMutators = random.nextFloat(50);
    fuzzConf.percentOfAll = random.nextFloat(20);
    fuzzConf.percentOfEach = random.nextFloat(20);
    fuzzConf.useAllMutators = false;
    fuzzConf.iterations = random.nextInt(5);
    fuzzConf.shouldPrintToFile = false;
    log.info(
        format(
            "FuzzConfig called: {6}, Attributes had been setup default with following entries:"
                + " PercentOfMutators: {0}, PercentOfAll: {1}, PercentOfEach: {2}, UseAllMutators:"
                + " {3}, Iterations: {4}, ShouldPrintToFIle: {5}, specific SetupDetails: {7}",
            fuzzConf.usedPercentOfMutators,
            fuzzConf.percentOfAll,
            fuzzConf.percentOfEach,
            fuzzConf.useAllMutators,
            fuzzConf.iterations,
            fuzzConf.shouldPrintToFile,
            fuzzConf.name,
            !fuzzConf.detailSetup.isEmpty()
                ? fuzzConf.detailSetup.entrySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))
                : "no Entry"));
    return fuzzConf;
  }

  @Override
  public String toString() {
    return format(
        "FuzzConfig called: {6}: PercentOfMutators: {0}, PercentOfAll: {1}, PercentOfEach: {2},"
            + " UseAllMutators: {3}, ShouldPrintToFIle: {5}, specific SetupDetails: {7}",
        this.usedPercentOfMutators,
        this.percentOfAll,
        this.percentOfEach,
        this.useAllMutators,
        this.iterations,
        this.shouldPrintToFile,
        this.name,
        (this.detailSetup != null)
            ? this.detailSetup.entrySet().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "))
            : "no Entry");
  }
}
