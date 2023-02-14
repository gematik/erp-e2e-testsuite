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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
public class KthonValidator implements Callable<Integer> {

  @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
  private PrescriptionFileSelector fileSelector = new PrescriptionFileSelector();

  @CommandLine.Option(
      names = "--output",
      type = Path.class,
      description = "Directory to store the output-file (default=current working directory)")
  private Path outdir;

  @CommandLine.Option(
      names = "--showall",
      type = Boolean.class,
      description = "Show also validation result for valid examples (default=false)")
  private boolean showValidFiles = false;

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws Exception if unable to compute a result
   */
  @Override
  public Integer call() throws Exception {
    val parser = new FhirParser();
    val providers = fileSelector.getKbvBundels(parser);

    for (val bundleProvider : providers) {
      log.info("Validating: " + bundleProvider.getFileName());

      val vr = bundleProvider.validateKbvBundle();

      // give some help on debugging if still errors in "valid resources"
      if (!vr.isSuccessful()) {
        log.error(
            format(
                "\nYou have {0} errors in File {1}",
                vr.getMessages().size(), bundleProvider.getFileName()));
        vr.getMessages().forEach(m -> log.error(m.getMessage()));
        writeBundle(bundleProvider);
      } else {
        if (showValidFiles) {
          System.out.println(
              format(
                  "{0} is valid FHIR conforming E-Rezept profiles", bundleProvider.getFileName()));
          writeBundle(bundleProvider);
        }
      }
    }

    return 0;
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  private void writeBundle(KbvBundleProvider provider) {
    if (outdir != null) {
      val out = Path.of(outdir.toAbsolutePath().toString(), provider.getFileName()).toFile();
      try (val writer = new BufferedWriter(new FileWriter(out))) {
        writer.write(provider.getKbvBundleContent());
      }
    }
  }
}
