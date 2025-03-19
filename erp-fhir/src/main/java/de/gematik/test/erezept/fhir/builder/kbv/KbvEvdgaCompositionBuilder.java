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
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvEvdgaCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItvEvdgaVersion;
import org.hl7.fhir.r4.model.CanonicalType;

public class KbvEvdgaCompositionBuilder
    extends KbvBaseCompositionBuilder<KbvItvEvdgaVersion, KbvEvdgaCompositionBuilder> {

  public static KbvEvdgaCompositionBuilder builder() {
    return new KbvEvdgaCompositionBuilder();
  }

  private KbvEvdgaCompositionBuilder() {
    super();
    this.version = KbvItvEvdgaVersion.getDefaultVersion();
  }

  @Override
  protected CanonicalType getProfile() {
    return KbvItvEvdgaStructDef.COMPOSITION.asCanonicalType(this.version);
  }

  @Override
  protected String getFormularArtCode() {
    return "e16D";
  }

  @Override
  protected String getTitle() {
    return "elektronische Verordnung digitaler Gesundheitsanwendungen";
  }

  @Override
  protected WithCodeSystem getSectionCodeSystem() {
    return KbvEvdgaCodeSystem.SECTION_TYPE;
  }
}
