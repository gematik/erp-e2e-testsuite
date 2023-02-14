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

package de.gematik.test.erezept.fhirdump;

import static java.text.MessageFormat.*;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.thucydides.core.model.*;

@Slf4j
public class FhirDumper {

  private static FhirDumper instance;

  @Getter private final Path basePath;
  private final DumpSummary index;
  private final List<ScenarioDump> scenarios;
  private int dumpCounter = 0;
  private ScenarioDump currentScenario;

  protected FhirDumper(Path basePath) {
    this.basePath = basePath;
    this.index = new DumpSummary();
    this.scenarios = new LinkedList<>();

    // clean the path before writing new dumps
    try (val fileStream = Files.walk(this.basePath)) {
      fileStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      log.warn(format("Not able to clear base path for FHIR dumps"));
    }

    basePath.toFile().mkdirs();
  }

  public static FhirDumper getInstance() {
    if (instance == null) {
      instance = new FhirDumper(Path.of(System.getProperty("user.dir"), "target", "fhir_dumps"));
    }
    return instance;
  }

  public void startScenario(String id, String name) {
    this.currentScenario = new ScenarioDump();
    this.currentScenario.setId(id);
    this.currentScenario.setName(name);
    this.scenarios.add(this.currentScenario);
    this.dumpCounter = 0;
  }

  public void finishScenario(TestOutcome testOutcome) {
    val summary = new ScenarioSummary();
    summary.setName(this.currentScenario.getName());
    summary.setId(this.currentScenario.getId());
    summary.setDescription(this.currentScenario.getDescription());
    summary.setFeatureFile(this.currentScenario.getFeatureFile());
    summary.setFeature(this.currentScenario.getFeature());
    summary.setResult(testOutcome.getResult().getLabel());
    this.currentScenario = null;
    this.index.getScenarios().add(summary);
  }

  public Optional<ScenarioDump> getCurrentScenario() {
    return Optional.ofNullable(this.currentScenario);
  }

  @SneakyThrows
  @SuppressWarnings("java:S6300") // write to file by intention, not an issue
  public void writeDump(String operation, String fileName, String content) {
    writeDump(
        operation,
        fileName,
        file -> {
          try (val writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
          } catch (IOException ioe) {
            log.error(format("Error while writing {0}", fileName), ioe);
          }
        });
  }

  public void writeDump(String operation, String fileName, Consumer<File> fileWriter) {
    val optionalFile = getScenarioDumpFile(fileName);
    if (optionalFile.isPresent()) {
      val currentStep = this.currentScenario.getSteps().getLast();
      val dumpFile = optionalFile.orElseThrow();
      dumpFile.getParentFile().mkdirs();
      fileWriter.accept(dumpFile);
      val relativePath = basePath.toUri().relativize(dumpFile.toURI()).getPath();
      currentStep.addDump(operation, relativePath);
    }
  }

  private Optional<File> getScenarioDumpFile(String fileName) {
    if (this.currentScenario == null || this.currentScenario.getSteps().isEmpty()) {
      return Optional.empty();
    }

    val scenarioBasePath = getScenarioBasePath(this.currentScenario);
    val dumpIdx = dumpCounter++;
    val idxFileName = format("{0}_{1}", dumpIdx, fileName);

    val dumpFilePath = Path.of(scenarioBasePath, idxFileName);
    val dumpFile = dumpFilePath.toFile();
    return Optional.of(dumpFile);
  }

  private String getScenarioBasePath(ScenarioDump scenario) {
    //    val featureDir = scenario.getId().split(";")[0];  // should not be required
    val scenarioId = scenario.getName().toLowerCase().replace(" ", "_");
    return Path.of(basePath.toString(), scenarioId).toString();
  }

  @SneakyThrows
  public void writeDumpSummary() {
    val mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
    val indexFile = basePath.resolve("fhir_dump.json").toFile();
    this.index.setRecorded(new Date());
    mapper.writeValue(indexFile, this.index);

    for (val scenario : this.scenarios) {
      val scenarioFile = Path.of(getScenarioBasePath(scenario), "index.json").toFile();
      if (scenarioFile.getParentFile().exists() && scenarioFile.getParentFile().isDirectory()) {
        mapper.writeValue(scenarioFile, scenario);
      }
    }
  }
}
