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

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.cli.param.InputOutputDirectoryParameter;
import de.gematik.test.erezept.fhir.anonymizer.AnonymizationType;
import de.gematik.test.erezept.fhir.anonymizer.AnonymizerFacade;
import de.gematik.test.erezept.fhir.anonymizer.BlackingStrategy;
import de.gematik.test.erezept.fhir.anonymizer.CharReplacementStrategy;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Slf4j
@Command(
    name = "anonymize",
    description = "anonymize FHIR resources",
    mixinStandardHelpOptions = true)
public class CliAnonymizer implements Callable<Integer> {

  @Option(
      names = {"--id-anonymization", "--ida"},
      paramLabel = "<TYPE>",
      type = AnonymizationType.class,
      description = "The Type of anonymization from ${COMPLETION-CANDIDATES}")
  private AnonymizationType idAnonymization = AnonymizationType.REPLACING;

  @Option(
      names = {"--blacking"},
      type = Boolean.class,
      defaultValue = "false",
      description =
          "When true (default=${DEFAULT-VALUE}) String values (like names, streets etc.) other than"
              + " IDs are simply blacked with '*' otherwise a character-substitution strategy is"
              + " applied")
  @Getter
  private Boolean blacking = false;

  @Option(
      names = "--pretty",
      paramLabel = "Pretty Print",
      type = Boolean.class,
      description = "Pretty Print the Output")
  private boolean prettyPrint = true;

  @Mixin protected InputOutputDirectoryParameter inputOutputDirectory;

  @Override
  public Integer call() throws Exception {
    val fhir = new FhirParser();
    val strategy = blacking ? new BlackingStrategy() : new CharReplacementStrategy();
    val anonymizer = new AnonymizerFacade(idAnonymization, strategy);

    while (inputOutputDirectory.hasNext()) {
      val f = inputOutputDirectory.next();

      val originalEncoding = EncodingType.fromString(f.getName());
      val anonymizedFileName = format("anonymized_{0}", f.getName());

      String originalContent;
      try (val fis = new FileInputStream(f)) {
        originalContent = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
      }
      val resource = fhir.decode(originalContent);
      val anonymized = anonymizer.anonymize(resource);

      if (anonymized) {
        log.info(
            "Anonymized {} -> {}/{}",
            f.getName(),
            inputOutputDirectory.getOut(),
            anonymizedFileName);
      } else {
        log.warn("{} was NOT anonymized", f.getName());
      }

      val anonymizedContent = fhir.encode(resource, originalEncoding, prettyPrint);
      inputOutputDirectory.writeFile(anonymizedFileName, anonymizedContent);
    }

    return 0;
  }
}
