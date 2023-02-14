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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.cmd.param.InputDirectoryParameter;
import de.gematik.test.erezept.cli.cmd.param.OutputDirectoryParameter;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Slf4j
@Command(
    name = "transcode",
    description = "transcode FHIR resources from xml to json and vice versa",
    mixinStandardHelpOptions = true)
public class EncodingTranscoder implements Callable<Integer> {

  @Option(
      names = "--pretty",
      paramLabel = "Pretty Print",
      type = Boolean.class,
      description = "Pretty Print the Output")
  private boolean prettyPrint = true;

  @Mixin private InputDirectoryParameter inputDirectory;
  @Mixin private OutputDirectoryParameter outputDirectory;

  @Override
  public Integer call() throws Exception {
    val fhir = new FhirParser();

    while (inputDirectory.hasNext()) {
      val f = inputDirectory.next();
      val originalFileName = f.getName();
      val originalEncoding = EncodingType.fromString(originalFileName);
      val flippedEncoding = EncodingUtil.flipEncoding(originalEncoding);
      log.info(
          format(
              "read {0} as {1} and transform to {2}",
              f.getName(), originalEncoding, flippedEncoding));

      String originalContent;
      try (val fis = new FileInputStream(f)) {
        originalContent = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
      }
      val originalResource = fhir.decode(originalContent, originalEncoding);

      val flippedFileName =
          originalFileName.replace(
              originalEncoding.toFileExtension(), flippedEncoding.toFileExtension());
      val flippedContent = fhir.encode(originalResource, flippedEncoding, prettyPrint);
      outputDirectory.writeFile(flippedFileName, flippedContent);
    }

    return 0;
  }
}
