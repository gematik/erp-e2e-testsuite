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

package de.gematik.test.erezept.primsys.actors;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.primsys.data.actors.ActorType;

public class HealthInsurance extends BaseActor {

  public HealthInsurance(
      String name, SmcB smcb, ErpClient client, CryptoSystem algorithm, Konnektor konnektor) {
    super(ActorType.HEALTH_INSURANCE, name, smcb, client, algorithm, konnektor);
  }
}
