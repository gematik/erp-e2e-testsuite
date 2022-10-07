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

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;

@AllArgsConstructor
public class KbvBundleProvider {

  private final Path kbvBundlePath;
  @Nullable private final Path coveragePath;
  private final FhirParser parser;

  public KbvErpBundle getKbvBundle() {
    val content = readFile(kbvBundlePath);
    val bundle = parser.decode(KbvErpBundle.class, content);

    if (coveragePath != null) {
      val coverage = readCoverage();
      bundle.changeCoverage(coverage);
    }

    return bundle;
  }

  public String getKbvBundleContent() {
    val bundle = getKbvBundle();
    return parser.encode(bundle, EncodingType.XML);
  }

  public ValidationResult validateKbvBundle() {
    val content = getKbvBundleContent();
    return parser.validate(content);
  }

  public String getFileName() {
    val encoding = EncodingType.fromString(kbvBundlePath.toFile().getName());
    return getFileName(encoding);
  }

  public String getFileName(EncodingType encodingType) {
    String ret = fileNameWithoutExtension(kbvBundlePath);
    if (coveragePath != null) {
      ret += format("_{0}", fileNameWithoutExtension(coveragePath));
    }

    return ret + "." + encodingType.toFileExtension();
  }

  private Coverage readCoverage() {
    val content = readFile(coveragePath);
    return parser.decode(Coverage.class, content);
  }

  @SneakyThrows
  private static String readFile(Path filePath) {
    try (val stream = Files.lines(filePath)) {
      return stream.parallel().collect(Collectors.joining("\n"));
    }
  }

  private static String fileNameWithoutExtension(Path filePath) {
    val fileName = filePath.toFile().getName();
    return fileName.replaceAll("(\\.\\w{3,4})", "");
  }
}
