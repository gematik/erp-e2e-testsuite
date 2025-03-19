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

package de.gematik.test.erezept.actions.bundlepaging;

import de.gematik.test.erezept.*;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.*;
import org.hl7.fhir.r4.model.Bundle;

@Slf4j
public class DownloadBundle<T extends Bundle> extends ErpAction<T> {

  private final BundlePagingCommand<T> cmd;

  private DownloadBundle(BundlePagingCommand<T> command) {
    this.cmd = command;
  }

  public static <T extends Bundle> DownloadBundle<T> withBundlePagingCommand(
      BundlePagingCommand<T> bundlePagingCommand) {
    return new DownloadBundle<>(bundlePagingCommand);
  }

  public static <T extends Bundle> DownloadBundle<T> nextFor(T bundle) {
    return withBundlePagingCommand(BundlePagingCommand.getNextFrom(bundle));
  }

  public static <T extends Bundle> DownloadBundle<T> selfFor(T bundle) {
    return withBundlePagingCommand(BundlePagingCommand.getSelfFrom(bundle));
  }

  public static <T extends Bundle> DownloadBundle<T> previousFor(T bundle) {
    return withBundlePagingCommand(BundlePagingCommand.getPreviousFrom(bundle));
  }

  @Override
  @Step("{0} ruft ein Bundle ab")
  public ErpInteraction<T> answeredBy(Actor actor) {
    return this.performCommandAs(cmd, actor);
  }
}
