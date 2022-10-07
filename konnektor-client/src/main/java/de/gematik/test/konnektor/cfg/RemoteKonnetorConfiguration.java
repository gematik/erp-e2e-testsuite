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

package de.gematik.test.konnektor.cfg;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorImpl;
import de.gematik.test.konnektor.profile.ProfileType;
import de.gematik.test.konnektor.soap.RemoteKonnektorServiceProvider;
import de.gematik.test.konnektor.soap.TrustProvider;
import java.net.URL;
import java.util.Objects;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@Data
public class RemoteKonnetorConfiguration extends KonnektorConfiguration {

  private String address;
  private String protocol;
  private String profile;
  private ContextConfiguration context;
  private TLSConfiguration tls;
  private BasicAuthConfiguration basicAuth;

  @JsonCreator
  public RemoteKonnetorConfiguration() {
    this.setType(KonnektorType.REMOTE);
  }

  private String getUrlString() {
    return format("{0}://{1}", protocol, address);
  }

  @SneakyThrows
  private URL getUrl() {
    Objects.requireNonNull(
        protocol, "RemoteKonnektor requires a protocol"); // is not implicitly checked by getUrl()
    Objects.requireNonNull(address, "RemoteKonnektor requires a network address");
    return new URL(getUrlString());
  }

  public ProfileType getProfileType() {
    return ProfileType.fromString(profile);
  }

  public Konnektor create() {
    val ctx = context.getContextType();
    val konnektorProfile = getProfileType().createProfile();

    val sPB = RemoteKonnektorServiceProvider.of(getUrl(), konnektorProfile);

    if (protocol.equalsIgnoreCase("https") && tls != null) {
      val trustProvider = TrustProvider.from(tls);
      sPB.trustProvider(trustProvider);
    } else if (!protocol.equalsIgnoreCase("http")) {
      throw new IllegalArgumentException(
          format(
              "Konnektor configuration {0} with protocol {1} and TLS {2} is invalid",
              this.getName(), this.getProtocol(), this.getTls()));
    }

    if (getBasicAuth() != null) {
      sPB.username(getBasicAuth().getUsername());
      sPB.password(getBasicAuth().getPassword());
    }

    val serviceProvider = sPB.build();
    return new KonnektorImpl(ctx, this.getName(), KonnektorType.REMOTE, serviceProvider);
  }

  @Override
  public String toString() {
    return format("Remote Konnektor \"{0}\" at {1}", this.getName(), getUrlString());
  }
}
