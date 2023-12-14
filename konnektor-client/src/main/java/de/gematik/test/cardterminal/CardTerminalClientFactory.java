package de.gematik.test.cardterminal;

import de.gematik.test.cardterminal.cats.CatsClient;
import de.gematik.test.erezept.config.dto.konnektor.CardTerminalClientConfiguration;

public class CardTerminalClientFactory {

  private CardTerminalClientFactory() throws IllegalAccessException {
    throw new IllegalAccessException("utility class");
  }

  public static CardTerminalClient createCatsClient(String ctId, String url) {
    return new CatsClient(ctId, url);
  }

  public static CardTerminalClient createClient(CardTerminalClientConfiguration config) {
    return createCatsClient(config.ctId(), config.url());
  }
}
