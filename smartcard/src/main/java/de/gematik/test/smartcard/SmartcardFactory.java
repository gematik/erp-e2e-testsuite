/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.smartcard;

import static java.text.MessageFormat.*;

import com.fasterxml.jackson.databind.*;
import de.gematik.test.smartcard.cfg.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public class SmartcardFactory {

  private static SmartcardArchive smartcardArchive;

  private SmartcardFactory() {
    throw new UnsupportedOperationException(
        "Do not instantiate utility class " + SmartcardFactory.class);
  }

  /**
   * Erstelle default Smartcard Archiv aus dem mitgelieferten Kartenmaterial
   *
   * @return {@link SmartcardArchive} Objekt welches den Zugriff auf die default Karten ermöglicht
   */
  @SneakyThrows
  public static SmartcardArchive getArchive() {
    if (smartcardArchive == null) {
      val is =
          Objects.requireNonNull(
              SmartcardFactory.class.getClassLoader().getResourceAsStream("cardimages/images.json"),
              format("Cannot read card images from resources"));
      smartcardArchive = getArchive(is);
    }
    return smartcardArchive;
  }

  @SneakyThrows
  private static SmartcardArchive getArchive(final InputStream is) {
    val list = Arrays.stream(new ObjectMapper().readValue(is, SmartcardConfigDto[].class)).toList();

    val egkCards =
        list.stream()
            .filter(it -> it.getCardType() == SmartcardType.EGK)
            .map(it -> new Egk(toSmartcardKey.apply(it), it.getIccsn(), it.getKvnr()))
            .toList();
    val hbaCards =
        list.stream()
            .filter(it -> it.getCardType() == SmartcardType.HBA)
            .map(it -> new Hba(toSmartcardKey.apply(it), it.getIccsn()))
            .toList();
    val smcbCards =
        list.stream()
            .filter(it -> it.getCardType() == SmartcardType.SMC_B)
            .map(it -> new SmcB(toSmartcardKey.apply(it), it.getIccsn()))
            .toList();

    return new SmartcardArchive(smcbCards, hbaCards, egkCards);
  }

  static Function<SmartcardConfigDto, List<Supplier<SmartcardCertificate>>> toSmartcardKey =
      it ->
          it.getStores().stream()
              .map(SmartcardFactory::normalizePath)
              .map(
                  path ->
                      (Supplier<SmartcardCertificate>)
                          () -> {
                            log.trace(format("Load Smardcard certificate from Store={0}", path));
                            return new SmartcardCertificate(path);
                          })
              .toList();

  private static String normalizePath(final String input) {
    val baseDir = "cardimages";
    return Path.of(baseDir, input).normalize().toString().replace("\\", "/");
  }
}
