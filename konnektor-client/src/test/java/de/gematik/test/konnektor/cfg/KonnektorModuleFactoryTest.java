package de.gematik.test.konnektor.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.dto.konnektor.LocalKonnektorConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.RemoteKonnektorConfiguration;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class KonnektorModuleFactoryTest {

  @Test
  void shouldWrapFromKonnektorConfigList() {
    val lkc = new LocalKonnektorConfiguration();
    lkc.setName("Soft-Konn");

    val rkc = new RemoteKonnektorConfiguration();
    rkc.setName("Remote-Konn");
    rkc.setProtocol("https");
    rkc.setAddress("localhost:443");
    rkc.setProfile("KONSIM");

    val factory = KonnektorModuleFactory.fromKonnektorConfigs(List.of(lkc, rkc));
    assertNotNull(factory);

    val softKonn = factory.createSoftKon();
    assertNotNull(softKonn);
    assertEquals("Soft-Konn", softKonn.getName());
  }
}
