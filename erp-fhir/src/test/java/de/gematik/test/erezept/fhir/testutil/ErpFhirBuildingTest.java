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

import static de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory.ERP_FHIR_PROFILES_CONFIG;

import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory;

public abstract class ErpFhirBuildingTest {
  public static final String ERP_FHIR_PROFILES_TOGGLE =
      ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;

  /**
   * Required to initialise the virtual default profile for the test which require default versions
   */
  protected static ProfilesConfigurator configuration =
      ProfilesConfigurator.getConfiguration(ERP_FHIR_PROFILES_CONFIG, ERP_FHIR_PROFILES_TOGGLE);
}
