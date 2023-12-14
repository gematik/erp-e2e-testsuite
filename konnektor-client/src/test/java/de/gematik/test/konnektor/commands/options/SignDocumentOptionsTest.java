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

package de.gematik.test.konnektor.commands.options;

import lombok.val;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SignDocumentOptionsTest {

  @Test
  public void defaultSignDocumentOptions() {
    val opts = SignDocumentOptions.getDefaultOptions();

    assertEquals("NONE", opts.getTvMode());
    assertEquals("text/plain; charset=utf-8", opts.getMimeType());
    assertEquals(SignatureType.RFC_5652, opts.getSignatureType());
    assertTrue(opts.isIncludeEContent());
    assertEquals(SigningCryptType.RSA_ECC, opts.getCryptoType());
  }

  @Test
  public void SignDocumentOptionsWithSigningCrypto() {
    val cryptos = List.of(SigningCryptType.RSA_ECC, SigningCryptType.RSA, SigningCryptType.ECC);
    cryptos.forEach(
        ct -> {
          val opts = SignDocumentOptions.getDefaultOptions(ct);
          assertEquals(ct, opts.getCryptoType());
        });
  }
}
