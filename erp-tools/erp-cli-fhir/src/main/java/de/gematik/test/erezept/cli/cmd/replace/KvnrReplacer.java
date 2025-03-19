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
 */

package de.gematik.test.erezept.cli.cmd.replace;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.cli.converter.StringListConverter;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;

@Slf4j
@Command(
    name = "kvnr",
    description = "change KVNR values within KbvBundle or KbvPatient FHIR Resources",
    mixinStandardHelpOptions = true)
public class KvnrReplacer extends BaseResourceReplacer {

  private static final List<DeBasisProfilNamingSystem> KVID_SYSTEMS =
      List.of(
          DeBasisProfilNamingSystem.KVID,
          DeBasisProfilNamingSystem.KVID_GKV_SID,
          DeBasisProfilNamingSystem.KVID_PKV_SID);

  @CommandLine.Option(
      names = {"--to"},
      paramLabel = "<KVNR(s)>",
      type = List.class,
      converter = StringListConverter.class,
      required = true,
      description = "A comma separated list of KVNRs which should be set")
  private List<String> kvnrs;

  @Override
  public Integer call() throws Exception {
    val fhir = new FhirParser();

    while (inputOutputDirectory.hasNext()) {
      val f = inputOutputDirectory.next();
      try (val fis = new FileInputStream(f)) {
        val content = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
        val resource = fhir.decode(content);
        replace(fhir, resource, f.getName());
      }
    }

    return ExitCode.OK;
  }

  private void replace(FhirParser parser, Resource resource, String originalName) {
    if (resource instanceof KbvErpBundle kbvBundle) {
      replace(parser, kbvBundle, originalName);
    } else if (resource instanceof KbvPatient kbvPatient) {
      replace(parser, kbvPatient, originalName);
    } else {
      // Note: extend further resources which might contain a KVNR here!
      log.warn(
          "Given Resource {} of type {} ({}) is not supported for this operation",
          originalName,
          resource.getResourceType(),
          resource.getClass().getSimpleName());
    }
  }

  private void replace(FhirParser parser, KbvPatient kbvPatient, String originalName) {
    kvnrs.forEach(
        kvnr -> {
          val hasChanged = replaceKvnr(kbvPatient, kvnr);
          writeOnChange(parser, kbvPatient, originalName, kvnr, hasChanged);
        });
  }

  private void replace(FhirParser parser, KbvErpBundle kbvBundle, String originalName) {
    kvnrs.forEach(
        kvnr -> {
          val hasChanged = replaceKvnr(kbvBundle.getPatient(), kvnr);
          writeOnChange(parser, kbvBundle, originalName, kvnr, hasChanged);
        });
  }

  private boolean replaceKvnr(KbvPatient patient, String kvnr) {
    val hasChanged = new AtomicReference<>(false);
    patient.getIdentifier().stream()
        .filter(identifier -> KVID_SYSTEMS.stream().anyMatch(ns -> ns.matches(identifier)))
        .findAny()
        .ifPresent(
            identifier -> {
              identifier.setValue(kvnr);
              hasChanged.set(true);
            });
    return hasChanged.get();
  }

  private void writeOnChange(
      FhirParser parser, Resource resource, String originalName, String kvnr, boolean hasChanged) {
    if (hasChanged) {
      log.info(
          "Replace KVNR to {} in {} ({})", kvnr, resource.getClass().getSimpleName(), originalName);
      val encodingType = EncodingType.fromString(originalName);
      val content = parser.encode(resource, encodingType, true);
      inputOutputDirectory.writeFile(format("{0}_{1}", kvnr, originalName), content);
    } else {
      log.warn("{} does not have any KVID Identifier", resource.getClass().getSimpleName());
    }
  }
}
