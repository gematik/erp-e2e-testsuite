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

package de.gematik.test.utils;

import static de.gematik.test.erezept.client.cfg.ErpClientFactoryUtil.callInMockedStaticContext;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;

public class ActorStageUtil {

  private static final PrimsysConfigurationDto CONFIG =
      ConfigurationReader.forPrimSysConfiguration().create();
  private static final SmartcardArchive SMARTCARD_ARCHIVE = SmartcardArchive.fromResources();

  public static ActorStage createMockedActorStage(FhirCodec codec) {
    return callInMockedStaticContext(CONFIG, SMARTCARD_ARCHIVE, codec, ActorStage::new);
  }

  public static ErpFdTestsuiteFactory createMockedErpFdTestsuiteFactory(FhirCodec codec) {
    return callInMockedStaticContext(
        CONFIG, SMARTCARD_ARCHIVE, codec, ErpFdTestsuiteFactory::create);
  }
}
