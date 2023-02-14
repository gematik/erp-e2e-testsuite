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

package de.gematik.test.erezept.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class JWTPayload {

  private String sub;
  private String professionOID;
  private String organizationName;

  @JsonProperty("idNummer")
  private String identifier;

  private List<String> amr;
  private String iss;

  @JsonProperty("given_name")
  private String givenName;

  @JsonProperty("client_id")
  private String clientId;

  private String acr;
  private String aud;
  private String azp;
  private String scope;

  @JsonProperty("auth_time")
  private long authTime;

  private long exp;

  @JsonProperty("family_name")
  private String familyName;

  private long iat;
  private String jti;
}
