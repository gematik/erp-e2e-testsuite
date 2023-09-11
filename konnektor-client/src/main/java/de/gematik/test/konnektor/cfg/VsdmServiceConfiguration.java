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

package de.gematik.test.konnektor.cfg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import java.util.Base64;

public class VsdmServiceConfiguration {

  private final String version;
  private final String operator;
  private final String hMacKey;

  @JsonCreator
  public VsdmServiceConfiguration(
      @JsonProperty("hMacKey") String hMacKey,
      @JsonProperty("operator") String operator,
      @JsonProperty("version") String version) {
    this.hMacKey = hMacKey;
    this.operator = operator;
    this.version = version;
  }

  public VsdmService createDefault() {
    return new VsdmService(
        Base64.getDecoder().decode(hMacKey), operator.charAt(0), version.charAt(0));
  }

  public static VsdmServiceConfiguration createCfg() {
    return new VsdmServiceConfiguration(Base64.getEncoder().encodeToString(new byte[32]), "S", "1");
  }
}
