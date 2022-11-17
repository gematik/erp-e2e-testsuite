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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.config.PSPClientConfig;
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.HasTeardown;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class UsePspClient implements Ability, HasTeardown {
  @Delegate private final PSPClient pspClient;
  private final String url;
  @Nullable private final String xAuth;

  @SneakyThrows
  private UsePspClient(
      PSPClient pspClient, int connectionTimeout, String url, @Nullable String xAuth) {
    this.pspClient = pspClient;
    this.url = stripUrl(url);
    this.xAuth = xAuth;
    this.pspClient.connectBlocking(connectionTimeout, TimeUnit.SECONDS);
  }

  public Optional<String> getXAuth() {
    return Optional.ofNullable(xAuth);
  }

  public String getBaseUrl() {
    return this.url;
  }

  public String getFullUrl(DeliveryOption option) {
    return format("{0}/{1}/{2}", this.url, option.getPath(), pspClient.getId());
  }

  @Override
  public void tearDown() {
    log.info(format("try to close PspClient Ability with ID {0}", this.pspClient.getId()));
    this.pspClient.close();
  }

  private static String stripUrl(String url) {
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1);
    } else {
      return url;
    }
  }

  public static Builder with(PSPClient client) {
    return new Builder(client);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final PSPClient client;

    public UsePspClient andConfig(PSPClientConfig config) {
      return new UsePspClient(
          client, config.getConnectTimeOut(), config.getUrl(), config.getAuth());
    }
  }
}
