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

package de.gematik.test.erezept.fhir.parser;

import de.gematik.test.erezept.fhir.parser.profiles.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.resources.dav.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import java.util.*;
import java.util.Map.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public class ErpFhirTypeHints {

  private static final Map<IStructureDefinition<?>, Class<? extends Resource>> TYPE_HINTS =
      Map.of(
          KbvItaErpStructDef.BUNDLE, KbvErpBundle.class,
          KbvItaForStructDef.PRACTITIONER, KbvPractitioner.class,
          KbvItaForStructDef.ORGANIZATION, MedicalOrganization.class,
          KbvItaForStructDef.COVERAGE, KbvCoverage.class,
          KbvItaForStructDef.PATIENT, KbvPatient.class,
          KbvItaErpStructDef.PRESCRIPTION, KbvErpMedicationRequest.class,
          KbvItaErpStructDef.MEDICATION_PZN, KbvErpMedication.class,
          AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ, DavAbgabedatenBundle.class);

  public Class<? extends Resource> getType(String content) {
    val profileOpt = ProfileExtractor.extractProfile(content);
    if (profileOpt.isPresent()) {
      val profile = profileOpt.get();
      val classType =
          TYPE_HINTS.entrySet().stream()
              .filter(entry -> entry.getKey().match(profile))
              .map(Entry::getValue)
              .findFirst();
      if (classType.isPresent()) {
        return classType.get();
      }
    }
    return null; // if no hint found, let HAPI choose a base class
  }
}
