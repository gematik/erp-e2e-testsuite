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

package de.gematik.test.konnektor.commands.options;

import static org.junit.Assert.*;

import lombok.val;
import org.junit.Test;

public class VerifyDocumentOptionsTest {

  @Test
  public void defaultVerifyDocumentOptions() {
    val opts = VerifyDocumentOptions.getDefaultOptions();

    assertEquals("NONE", opts.getTvMode());
    assertEquals(SignatureType.RFC_5652, opts.getSignatureType());
    assertTrue(opts.isIncludeVerifier());
    assertTrue(opts.isIncludeCertificateValue());
    assertTrue(opts.isIncludeRevocationValue());
    assertFalse(opts.isIncludeRevocationInfo());
    assertFalse(opts.isExpandBinaryValues());
  }
}