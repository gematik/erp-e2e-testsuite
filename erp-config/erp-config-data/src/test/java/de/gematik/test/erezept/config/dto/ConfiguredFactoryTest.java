package de.gematik.test.erezept.config.dto;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;

class ConfiguredFactoryTest {

  @Test
  void shouldGetConfigByName() {
    val e1 = new TestConfigElement("first");
    val e2 = new TestConfigElement("second");
    val factory = new TestConfigFactory();

    val chosen = assertDoesNotThrow(() -> factory.getConfig("First", List.of(e1, e2)));
    assertEquals(e1, chosen);
  }

  @Test
  void shouldThrowOnUnknown() {
    val e1 = new TestConfigElement("first");
    val e2 = new TestConfigElement("second");
    val configs = List.of(e1, e2);
    val factory = new TestConfigFactory();

    val cme =
        assertThrows(
            ConfigurationMappingException.class, () -> factory.getConfig("third", configs));
    // ensure a proper error message is given containing all possible choices + the invalid one
    assertTrue(cme.getMessage().contains("first"));
    assertTrue(cme.getMessage().contains("second"));
    assertTrue(cme.getMessage().contains("third"));
  }

  private static class TestConfigFactory extends ConfiguredFactory {}

  @Getter
  @RequiredArgsConstructor
  private static class TestConfigElement implements INamedConfigurationElement {
    private final String name;
  }
}
