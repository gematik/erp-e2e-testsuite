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

package de.gematik.test.erezept.fhir.values.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunicationDisReqMessage(
    int version,
    String supplyOptionsType,
    String name,
    List<String> address,
    String hint,
    String phone)
    implements CommunicationStructuredMessage {

  public CommunicationDisReqMessage(
      SupplyOptionsType supplyOptionsType,
      String name,
      List<String> address,
      String hint,
      String phone) {
    this(
        CommunicationStructuredMessage.DEFAULT_VERSION,
        supplyOptionsType.getLabel(),
        name,
        address,
        hint,
        phone);
  }

  public CommunicationDisReqMessage(SupplyOptionsType supplyOptionsType, String hint) {
    this(supplyOptionsType, null, null, hint, null);
  }

  public CommunicationDisReqMessage() {
    this(
        GemFaker.randomElement(SupplyOptionsType.values()),
        GemFaker.fakerName(),
        List.of(GemFaker.fakerStreetName(), GemFaker.fakerZipCode(), GemFaker.fakerCity()),
        GemFaker.getFaker().dune().quote(),
        GemFaker.fakerPhone());
  }
}
