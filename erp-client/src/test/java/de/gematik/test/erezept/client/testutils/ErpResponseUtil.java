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

package de.gematik.test.erezept.client.testutils;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.util.Map;
import javax.annotation.Nullable;
import org.hl7.fhir.r4.model.Resource;

public class ErpResponseUtil {

  private static final String ENCODED_JWT =
      "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

  public static <E extends Resource> FhirBResponse<E> createErpResponse(
      @Nullable Resource resource, Class<E> expectType) {
    return createErpResponse(
        resource, expectType, 200, ENCODED_JWT, Map.of(), createEmptyValidationResult());
  }

  public static <E extends Resource> FhirBResponse<E> createErpResponse(
      @Nullable Resource resource, Class<E> expectType, int statusCode) {
    return createErpResponse(
        resource, expectType, statusCode, ENCODED_JWT, Map.of(), createEmptyValidationResult());
  }

  public static <E extends Resource> FhirBResponse<E> createErpResponse(
      @Nullable Resource resource,
      Class<E> expectType,
      int statusCode,
      String jwtToken,
      Map<String, String> headderMap,
      ValidationResult validationResult) {
    return FhirBResponse.forPayload(expectType, resource)
        .withStatusCode(statusCode)
        .usedJwt(jwtToken)
        .withHeaders(HttpHeader.from(headderMap))
        .andValidationResult(validationResult);
  }
}
