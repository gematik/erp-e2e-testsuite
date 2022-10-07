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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.PrescribeUseCase;
import jakarta.ws.rs.WebApplicationException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Getter
@Slf4j
public class KthonRunner implements Callable<Integer> {

  @CommandLine.Option(
      names = "--failfast",
      paramLabel = "FAIL_FAST",
      type = Boolean.class,
      description = "Force the App to fail on first Error (default=false)")
  private boolean failFast = false;

  @CommandLine.Option(
      names = "--conf",
      paramLabel = "CONFIG",
      type = Path.class,
      required = true,
      description = "Path to a Configuration File")
  private Path config;

  @CommandLine.Option(
      names = "--output",
      type = Path.class,
      description = "Directory to store the output-file (default=current working directory)")
  private Path outdir = Path.of(System.getProperty("user.dir"));

  @CommandLine.ArgGroup(exclusive = false, multiplicity = "1")
  private PrescriptionFileSelector fileSelector = new PrescriptionFileSelector();

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   */
  @Override
  public Integer call() {
    int returnCode = 0;
    val configFile = config.toFile();
    log.info("Initialize Erp-Client with Config from " + configFile.getAbsolutePath());
    TestsuiteConfiguration.getInstance(configFile);
    val ctx = ActorContext.getInstance();
    val doc = ctx.getDoctors().get(0);

    val kbvBundleFileProviders = fileSelector.getKbvBundels(doc.getClient().getFhir());

    int numKbvBundles = kbvBundleFileProviders.size();
    log.info(format("Executing {0} KBV Bundle(s)", numKbvBundles));

    int prescriptions = 0;
    int errorCounter = 0;
    final List<String> failedExamples = new LinkedList<>();

    for (val provider : kbvBundleFileProviders) {

      try {
        val kbvBundle = provider.getKbvBundle();

        val resp = PrescribeUseCase.issuePrescription(doc, kbvBundle);

        log.info(
            format(
                "FD answered for KBV Bundle {0} with {1}",
                provider.getFileName(), resp.getStatus()));
        prescriptions++;
      } catch (WebApplicationException wae) {
        errorCounter++;
        failedExamples.add(provider.getFileName());
        log.error(
            format(
                "Error while creating Prescription for {0} with {1}",
                provider, wae.getResponse().getEntity()));
        if (failFast) throw wae;
        returnCode = 1;
      } catch (Exception e) {
        errorCounter++;
        failedExamples.add(provider.getFileName());
        log.error(
            format("Error while creating Prescription for {0} with {1}", provider, e.getMessage()));
        if (failFast) throw new RuntimeException(e); // NOSONAR no need! if failFast just stop!
        returnCode = 2;
      }
    }

    val percentage = 100.0 / numKbvBundles * prescriptions;
    log.info(
        format(
            "Successfully issued {0} out of {1} (failed {2}) ({3}%) Prescriptions",
            prescriptions, numKbvBundles, errorCounter, percentage));

    writePrescriptionSummary();
    if (!failedExamples.isEmpty()) {
      log.warn(format("Failed Examples: {0}", String.join("\n", failedExamples)));
    }

    return returnCode;
  }

  @SneakyThrows
  private void writePrescriptionSummary() {
    val mapper =
        new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    val prescriptions = ActorContext.getInstance().getPrescriptions();
    val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
    val fileName = format("kthon_{0}.json", timestamp);
    val out = Path.of(outdir.toAbsolutePath().toString(), fileName).toFile();
    mapper.writerWithDefaultPrettyPrinter().writeValue(out, prescriptions);
  }
}
