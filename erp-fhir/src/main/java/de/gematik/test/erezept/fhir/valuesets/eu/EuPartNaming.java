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

package de.gematik.test.erezept.fhir.valuesets.eu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EuPartNaming {
  REQUEST_DATA("requestData"),
  COUNTRY_CODE("countryCode"),
  EU_KVNR("kvnr"),
  REQUEST_TYPE("requesttype"),
  ACCESS_CODE("accessCode"),
  POINT_OF_CARE("pointOfCare"),
  HEALTHCARE_FACILITY_TYPE("healthcare-facility-type"),
  PRACTITIONER_NAME("practitionerName"),
  PRESCRIPTION_ID("prescription-id"),
  PRACTITIONER_ROLE("practitionerRole"),
  PRACTITIONER_ROLE_DATA("practitionerRoleData"),
  PRACTITIONER_DATA("practitionerData"),
  RX_DISPENSATION("rxDispensation"),
  ORGANIZATION_DATA("organizationData"),
  MED_DISPENSE("medicationDispense"),
  MEDICATION("medication");
  ;

  private final String code;
}
