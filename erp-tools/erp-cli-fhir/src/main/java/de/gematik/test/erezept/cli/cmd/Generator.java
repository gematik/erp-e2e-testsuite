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

import de.gematik.test.erezept.cli.cmd.generate.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Slf4j
@Command(
    name = "generate",
    description = "generate exemplary E-Rezept FHIR Resources",
    mixinStandardHelpOptions = true,
    subcommands = {
      KbvBundleGenerator.class,
      DavBundleGenerator.class,
      KbvPractitionerGenerator.class,
      KbvMedicalOrganizationGenerator.class,
      KbvPatientGenerator.class,
      KbvCoverageGenerator.class,
      KbvMedicationGenerator.class,
      KbvMedicationRequestGenerator.class,
      ChargeItemGenerator.class,
      ChargeItemBundleGenerator.class
    })
@Getter
public class Generator {}
