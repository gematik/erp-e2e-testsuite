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

package de.gematik.test.erezept.lei.cfg;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.cfg.ErpClientConfiguration;
import lombok.Data;
import lombok.val;

@Data
public class PatientConfiguration extends ActorConfiguration {

  private String egkIccsn;

  // overrides the xml-defaults from ActorConfiguration
  private String acceptMime = "application/fhir+json";
  private String sendMime = "application/fhir+json";

  @Override
  public ErpClientConfiguration toErpClientConfig(
      EnvironmentConfiguration environment, ClientType type) {
    val erpClientConfig = super.toErpClientConfig(environment, type);
    erpClientConfig.setXApiKey(environment.getInternet().getXapiKey());
    return erpClientConfig;
  }
}
