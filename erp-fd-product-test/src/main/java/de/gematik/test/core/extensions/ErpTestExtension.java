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

package de.gematik.test.core.extensions;

import static java.text.MessageFormat.format;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.exceptions.MissingAnnotationException;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.thucydides.core.steps.StepEventBus;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

@Slf4j
public class ErpTestExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback, BeforeAllCallback, AfterAllCallback {

  private static final String START_TIME = "start time";

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    getStore(context).put(START_TIME, System.currentTimeMillis());
    val testMethod = context.getRequiredTestMethod();
    val id = getRequiredAnnotation(testMethod, TestcaseId.class).value();
    
    log.info(format("# Start Testcase {0} / {1} #", id, context.getDisplayName()));

    CoverageReporter.getInstance().startTestcase(id);
    StepEventBus.getEventBus().enableSoftAsserts();
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    val testMethod = context.getRequiredTestMethod();
    val id = testMethod.getAnnotation(TestcaseId.class).value();
    long startTime = getStore(context).remove(START_TIME, long.class);
    long duration = System.currentTimeMillis() - startTime;

    CoverageReporter.getInstance().finishTestcase();
    log.info(format("# Finished Testcase {0} after {1} ms. #", id, duration));
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    StopwatchProvider.init();
  }
  
  @Override
  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  public void afterAll(ExtensionContext extensionContext) {
    val coverageAsJson = CoverageReporter.getInstance().serializeReport();

    val outDirPath = Path.of("target", "site", "serenity", "coverage");
    val rawJsonOutput = Path.of(outDirPath.toString(), "coverage_report.json");
    val jsOutput = Path.of(outDirPath.toString(), "coverage.js");

    // make sure previous report data is deleted even if mvn clean was not executed!
    rawJsonOutput.toFile().delete();
    jsOutput.toFile().delete();
    outDirPath.toFile().delete();
    if (outDirPath.toFile().mkdirs()) {
      try (val fw = new FileWriter(rawJsonOutput.toFile())) {
        fw.write(coverageAsJson.toString(4));
      }
      writeToJavaScript(coverageAsJson, jsOutput);
    } else {
      throw new IOException(format("Unable to write File {0} ", rawJsonOutput));
    }
    
    StopwatchProvider.close();
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  private void writeToJavaScript(JSONObject coverage, Path jsOutput) {
    val templateStream =
        Objects.requireNonNull(
            ErpTestExtension.class.getClassLoader().getResourceAsStream("coverage.js"));
    try (val reader = new BufferedReader(new InputStreamReader(templateStream))) {
      val script =
          reader
              .lines()
              .collect(Collectors.joining("\n"))
              .replace(
                  "const rawCoverageData = {}", format("const rawCoverageData = {0}", coverage));

      try (val fw = new FileWriter(jsOutput.toFile())) {
        fw.write(script);
      }
    }
  }

  private Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
  }

  private <T extends Annotation> T getRequiredAnnotation(Method method, Class<T> annotationClass) {
    val a = method.getAnnotation(annotationClass);
    if (a == null) {
      throw new MissingAnnotationException(method, annotationClass);
    }
    return a;
  }
}
