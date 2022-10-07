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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine;

/**
 * This subcommand will generate the full urls for the pharmacy service provider to be put into
 * ApoVZD. With these URLs the App can send prescriptions for different delivery options to a
 * specific pharmacy via the pharmacy service provider. Instead of using the plain Telematik-ID to
 * identify the concrete pharmacy, we are using a simple SHA-1 of the Telematik-ID.
 */
@SuppressWarnings({"java:S106"}) // System.out.println is not logging here!
public class PharmacyIdentifiers implements Callable<Integer> {

  @CommandLine.Option(
      names = "--url",
      type = URL.class,
      description = "Base-URL of the Pharmacy Service Provider")
  private URL url = getDefaultUrl();

  private final PrintStream out;

  public PharmacyIdentifiers() {
    this(System.out);
  }

  public PharmacyIdentifiers(PrintStream out) {
    // potentially possible to write to a file later on!
    this.out = out;
  }

  @SneakyThrows
  private static URL getDefaultUrl() {
    return new URL("https://erp-pharmacy-serviceprovider.dev.gematik.solutions");
  }

  @Override
  public Integer call() throws Exception {
    val cfg = TestsuiteConfiguration.getInstance();
    val sca = SmartcardFactory.readArchive();

    val md = MessageDigest.getInstance("sha-1"); // NOSONAR not security related!

    cfg.getActors()
        .getPharmacies()
        .forEach(
            pharm -> {
              val name = pharm.getName();
              val smcb = sca.getSmcbByICCSN(pharm.getSmcbIccsn(), Crypto.RSA_2048);
              val telematikId = smcb.getTelematikId();
              val digest = md.digest(telematikId.getBytes(StandardCharsets.UTF_8));
              val id = new BigInteger(1, digest).toString(16);
              this.out.println(
                  format("Pharmacy {0} ({1}) -> PSP Identifier: {2}", name, telematikId, id));
              printFullUrl(id, DeliveryOption.DELIVERY);
              printFullUrl(id, DeliveryOption.PICKUP);
              printFullUrl(id, DeliveryOption.ON_PREMISE);
            });

    sca.destroy();
    return 0;
  }

  private void printFullUrl(String pharmacyId, DeliveryOption option) {
    this.out.println(format("\t{0}: {1}", option, createFullUrl(pharmacyId, option)));
  }

  private String createFullUrl(String pharmacyId, DeliveryOption option) {
    var base = this.url.toString();
    if (!base.endsWith("/")) {
      base = base + "/"; // just append at the end to ensure correct concatenation afterwards!
    }
    return format("{0}{1}/{2}", base, option.getResource(), pharmacyId);
  }

  // Note: later on take the one from PSP Client
  private enum DeliveryOption {
    DELIVERY("delivery_only"),
    PICKUP("pick_up"),
    ON_PREMISE("local_delivery");

    @Getter private final String resource;

    DeliveryOption(String resource) {
      this.resource = resource;
    }
  }
}
