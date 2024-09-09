/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.konnektor.soap.mock.vsdm;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.konnektor.exceptions.InvalidKeyLengthException;
import de.gematik.test.konnektor.exceptions.ParsingUpdateResonException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VsdmChecksumTest {

  @Test
  void generateValidChecksum() {
    assertDoesNotThrow(() -> new VsdmChecksum("X123456789").sign(new byte[32]));

    val checksum =
        new VsdmChecksum("X123456789")
            .setIdentifier('S')
            .setUpdateReason(VsdmUpdateReason.UFS_UPDATE)
            .setVersion('1')
            .sign(new byte[32]);

    assertNotNull(checksum);
    val decodeChecksum = Base64.getDecoder().decode(checksum);
    assertEquals(47, decodeChecksum.length);

    val payload = new byte[decodeChecksum.length - 24];
    System.arraycopy(decodeChecksum, 0, payload, 0, payload.length);

    assertEquals("X123456789", parseFromByteArray(payload, 0, 10));
    assertEquals(
        Instant.now().getEpochSecond(), Long.parseLong(parseFromByteArray(payload, 10, 20)));
    assertEquals("U", parseFromByteArray(payload, 20, 21));
    assertEquals("S", parseFromByteArray(payload, 21, 22));
    assertEquals(1, Integer.parseInt(parseFromByteArray(payload, 22, 23)));
  }

  private String parseFromByteArray(byte[] input, int from, int to) {
    return new String(Arrays.copyOfRange(input, from, to), StandardCharsets.UTF_8);
  }

  @Test
  void invalidKey() {
    val checksum = new VsdmChecksum("X123456789");
    List.of(0, 31, 33)
        .forEach(
            keyLen ->
                assertThrows(
                    InvalidKeyLengthException.class, () -> checksum.sign(new byte[keyLen])));
  }

  @SneakyThrows
  @Test
  void shouldParseValidChecksum() {
    Assertions.assertDoesNotThrow(
        () ->
            VsdmChecksum.parse("WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI="));
    val checksum =
        VsdmChecksum.parse("WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI=");
    Assertions.assertEquals("Y785728071", checksum.getKvnr());
    Assertions.assertEquals('1', checksum.getVersion());
    Assertions.assertEquals('C', checksum.getIdentifier());
    Assertions.assertEquals(VsdmUpdateReason.UFS_UPDATE, checksum.getUpdateReason());
  }

  @Test
  void shouldNotParseInvalidChecksum() {
    Assertions.assertThrows(ParsingUpdateResonException.class, () -> VsdmChecksum.parse("abc"));
    Assertions.assertThrows(
        ParsingUpdateResonException.class, () -> VsdmChecksum.parse("UzYyODUwMzkzMDE2ODM3MjQyODR"));
    Assertions.assertThrows(
        ParsingUpdateResonException.class,
        () -> VsdmChecksum.parse("UzYyODUwMzkzMDE2ODM3MjQyODRV"));
    Assertions.assertThrows(
        ParsingUpdateResonException.class,
        () -> VsdmChecksum.parse("UzYyODUwMzkzMDE2ODM3MjQyODRP"));
  }
}
