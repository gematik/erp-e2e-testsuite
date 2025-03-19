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

package de.gematik.test.erezept.fhir.testutil;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.NonNull;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings({"java:S2187"})
public abstract class ErpFhirParsingTest extends ErpFhirBuildingTest {

  protected static FhirParser parser;

  static {
    parser = new FhirParser();
  }

  protected <T extends Resource> T getDecodedFromPath(
      Class<T> expectedClass, @NonNull final String path) {
    return parser.decode(expectedClass, ResourceLoader.readFileFromResource(path));
  }
}
