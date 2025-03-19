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

package de.gematik.test.erezept.eml.fhir.testutil;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;

@SuppressWarnings({"java:S2187"})
public abstract class EpaFhirParsingTest extends FhirTest {

  protected static final FhirCodec epaFhir = EpaFhirFactory.create();

  @Override
  protected void initialize() {
    this.fhirCodec = epaFhir;
  }
}
