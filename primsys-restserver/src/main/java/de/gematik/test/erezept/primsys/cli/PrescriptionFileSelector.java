/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Getter
@Slf4j
public class PrescriptionFileSelector {

  @CommandLine.Option(
      names = "--bundels",
      type = Path.class,
      required = true,
      description = "Define a directory with KBV-Bundels to execute")
  private Path bundelsPath;

  @CommandLine.Option(
      names = "--bundels-regex",
      type = String.class,
      description = "Define a RegEx to match KBV-Bundels from --bundels")
  private String bundelsRegex;

  @CommandLine.Option(
      names = "--coverages",
      type = Path.class,
      description = "Define a directory with Coverages to execute")
  private Path coveragesPath;

  @CommandLine.Option(
      names = "--coverages-regex",
      type = String.class,
      description = "Define a RegEx to match Coverages from --coverages")
  private String coveragesRegex;

  public List<KbvBundleProvider> getKbvBundels(FhirParser parser) {
    var bundleFiles = getFiles(bundelsPath, bundelsRegex);

    val providers = new ArrayList<KbvBundleProvider>();
    if (coveragesPath != null) {
      val coverageFiles = getFiles(coveragesPath, coveragesRegex);
      bundleFiles.forEach(
          bundlePath -> {
            coverageFiles.forEach(
                covPath -> providers.add(new KbvBundleProvider(bundlePath, covPath, parser)));
          });
    } else {
      bundleFiles.forEach(
          bundlePath -> providers.add(new KbvBundleProvider(bundlePath, null, parser)));
    }

    return providers;
  }

  private static List<Path> getFiles(Path directory, @Nullable String regex) {
    var retList = getFiles(directory);

    if (regex != null) {
      val pattern = Pattern.compile(regex);
      retList =
          retList.stream()
              .filter(filePath -> pattern.matcher(filePath.toFile().getName()).matches())
              .collect(Collectors.toList());
    }
    return retList;
  }

  @SneakyThrows
  private static List<Path> getFiles(Path directory) {
    log.info(format("Providing all files from {0}", directory.toAbsolutePath()));
    val dirFile = directory.toFile();
    if (!dirFile.isDirectory()) {
      throw new RuntimeException(
          format("Given Path {0} is not a directory", dirFile.getAbsolutePath()));
    }

    try (val files = Files.walk(directory)) {
      return files
          .filter(Files::isRegularFile)
          .filter(
              filePath ->
                  filePath.toString().endsWith("xml") || filePath.toString().endsWith("json"))
          .collect(Collectors.toList());
    }
  }
}
