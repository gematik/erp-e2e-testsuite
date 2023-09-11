package de.gematik.test.erezept.toggle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class FeatureConfigurationTest {

  @Test
  void shouldNotHaveUndefinedToggles() {
    val fc = new FeatureConfiguration();
    assertFalse(fc.hasToggle("hello.world"));
    assertEquals("Default Value", fc.getToggle("hello.world", "Default Value"));
  }

  @Test
  @SetSystemProperty(key = "hello.world", value = "Toggle Config")
  void shouldHaveSimpleToggle() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.hasToggle("hello.world"));
    assertEquals("Toggle Config", fc.getToggle("hello.world", "Default Value"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "Yes")
  void shouldHaveCustomBooleanToggleValue() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.getBooleanToggle("hello.boolean"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "TRUE")
  void shouldHaveBooleanToggle() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.getBooleanToggle("hello.boolean"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "False")
  void shouldHaveDefaultBooleanToggle() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.getBooleanToggle("hello.boolean2", true));
  }

  @Test
  void shouldHaveDefaultBooleanToggleValue() {
    val fc = new FeatureConfiguration();
    assertFalse(fc.hasToggle("hello.boolean"));
    assertFalse(fc.getBooleanToggle("hello.boolean2"));
  }

  @Test
  @SetSystemProperty(key = "hello.integer", value = "100")
  void shouldHaveIntegerToggle() {
    val fc = new FeatureConfiguration();
    assertEquals(100, fc.getIntegerToggle("hello.integer"));
  }

  @Test
  void shouldHaveDefaultIntegerToggles() {
    val fc = new FeatureConfiguration();
    assertEquals(0, fc.getIntegerToggle("hello.integer"));
    assertEquals(10, fc.getIntegerToggle("hello.integer", 10));
  }

  @Test
  @SetSystemProperty(key = "hello.double", value = "200.5")
  void shouldHaveDoubleToggle() {
    val fc = new FeatureConfiguration();
    assertEquals(200.5, fc.getDoubleToggle("hello.double"));
  }

  @Test
  void shouldHaveDefaultDoubleToggles() {
    val fc = new FeatureConfiguration();
    assertEquals(0, fc.getDoubleToggle("hello.double"));
    assertEquals(10.1, fc.getDoubleToggle("hello.double", 10.1));
  }

  @Test
  @SetSystemProperty(key = "hello.enum", value = "hello_world")
  void shouldMapToEnum() {
    val fc = new FeatureConfiguration();
    assertEquals(
        TestToggle.HELLO_WORLD,
        fc.getEnumToggle("hello.enum", TestToggle.class, TestToggle.TOGGLE_A));
  }

  @Test
  void shouldMapToDefaultEnum() {
    val fc = new FeatureConfiguration();
    assertFalse(fc.hasToggle("hello.enum"));
    assertEquals(
        TestToggle.TOGGLE_A, fc.getEnumToggle("hello.enum", TestToggle.class, TestToggle.TOGGLE_A));
  }

  @Test
  @SetSystemProperty(key = "hello.enum", value = "hello_world")
  void shouldMapFromFeatureToggle() {
    val fc = new FeatureConfiguration();
    val tft = new TestFeatureToggle();

    assertTrue(fc.hasToggle("hello.enum"));
    assertEquals(TestToggle.HELLO_WORLD, fc.getToggle(tft));
  }

  @Test
  void shouldMapFromDefaultFeatureToggle() {
    val fc = new FeatureConfiguration();
    val tft = new TestFeatureToggle();

    assertFalse(fc.hasToggle("hello.enum"));
    assertEquals(TestToggle.TOGGLE_B, fc.getToggle(tft));
  }

  public enum TestToggle {
    TOGGLE_A,
    TOGGLE_B,
    HELLO_WORLD
  }

  public static class TestFeatureToggle implements FeatureToggle<TestToggle> {

    @Override
    public String getKey() {
      return "hello.enum";
    }

    @Override
    public Function<String, TestToggle> getConverter() {
      return value -> TestToggle.valueOf(value.toUpperCase());
    }

    @Override
    public TestToggle getDefaultValue() {
      return TestToggle.TOGGLE_B;
    }
  }
}
