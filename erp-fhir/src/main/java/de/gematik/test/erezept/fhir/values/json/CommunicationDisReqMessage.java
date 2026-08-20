/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.values.json;

import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunicationDisReqMessage(
    int version,
    String supplyOptionsType,
    String communicationType,
    UUID transactionID,
    String name,
    String firstname,
    String lastname,
    List<String> addressLines,
    String address,
    String postcode,
    String city,
    String country,
    String hint,
    String phone,
    String text,
    String email,
    String url,

    // V1
    String pickUpCodeHR,
    String pickUpCodeDMC,

    // V3
    String pickupCodeHR,
    String pickupCodeDMC)
    implements CommunicationStructuredMessage {

  public static CommunicationDisReqMessageV1Builder forV1() {
    return new CommunicationDisReqMessageV1Builder();
  }

  public static class CommunicationDisReqMessageV1Builder {

    private int version = 1;

    private String supplyOptionsType = randomElement(SupplyOptionsType.values()).getLabel();

    private String name = GemFaker.fakerFirstName() + " " + GemFaker.fakerLastName();
    private List<String> addressLines;
    private String phone = GemFaker.fakerPhone();
    private String hint = GemFaker.getFaker().lorem().sentence();

    private String pickUpCodeHR;
    private String pickUpCodeDMC;

    public CommunicationDisReqMessageV1Builder version(int version) {
      this.version = version;
      return this;
    }

    public CommunicationDisReqMessageV1Builder supplyOptionsType(
        SupplyOptionsType supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType.getLabel();
      return this;
    }

    public CommunicationDisReqMessageV1Builder supplyOptionsType(String supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType;
      return this;
    }

    public CommunicationDisReqMessageV1Builder name(String name) {
      this.name = name;
      return this;
    }

    public CommunicationDisReqMessageV1Builder addressLines(List<String> addressLines) {
      this.addressLines = addressLines;
      return this;
    }

    public CommunicationDisReqMessageV1Builder addressLines(String... addressLines) {
      this.addressLines = List.of(addressLines);
      return this;
    }

    public CommunicationDisReqMessageV1Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public CommunicationDisReqMessageV1Builder hint(String hint) {
      this.hint = hint;
      return this;
    }

    public CommunicationDisReqMessageV1Builder pickUpCodeHR(String pickUpCodeHR) {
      this.pickUpCodeHR = pickUpCodeHR;
      return this;
    }

    public CommunicationDisReqMessageV1Builder pickUpCodeDMC(String pickUpCodeDMC) {
      this.pickUpCodeDMC = pickUpCodeDMC;
      return this;
    }

    public CommunicationDisReqMessage build() {
      return new CommunicationDisReqMessage(
          version,
          supplyOptionsType,
          null,
          null,
          name,
          null,
          null,
          addressLines,
          null,
          null,
          null,
          null,
          hint,
          phone,
          null,
          null,
          null,
          pickUpCodeHR,
          pickUpCodeDMC,
          null,
          null);
    }
  }

  public static CommunicationDisReqMessageV3Builder forV3() {
    return new CommunicationDisReqMessageV3Builder();
  }

  public static class CommunicationDisReqMessageV3Builder {

    private String communicationType;
    private String supplyOptionsType;

    private UUID transactionID = UUID.randomUUID();

    private String firstname;
    private String lastname;
    private String address;
    private String postcode;
    private String city;
    private String country;

    private String phone;
    private String email;
    private String text;
    private String hint;

    private String url;

    private String pickupCodeHR;
    private String pickupCodeDMC;

    public CommunicationDisReqMessageV3Builder communicationType(String communicationType) {
      this.communicationType = communicationType;
      return this;
    }

    public CommunicationDisReqMessageV3Builder communicationType(
        CommunicationPayloadType communicationPayloadType) {
      this.communicationType = communicationPayloadType.getLabel();
      return this;
    }

    public CommunicationDisReqMessageV3Builder supplyOptionsType(
        SupplyOptionsType supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType.getLabel();
      return this;
    }

    public CommunicationDisReqMessageV3Builder supplyOptionsType(String supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType;
      return this;
    }

    public CommunicationDisReqMessageV3Builder transactionID(UUID transactionID) {
      this.transactionID = transactionID;
      return this;
    }

    public CommunicationDisReqMessageV3Builder firstname(String firstname) {
      this.firstname = firstname;
      return this;
    }

    public CommunicationDisReqMessageV3Builder lastname(String lastname) {
      this.lastname = lastname;
      return this;
    }

    public CommunicationDisReqMessageV3Builder address(String address) {
      this.address = address;
      return this;
    }

    public CommunicationDisReqMessageV3Builder postcode(String postcode) {
      this.postcode = postcode;
      return this;
    }

    public CommunicationDisReqMessageV3Builder city(String city) {
      this.city = city;
      return this;
    }

    /**
     * Caused by Specification the CountryCode has to be a 2 or 3 digits Code!!
     *
     * @param countryCode IsoCountryCode
     * @return CommunicationDisReqMessageV3Builder
     */
    public CommunicationDisReqMessageV3Builder country(String countryCode) {
      this.country = countryCode;
      return this;
    }

    public CommunicationDisReqMessageV3Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public CommunicationDisReqMessageV3Builder email(String email) {
      this.email = email;
      return this;
    }

    public CommunicationDisReqMessageV3Builder text(String text) {
      this.text = text;
      return this;
    }

    public CommunicationDisReqMessageV3Builder hint(String hint) {
      this.hint = hint;
      return this;
    }

    public CommunicationDisReqMessageV3Builder url(String url) {
      this.url = url;
      return this;
    }

    public CommunicationDisReqMessageV3Builder pickupCodeHR(String pickupCodeHR) {
      this.pickupCodeHR = pickupCodeHR;
      return this;
    }

    public CommunicationDisReqMessageV3Builder pickupCodeDMC(String pickupCodeDMC) {
      this.pickupCodeDMC = pickupCodeDMC;
      return this;
    }

    public CommunicationDisReqMessage build() {
      if (this.communicationType != null
          && this.communicationType.equalsIgnoreCase("order")
          && this.supplyOptionsType == null)
        this.supplyOptionsType = randomElement(SupplyOptionsType.values()).getLabel();

      if (this.communicationType == null || this.communicationType.equalsIgnoreCase("text"))
        hint = null;
      return new CommunicationDisReqMessage(
          3,
          supplyOptionsType,
          communicationType,
          transactionID,
          null,
          firstname,
          lastname,
          null,
          address,
          postcode,
          city,
          country,
          hint,
          phone,
          text,
          email,
          url,
          null,
          null,
          pickupCodeHR,
          pickupCodeDMC);
    }
  }
}
