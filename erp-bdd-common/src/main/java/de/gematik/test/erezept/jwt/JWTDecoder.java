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

package de.gematik.test.erezept.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.gematik.test.erezept.exceptions.JWTDecoderException;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;

public class JWTDecoder {

  private final ObjectMapper objectMapper;
  private final ObjectWriter writer;

  private JWTDecoder(boolean pretty) {
    this.objectMapper = new ObjectMapper();
    if (pretty) {
      this.writer = this.objectMapper.writerWithDefaultPrettyPrinter();
    } else {
      this.writer = this.objectMapper.writer();
    }
  }

  public static JWTDecoder withPrettyPrinter() {
    return new JWTDecoder(true);
  }

  public static JWTDecoder withCompactWriter() {
    return new JWTDecoder(false);
  }

  public Token decode(String token) {
    val chunks = token.split("\\.");
    val decoder = Base64.getUrlDecoder();

    if (chunks.length != 3) {
      throw new JWTDecoderException(token);
    }

    String header = new String(decoder.decode(chunks[0]));
    String payload = new String(decoder.decode(chunks[1]));

    try {
      val jwtHeader = objectMapper.readValue(header, JWTHeader.class);
      val jwtPayload = objectMapper.readValue(payload, JWTPayload.class);
      return new Token(jwtHeader, jwtPayload);
    } catch (JsonProcessingException jpe) {
      throw new JWTDecoderException(token, jpe);
    }
  }

  @SneakyThrows
  public String decodeToJson(String token) {
    val tokenObj = decode(token);
    return writer.writeValueAsString(tokenObj);
  }
}
