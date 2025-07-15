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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.core.extensions;

import static java.text.MessageFormat.format;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.exceptions.MissingAnnotationException;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

@Slf4j
public class ErpTestExtension
    implements BeforeTestExecutionCallback,
        AfterTestExecutionCallback,
        BeforeAllCallback,
        AfterAllCallback {

  private static final String START_TIME = "start time";

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    getStore(context).put(START_TIME, System.currentTimeMillis());
    val testMethod = context.getRequiredTestMethod();
    val id = getRequiredAnnotation(testMethod, TestcaseId.class).value();

    log.info(format("# Start Testcase {0} / {1} #", id, context.getDisplayName()));

    CoverageReporter.getInstance().startTestcase(id);
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
    val reporter = CoverageReporter.getInstance();

    val outDirPath = Path.of("target", "site", "serenity", "coverage");
    val rawJsonOutput = Path.of(outDirPath.toString(), "coverage_report.json");

    // make sure previous report data is deleted even if mvn clean was not executed!
    rawJsonOutput.toFile().delete();
    outDirPath.toFile().delete();
    if (outDirPath.toFile().mkdirs()) {
      try (val fw = new FileWriter(rawJsonOutput.toFile())) {
        reporter.writeToFile(fw);
      }
    } else {
      throw new IOException(format("Unable to write File {0} ", rawJsonOutput));
    }

    StopwatchProvider.close();
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
