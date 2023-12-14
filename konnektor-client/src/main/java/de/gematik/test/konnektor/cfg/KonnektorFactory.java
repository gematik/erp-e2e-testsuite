package de.gematik.test.konnektor.cfg;

import static java.text.MessageFormat.format;

import de.gematik.test.cardterminal.CardTerminalClientFactory;
import de.gematik.test.erezept.config.dto.konnektor.*;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorImpl;
import de.gematik.test.konnektor.profile.ProfileType;
import de.gematik.test.konnektor.soap.MockKonnektorServiceProvider;
import de.gematik.test.konnektor.soap.RemoteKonnektorServiceProvider;
import de.gematik.test.konnektor.soap.TrustProvider;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import de.gematik.test.smartcard.SmartcardFactory;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class KonnektorFactory {

  private KonnektorFactory() throws IllegalAccessException {
    throw new IllegalAccessException("utility class");
  }

  public static ContextType fromConfig(KonnektorContextConfiguration config) {
    val ctx = new ContextType();
    ctx.setMandantId(config.getMandantId());
    ctx.setClientSystemId(config.getClientSystemId());
    ctx.setWorkplaceId(config.getWorkplaceId());
    ctx.setUserId(config.getUserId());
    return ctx;
  }

  private static ContextType getDefaultContextType() {
    val ctx = new ContextType();
    ctx.setMandantId("Mandant1");
    ctx.setClientSystemId("CS1");
    ctx.setWorkplaceId("WP1");
    return ctx;
  }

  public static Konnektor createKonnektor(KonnektorConfiguration config) {
    if (config instanceof LocalKonnektorConfiguration lkc) {
      return createMockKonnektor(lkc);
    } else if (config instanceof RemoteKonnektorConfiguration rkc) {
      return createRemoteKonnektor(rkc);
    } else {
      throw new ConfigurationException(
          format(
              "Unable to create Konnektor with configuration of type {0}",
              config.getClass().getSimpleName()));
    }
  }

  public static Konnektor createSoftKon() {
    return createMockKonnektor("Soft-Konn");
  }

  public static Konnektor createMockKonnektor(LocalKonnektorConfiguration config) {
    if (config.getVsdmServiceConfiguration() != null) {
      return createMockKonnektor(config.getName(), config.getVsdmServiceConfiguration());
    } else {
      return createMockKonnektor(config.getName());
    }
  }

  public static Konnektor createMockKonnektor(String named) {
    return createMockKonnektor(named, VsdmServiceConfiguration.createDefault());
  }

  public static Konnektor createMockKonnektor(
      String named, VsdmServiceConfiguration vsdmServiceConfiguration) {
    val smartcards = SmartcardFactory.getArchive();
    log.info(format("Create Local Mock Konnektor {0}", named));

    val ctx = getDefaultContextType();
    val serviceProvider =
        new MockKonnektorServiceProvider(
            smartcards, VsdmService.createFrom(vsdmServiceConfiguration));
    return new KonnektorImpl(ctx, named, KonnektorType.LOCAL, serviceProvider);
  }

  public static Konnektor createRemoteKonnektor(RemoteKonnektorConfiguration config) {
    val name = config.getName();
    val tls = config.getTls();
    val basicAuth = config.getBasicAuth();
    val protocol = config.getProtocol();
    val profile = config.getProfile();
    val address = config.getAddress();
    log.info(format("Create Remote Konnektor \"{0}\" at {1}://{2}", name, protocol, address));

    val ctx = KonnektorFactory.fromConfig(config.getContext());
    val konnektorProfile = ProfileType.fromString(profile).createProfile();

    val sPB =
        RemoteKonnektorServiceProvider.of(getUrl(protocol, config.getAddress()), konnektorProfile);

    if (protocol.equalsIgnoreCase("https") && tls != null) {
      val trustProvider = TrustProvider.from(tls);
      sPB.trustProvider(trustProvider);
    } else if (!protocol.equalsIgnoreCase("http")) {
      throw new IllegalArgumentException(
          format(
              "Konnektor configuration {0} with protocol {1} and TLS {2} is invalid",
              name, protocol, tls));
    }

    if (basicAuth != null) {
      sPB.username(basicAuth.getUsername());
      sPB.password(basicAuth.getPassword());
    }

    val serviceProvider = sPB.build();
    val cardTerminals =
        config.getCardTerminalClientConfigurations().stream()
            .map(CardTerminalClientFactory::createClient)
            .collect(Collectors.toSet());
    return new KonnektorImpl(ctx, name, KonnektorType.REMOTE, serviceProvider, cardTerminals);
  }

  @SneakyThrows
  private static URL getUrl(String protocol, String address) {
    Objects.requireNonNull(
        protocol, "RemoteKonnektor requires a protocol"); // is not implicitly checked by getUrl()
    Objects.requireNonNull(address, "RemoteKonnektor requires a network address");
    return new URL(format("{0}://{1}", protocol, address));
  }
}
