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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.cli.description.FhirResourceDescriber;
import de.gematik.test.erezept.cli.indexmap.ExampleDetailsMap;
import de.gematik.test.erezept.cli.indexmap.ExampleEntry;
import de.gematik.test.erezept.cli.param.InputOutputDirectoryParameter;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Slf4j
@Command(
    name = "validate",
    description = "validate E-Rezept FHIR Resources",
    mixinStandardHelpOptions = true)
public class Validator implements Callable<Integer> {

  @Option(
      names = {"-s", "--severity"},
      paramLabel = "<SEVERITY>",
      type = ResultSeverityEnum.class,
      defaultValue = "WARNING",
      description =
          "Choose minimum severity to show from INFORMATION, WARNING, ERROR, FATAL"
              + " (default=${DEFAULT-VALUE})")
  private ResultSeverityEnum severity;

  @Mixin private InputOutputDirectoryParameter directories;

  private FhirParser fhir;

  @Override
  public Integer call() throws Exception {
    fhir = new FhirParser();
    val edm = ExampleDetailsMap.forCurrentUser();
    val descriptionCreator = new FhirResourceDescriber();

    while (directories.hasNext()) {
      val f = directories.next();

      try (val fis = new FileInputStream(f)) {
        val content = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
        log.info("Validate {}", f.getName());

        val resource = fhir.decode(content);
        val description = descriptionCreator.acceptResource(resource);

        val validationPair = validate(f, content, description);
        edm.addEntry(validationPair.getRight());

        val vr = validationPair.getLeft();
        if (!vr.isSuccessful()) {
          val errors =
              vr.getMessages().stream()
                  .filter(svm -> svm.getSeverity().equals(ResultSeverityEnum.ERROR))
                  .count();
          val sb = new StringBuilder(format("\n{0}", formatValidationHeader(f.getName(), errors)));
          vr.getMessages().stream()
              .filter(svm -> svm.getSeverity().ordinal() >= severity.ordinal())
              .forEach(
                  svm ->
                      sb.append(
                          format(
                              "\n -> {0} in {1} {2}: {3}",
                              formatSeverity(svm.getSeverity()),
                              formatValidationErrorLocation(svm.getLocationString()),
                              formatLineAndColumn(svm.getLocationLine(), svm.getLocationCol()),
                              svm.getMessage())));
          sb.append(format("\n{0}", formatValidationFooter(f.getName())));
          log.error(sb.toString());
        }
      }
    }

    edm.write(directories.getOut(), "validation_details.json");
    return 0;
  }

  private String formatSeverity(ResultSeverityEnum severityEnum) {
    val color =
        switch (severityEnum) {
          case INFORMATION -> Style.fg_cyan;
          case WARNING -> Style.fg_yellow;
          case ERROR, FATAL -> Style.fg_red;
        };
    return format("{0}{1}{2}", color.on(), severityEnum, Style.off(color));
  }

  private String formatValidationHeader(String filename, long numErrors) {
    val color = Style.fg_red;
    return format(
        "{0}<======== {1} contains {2} error(s) ========>{3}",
        color.on(), filename, numErrors, color.off());
  }

  private String formatValidationFooter(String filename) {
    val color = Style.fg_red;
    return format("{0}X======== {1} ========X{2}", color.on(), filename, color.off());
  }

  private String formatValidationErrorLocation(String location) {
    val style = Style.underline;
    return format("{0}{1}{2}", style.on(), location, style.off());
  }

  private String formatLineAndColumn(Integer line, Integer col) {
    if (line == null && col == null) {
      return ""; // no line and column given
    } else {
      val style = Style.fg_magenta;
      return format("{0}({1}:{2}){3}", style.on(), line, col, style.off());
    }
  }

  private Pair<ValidationResult, ExampleEntry> validate(
      File sourceFile, String content, String description) {
    val fileName = relativize(sourceFile);
    val encoding = EncodingType.guessFromContent(content);
    val result = fhir.validate(content);

    val entry = new ExampleEntry();
    entry.setFileType(encoding);
    entry.setFileName(fileName);
    entry.setDescription(description);
    entry.setValidationResults(result);
    return Pair.of(result, entry);
  }

  private String relativize(File sourceFile) {
    val base = directories.getIn().toUri();
    val source = sourceFile.toURI();
    if (base.equals(source)) {
      return sourceFile.getName();
    } else {
      return base.relativize(source).getPath();
    }
  }
}
