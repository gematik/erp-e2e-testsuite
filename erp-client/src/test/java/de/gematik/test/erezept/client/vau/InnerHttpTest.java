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

package de.gematik.test.erezept.client.vau;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.val;
import org.jose4j.base64url.Base64;
import org.junit.jupiter.api.Test;

class InnerHttpTest {

  @Test
  void shouldParsable() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSAyMDEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pgo8VGFzayB4bWxucz0iaHR0cDovL2hsNy5vcmcvZmhpciI+PGlkIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRXJ4VGFza3wxLjEuMSIvPjwvbWV0YT48ZXh0ZW5zaW9uIHVybD0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QcmVzY3JpcHRpb25UeXBlIj48dmFsdWVDb2Rpbmc+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvQ29kZVN5c3RlbS9GbG93dHlwZSIvPjxjb2RlIHZhbHVlPSIxNjAiLz48ZGlzcGxheSB2YWx1ZT0iTXVzdGVyIDE2IChBcG90aGVrZW5wZmxpY2h0aWdlIEFyem5laW1pdHRlbCkiLz48L3ZhbHVlQ29kaW5nPjwvZXh0ZW5zaW9uPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL1ByZXNjcmlwdGlvbklEIi8+PHZhbHVlIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PC9pZGVudGlmaWVyPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL0FjY2Vzc0NvZGUiLz48dmFsdWUgdmFsdWU9ImMxNTU3MzNiN2QxMzJlYWQyN2FmZmFmN2JiOTUwYWIxNzBhNWIxZWIwMzE3OWU2ZGFiYWQyYjk0ZThhM2M5M2UiLz48L2lkZW50aWZpZXI+PHN0YXR1cyB2YWx1ZT0iZHJhZnQiLz48aW50ZW50IHZhbHVlPSJvcmRlciIvPjxhdXRob3JlZE9uIHZhbHVlPSIyMDIyLTA1LTE4VDE4OjU2OjQ1LjQ2MiswMDowMCIvPjxsYXN0TW9kaWZpZWQgdmFsdWU9IjIwMjItMDUtMThUMTg6NTY6NDUuNDYyKzAwOjAwIi8+PHBlcmZvcm1lclR5cGU+PGNvZGluZz48c3lzdGVtIHZhbHVlPSJ1cm46aWV0ZjpyZmM6Mzk4NiIvPjxjb2RlIHZhbHVlPSJ1cm46b2lkOjEuMi4yNzYuMC43Ni40LjU0Ii8+PGRpc3BsYXkgdmFsdWU9IsOWZmZlbnRsaWNoZSBBcG90aGVrZSIvPjwvY29kaW5nPjx0ZXh0IHZhbHVlPSLDlmZmZW50bGljaGUgQXBvdGhla2UiLz48L3BlcmZvcm1lclR5cGU+PC9UYXNrPgo=";

    val response = InnerHttp.decode(Base64.decode(exampleInnerHttp));

    assertEquals(201, response.getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocol());
    assertEquals("application/fhir+xml;charset=utf-8", response.getHeader().get("Content-Type"));

    assertFalse(response.getBody().isEmpty(), "Body is not empty");
    assertTrue(
        response.getBody().contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"),
        "Body contains XML");
  }

  @Test
  void withoutBody() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSAyMDEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo";

    val response = InnerHttp.decode(Base64.decode(exampleInnerHttp));

    assertEquals(201, response.getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocol());
    assertEquals("application/fhir+xml;charset=utf-8", response.getHeader().get("Content-Type"));

    assertTrue(response.getBody().isEmpty(), "Body is empty");
  }

  @Test
  void missingStatusCode() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pgo8VGFzayB4bWxucz0iaHR0cDovL2hsNy5vcmcvZmhpciI+PGlkIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRXJ4VGFza3wxLjEuMSIvPjwvbWV0YT48ZXh0ZW5zaW9uIHVybD0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QcmVzY3JpcHRpb25UeXBlIj48dmFsdWVDb2Rpbmc+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvQ29kZVN5c3RlbS9GbG93dHlwZSIvPjxjb2RlIHZhbHVlPSIxNjAiLz48ZGlzcGxheSB2YWx1ZT0iTXVzdGVyIDE2IChBcG90aGVrZW5wZmxpY2h0aWdlIEFyem5laW1pdHRlbCkiLz48L3ZhbHVlQ29kaW5nPjwvZXh0ZW5zaW9uPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL1ByZXNjcmlwdGlvbklEIi8+PHZhbHVlIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PC9pZGVudGlmaWVyPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL0FjY2Vzc0NvZGUiLz48dmFsdWUgdmFsdWU9ImMxNTU3MzNiN2QxMzJlYWQyN2FmZmFmN2JiOTUwYWIxNzBhNWIxZWIwMzE3OWU2ZGFiYWQyYjk0ZThhM2M5M2UiLz48L2lkZW50aWZpZXI+PHN0YXR1cyB2YWx1ZT0iZHJhZnQiLz48aW50ZW50IHZhbHVlPSJvcmRlciIvPjxhdXRob3JlZE9uIHZhbHVlPSIyMDIyLTA1LTE4VDE4OjU2OjQ1LjQ2MiswMDowMCIvPjxsYXN0TW9kaWZpZWQgdmFsdWU9IjIwMjItMDUtMThUMTg6NTY6NDUuNDYyKzAwOjAwIi8+PHBlcmZvcm1lclR5cGU+PGNvZGluZz48c3lzdGVtIHZhbHVlPSJ1cm46aWV0ZjpyZmM6Mzk4NiIvPjxjb2RlIHZhbHVlPSJ1cm46b2lkOjEuMi4yNzYuMC43Ni40LjU0Ii8+PGRpc3BsYXkgdmFsdWU9IsOWZmZlbnRsaWNoZSBBcG90aGVrZSIvPjwvY29kaW5nPjx0ZXh0IHZhbHVlPSLDlmZmZW50bGljaGUgQXBvdGhla2UiLz48L3BlcmZvcm1lclR5cGU+PC9UYXNrPgo=";

    val b64InnerHttp = Base64.decode(exampleInnerHttp);
    assertThrows(VauException.class, () -> InnerHttp.decode(b64InnerHttp));
  }

  @Test
  void emptyResponseBody() {
    assertThrows(VauException.class, () -> InnerHttp.decode(new byte[0]));
  }

  @Test
  void shouldGenerateValidInnerHttp() {
    val expectedInnerHttp =
        "UE9TVCBUYXNrLyRjcmVhdGUgSFRUUC8xLjENClggS2V5OiBYIFZhbHVlDQpjb250ZW50LWxlbmd0aDogNw0KDQpjb250ZW50";
    val encode =
        InnerHttp.encode(
            HttpRequestMethod.POST, "Task/$create", Map.of("X Key", "X Value"), "content");
    val base64 = Base64.encode(encode.getBytes(StandardCharsets.UTF_8));
    assertEquals(expectedInnerHttp, base64);
  }
}
