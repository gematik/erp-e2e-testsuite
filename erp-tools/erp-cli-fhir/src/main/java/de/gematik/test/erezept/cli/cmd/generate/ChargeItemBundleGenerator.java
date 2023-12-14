/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.cli.cmd.generate;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.param.InputDirectoryParameter;
import de.gematik.test.erezept.cli.param.OutputDirectoryParameter;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Slf4j
@Command(
    name = "erxchargeitembundle",
    aliases = {"chargeitembundle"},
    description = "generate exemplary ErxChargeItemBundle FHIR Resources",
    mixinStandardHelpOptions = true)
public class ChargeItemBundleGenerator extends BaseResourceGenerator {

  @Mixin private InputDirectoryParameter inputDirectory;

  @Override
  public Integer call() throws Exception {
    if (this.outputDirectory == null || !this.outputDirectory.hasSetPath()) {
      // if no output path given, use the same as the input path
      this.outputDirectory = new OutputDirectoryParameter(inputDirectory.getInputDirectory());
    }

    val bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(BundleType.SEARCHSET);

    // well, not nice, but we have to deal with the charge items
    val fhir = new FhirParser();

    var totalCounter = 0;
    while (inputDirectory.hasNext()) {
      val f = inputDirectory.next();
      try (val fis = new FileInputStream(f)) {
        var content = new String(fis.readAllBytes(), StandardCharsets.UTF_8);

        // this hack is required because HAPI does not remove contained resources properly when read
        // from file
        content = content.replace("\n", "").replaceAll("<contained>.*</contained>", "");

        val chargeItem = fhir.decode(ErxChargeItem.class, content);

        // remove all information which are not required within the bundle
        chargeItem.getSupportingInformation().removeIf(si -> !si.getDisplay().equals("Binary"));
        if (chargeItem.isFromNewProfiles()) {
          chargeItem.setEnterer(null);
        }
        chargeItem.removeAccessCode();
        chargeItem.removeContainedResources();

        val entry =
            bundle.addEntry().setFullUrl(format("https://gematik.de/{0}", chargeItem.getId()));
        entry.setResource(chargeItem);
        entry.getSearch().setMode(SearchEntryMode.MATCH);
        totalCounter++;
      }
    }
    bundle.setTotal(totalCounter);
    return this.create("Gematik", () -> bundle);
  }
}
