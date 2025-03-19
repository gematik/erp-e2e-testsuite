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

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunicationReplyMessage(
    int version,
    String supplyOptionsType,
    String info_text,
    String url,
    String pickUpCodeHR,
    String pickUpCodeDMC)
    implements CommunicationStructuredMessage {

  public CommunicationReplyMessage(
      SupplyOptionsType supplyOptionsType,
      String infoText,
      String url,
      String pickUpCodeHR,
      String pickUpCodeDMC) {
    this(
        CommunicationStructuredMessage.DEFAULT_VERSION,
        supplyOptionsType.getLabel(),
        infoText,
        url,
        pickUpCodeHR,
        pickUpCodeDMC);
  }

  public CommunicationReplyMessage(SupplyOptionsType supplyOptionsType, String infoText) {
    this(supplyOptionsType, infoText, null, null, null);
  }

  public CommunicationReplyMessage() {
    this(GemFaker.randomElement(SupplyOptionsType.values()), GemFaker.getFaker().dune().quote());
  }
}
