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

package de.gematik.test.erezept.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;

public class JWTDecoder {

  private JWTDecoder() {
    throw new AssertionError("Do not instantiate this utility-class");
  }

  @SneakyThrows
  public static Token decode(String token) {
    String[] chunks = token.split("\\.");
    Base64.Decoder decoder = Base64.getUrlDecoder();

    String header = new String(decoder.decode(chunks[0]));
    String payload = new String(decoder.decode(chunks[1]));

    ObjectMapper objectMapper = new ObjectMapper();
    val jwtHeader = objectMapper.readValue(header, JWTHeader.class);
    val jwtPayload = objectMapper.readValue(payload, JWTPayload.class);

    return new Token(jwtHeader, jwtPayload);
  }
}
