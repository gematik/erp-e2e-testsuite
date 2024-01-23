/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.jwt;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.JWTDecoderException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class JWTDecoderTest {

  @RequiredArgsConstructor
  @Getter
  private enum TEST_TOKEN {
    WITHOUT_DISPLAY_NAME(
        "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g",
        "X110502414",
        "Robin Graf",
        "Vórmwinkel"),
    WITH_DISPLAY_NAME(
        "eyJhbGciOiJCUDI1NlIxIiwia2lkIjoicHVrX2lkcF9zaWciLCJ0eXAiOiJhdCtKV1QifQ.eyJhdXRoX3RpbWUiOjE2OTIyMjQxNTgsInNjb3BlIjoiZS1yZXplcHQtZGV2IG9wZW5pZCIsImNsaWVudF9pZCI6ImdlbWF0aWtUZXN0UHMiLCJnaXZlbl9uYW1lIjoiR8O8bnRoZXIgV29sZmdhbmcgR3JhZiIsImZhbWlseV9uYW1lIjoiQW5nZXJtw6RubiIsImRpc3BsYXlfbmFtZSI6IkfDvG50aGVyIFdvbGZnYW5nIEdyYWYgQW5nZXJtw6RubiIsIm9yZ2FuaXphdGlvbk5hbWUiOiJUZXN0IEdLVi1TVk5PVC1WQUxJRCIsInByb2Zlc3Npb25PSUQiOiIxLjIuMjc2LjAuNzYuNC40OSIsImlkTnVtbWVyIjoiWDExMDQ2NTc3MCIsImF6cCI6ImdlbWF0aWtUZXN0UHMiLCJhY3IiOiJnZW1hdGlrLWVoZWFsdGgtbG9hLWhpZ2giLCJhbXIiOlsibWZhIiwic2MiLCJwaW4iXSwiYXVkIjoiaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvIiwic3ViIjoiZjI0NjJkOGI4YjM3MmNjNTJhYmI0ZDhkNGE3MGNlMDI5MTZjZjk4ZDM2NjBhYzU4OGRlZDkwMDA5YzRiNjFhMyIsImlzcyI6Imh0dHBzOi8vaWRwLXJlZi56ZW50cmFsLmlkcC5zcGxpdGRucy50aS1kaWVuc3RlLmRlIiwiaWF0IjoxNjkyMjI0MTU4LCJleHAiOjE2OTIyMjQ0NTgsImp0aSI6IjhlM2U1YWI3LTUxMTUtNDNkMC1iNDE1LTc2ZTI1Y2I1YWIyNCJ9.ICk302J-mFimDwFyaINR1PVFKNldHps8lgAHez1ddw4HM-T1Pm9JVaglnXQLJ3Ew1j_a0RQ_9vJRsVfJLPZuAg",
        "X110465770",
        "Günther Wolfgang Graf",
        "Angermänn");

    private final String token;
    private final String kvid;
    private final String givenName;
    private final String familyName;
  }

  @ParameterizedTest()
  @EnumSource(TEST_TOKEN.class)
  void shouldDecode(TEST_TOKEN expectedToken) {
    val token = JWTDecoder.withCompactWriter().decode(expectedToken.getToken());
    val header = token.getHeader();
    val payload = token.getPayload();

    assertNotNull(token);
    assertNotNull(header);
    assertNotNull(payload);
    assertEquals(expectedToken.getKvid(), payload.getIdentifier());
    assertEquals(expectedToken.getGivenName(), payload.getGivenName());
    assertEquals(expectedToken.getFamilyName(), payload.getFamilyName());

    if (payload.getDisplayName() != null) {
      assertEquals(
          format("{0} {1}", payload.getGivenName(), payload.getFamilyName()),
          payload.getDisplayName());
    }
  }

  @Test
  void shouldDecodeToJson() {
    val decoder = JWTDecoder.withPrettyPrinter();
    assertDoesNotThrow(() -> decoder.decodeToJson(TEST_TOKEN.WITHOUT_DISPLAY_NAME.getToken()));
  }

  @Test
  void shouldThrowOnInvalidToken01() {
    val decoder = JWTDecoder.withCompactWriter();
    assertThrows(JWTDecoderException.class, () -> decoder.decode("123"));
  }

  @Test
  void shouldThrowOnInvalidToken02() {
    val decoder = JWTDecoder.withCompactWriter();
    val invalidToken = TEST_TOKEN.WITHOUT_DISPLAY_NAME.getToken().replace("hb", "xx");
    assertThrows(JWTDecoderException.class, () -> decoder.decode(invalidToken));
  }
}
