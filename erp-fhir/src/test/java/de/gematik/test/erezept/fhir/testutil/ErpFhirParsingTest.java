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

package de.gematik.test.erezept.fhir.testutil;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.bbriccs.utils.StopwatchUtil;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@SuppressWarnings({"java:S2187"})
public abstract class ErpFhirParsingTest extends ErpFhirBuildingTest {

  protected static FhirParser parser;

  static {
    // TODO: measure time of parser initialization
    val measurement = StopwatchUtil.measure(() -> new FhirParser());

    log.info("Initialized FhirParser in {}", measurement.duration());
    parser = measurement.response();
    //    parser = new FhirParser();
  }

  protected <T extends Resource> T getDecodedFromPath(
      Class<T> expectedClass, @NonNull final String path) {
    return parser.decode(expectedClass, ResourceLoader.readFileFromResource(path));
  }
}
