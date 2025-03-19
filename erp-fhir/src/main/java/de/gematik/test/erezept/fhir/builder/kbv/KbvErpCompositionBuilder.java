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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import org.hl7.fhir.r4.model.CanonicalType;

public class KbvErpCompositionBuilder
    extends KbvBaseCompositionBuilder<KbvItaErpVersion, KbvErpCompositionBuilder> {

  private KbvErpCompositionBuilder() {
    super();
    this.version = KbvItaErpVersion.getDefaultVersion();
  }

  protected static KbvErpCompositionBuilder builder() {
    return new KbvErpCompositionBuilder();
  }

  @Override
  protected CanonicalType getProfile() {
    return KbvItaErpStructDef.COMPOSITION.asCanonicalType(version);
  }

  @Override
  protected String getFormularArtCode() {
    return "e16A";
  }

  @Override
  protected String getTitle() {
    return "elektronische Arzneimittelverordnung";
  }

  @Override
  protected WithCodeSystem getSectionCodeSystem() {
    return KbvCodeSystem.SECTION_TYPE;
  }
}
