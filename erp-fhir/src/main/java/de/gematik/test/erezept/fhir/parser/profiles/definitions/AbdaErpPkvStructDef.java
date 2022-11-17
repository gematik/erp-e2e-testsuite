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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AbdaErpPkvStructDef implements IStructureDefinition<AbdaErpPkvVersion> {
  PKV_ABGABEDATENSATZ(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle"),
  PKV_ABGABEDATEN_COMPOSITION(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenComposition"),
  PKV_ABGABEINFORMATIONEN(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abgabeinformationen"),
  PKV_ABRECHNUNGSZEILEN(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen"),
  PKV_ABRECHNUNGSTYP(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-EX-ERP-AbrechnungsTyp"),
  APOTHEKE("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Apotheke"),
  ;

  private final String canonicalUrl;
}