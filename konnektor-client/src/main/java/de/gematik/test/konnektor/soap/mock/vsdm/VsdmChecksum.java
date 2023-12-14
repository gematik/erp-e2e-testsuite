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

package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.test.konnektor.exceptions.InvalidKeyLengthException;
import de.gematik.test.konnektor.exceptions.ParsingUpdateResonException;
import lombok.*;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class VsdmChecksum {

  /** Field 1 len: 10 */
  @NonNull private String kvnr;

  /** Field 2 Unix timestamp len: 10 */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Field 3 len: 1 */
  @Builder.Default private VsdmUpdateReason updateReason = VsdmUpdateReason.UFS_UPDATE;

  /** Field 4 len: 1 */
  @Builder.Default private char identifier = 'S';

  /** Field 5 len: 1 */
  @Builder.Default private char version = '1';

  @Builder.Default private byte[] key = new byte[32];

  public static VsdmChecksumBuilder builder(String kvnr) {
    return new VsdmChecksumBuilder().kvnr(kvnr);
  }

  public VsdmChecksum setKey(byte @NonNull [] key) {
    this.key = key;
    return this;
  }

  public static VsdmChecksum parse(String checksumAsBase64) throws ParsingUpdateResonException {
    val checksum = Base64.getDecoder().decode(checksumAsBase64.getBytes(StandardCharsets.UTF_8));
    val kvnr = new String(copyByteArrayFrom(checksum, 0, 10), StandardCharsets.UTF_8);
    val timestamp = Instant.ofEpochSecond(Long.parseLong(new String(copyByteArrayFrom(checksum, 10, 20), StandardCharsets.UTF_8)));
    val reason = VsdmUpdateReason.fromChecksum(copyCharFrom(checksum, 20));
    val identifier = copyCharFrom(checksum, 21);
    val version = copyCharFrom(checksum, 22);
    return VsdmChecksum.builder(kvnr)
            .timestamp(timestamp)
            .updateReason(reason)
            .identifier(identifier)
            .version(version)
            .build();
  }

  private static byte[] copyByteArrayFrom(byte[] data, int from, int to) throws ParsingUpdateResonException {
    try {
      return Arrays.copyOfRange(data, from, to);
    } catch(ArrayIndexOutOfBoundsException e) {
      throw new ParsingUpdateResonException(data, from, to);
    }
  }

  private static char copyCharFrom(byte[] data, int pos) throws ParsingUpdateResonException {
    try {
     return (char) data[pos];
    } catch(ArrayIndexOutOfBoundsException e) {
      throw new ParsingUpdateResonException(data, pos);
    }
  }
  /**
   * The method generate a checksum encode as base64. The checksum contains the first 24 bytes of
   * the signature, which contains a HMac hash (SHA256) over the fields 1 to 5
   *
   * @return a base64 encoded checksum
   */
  public String generate() {
    if (key.length != 32) {
      throw new InvalidKeyLengthException(key, 32);
    }

    val hMac = new HMac(new SHA256Digest());
    hMac.init(new KeyParameter(key));
    val data = genPayload().getBytes(StandardCharsets.UTF_8);
    hMac.update(data, 0, data.length);
    val signature = new byte[hMac.getMacSize()];
    hMac.doFinal(signature, 0);

    val checksum = new byte[data.length + 24];
    System.arraycopy(data, 0, checksum, 0, data.length);
    System.arraycopy(signature, 0, checksum, data.length, 24);
    return Base64.getEncoder().encodeToString(checksum);
  }

  private String genPayload() {
    val ret = new StringBuilder();
    ret.append(kvnr)
        .append(timestamp.getEpochSecond())
        .append(updateReason.getIdentifier())
        .append(identifier)
        .append(version);
    return ret.toString();
  }

  @Override
  public String toString() {
    return "VsdmChecksum{"
        + "kvnr='"
        + kvnr
        + '\''
        + ", timestamp="
        + timestamp
        + ", updateReason="
        + updateReason
        + ", identifier="
        + identifier
        + ", version="
        + version
        + '}';
  }
}
