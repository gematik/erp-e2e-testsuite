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

package de.gematik.test.erezept;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErpConfiguration {

  @Delegate private final TestsuiteConfiguration config;
  @Getter private final SmartcardArchive smartcards;

  public static ErpConfiguration create() {
    val actors = TestsuiteConfiguration.getInstance();
    val smartcards = SmartcardFactory.getArchive();
    return new ErpConfiguration(actors, smartcards);
  }
}