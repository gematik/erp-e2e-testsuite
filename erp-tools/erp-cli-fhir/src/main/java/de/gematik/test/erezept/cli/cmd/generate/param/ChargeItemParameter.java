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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.AccessCode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

public class ChargeItemParameter implements BaseResourceParameter {

  @CommandLine.ArgGroup(heading = "Prescription Information\n")
  private PrescriptionIdParameter prescriptionIdParameter = new PrescriptionIdParameter();

  @CommandLine.ArgGroup(heading = "Marking Flags\n")
  private MarkingFlagParameter markingFlagParameter;

  @Mixin private KvnrParameter kvnrParameter;

  @CommandLine.Option(
      names = {"--assigner", "--assigner-display"},
      paramLabel = "<DISPLAY>",
      type = String.class,
      description = "The name of the assigning insurance coverage")
  private String assignerName = GemFaker.insuranceName();

  @CommandLine.Option(
      names = {"--enterer", "--telematik-id"},
      paramLabel = "<TI-ID>",
      type = String.class,
      description = "The TelematikID of the enterer of this chargeitem which is usually a pharmacy")
  private String telematikId = GemFaker.fakerTelematikId();

  @CommandLine.Option(
      names = {"--accesscode", "--ac"},
      paramLabel = "<ACCESS-CODE>",
      type = AccessCode.class,
      description = "The AccessCode for the ChargeItem (will be set only in new profiles)")
  private AccessCode accessCode = AccessCode.random();

  public ErxChargeItem createChargeItem() {
    // well, not nice, but we have to deal with it for now because we need to contain a DavBundle
    val fhir = new FhirParser();

    val prescriptionId = prescriptionIdParameter.getPrescriptionId();
    val davBundle = DavPkvAbgabedatenFaker.builder(prescriptionId).fake();
    // only encoded, but not signed because we don't have a signer here
    val encodedDavBundle =
        fhir.encode(davBundle, EncodingType.XML).getBytes(StandardCharsets.UTF_8);
    return ErxChargeItemFaker.builder()
        .withPrescriptionId(prescriptionId)
        .withEnterer(telematikId)
        .withSubject(kvnrParameter.getKvnr(), assignerName)
        .withAccessCode(accessCode)
        .withAbgabedatensatz(davBundle.getReference(), encodedDavBundle)
        .withMarkingFlag(
            Optional.ofNullable(markingFlagParameter)
                .map(MarkingFlagParameter::createFlags)
                .orElse(null)) // required to overwrite the faker value
        .fake();
  }
}
