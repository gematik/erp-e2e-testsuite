package de.gematik.test.erezept.toggle;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class FeatureConfiguration {

  public final boolean hasToggle(String key) {
    return getToggle(key).isPresent();
  }

  public final Optional<String> getToggle(String key) {
    return Optional.ofNullable(
        System.getProperty(key, System.getenv(key.toUpperCase().replace(".", "_"))));
  }

  public final String getToggle(String key, String defaultValue) {
    return getToggle(key).orElse(defaultValue);
  }

  public final boolean getBooleanToggle(String key) {
    return getToggle(BooleanToggle.forKey(key));
  }

  public final boolean getBooleanToggle(String key, boolean defaultValue) {
    return getToggle(BooleanToggle.forKey(key, defaultValue));
  }

  public final int getIntegerToggle(String key) {
    return getIntegerToggle(key, 0);
  }

  public final int getIntegerToggle(String key, int defaultValue) {
    return getToggle(key, value -> Integer.parseInt(value, 10), defaultValue);
  }

  public final double getDoubleToggle(String key) {
    return getDoubleToggle(key, 0.0);
  }

  public final double getDoubleToggle(String key, double defaultValue) {
    return getToggle(key, Double::parseDouble, defaultValue);
  }

  public final <T extends Enum<?>> T getEnumToggle(String key, Class<T> type, T defaultValue) {
    Function<String, T> converter =
        value ->
            Arrays.stream(type.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(defaultValue);
    return getToggle(key, converter, defaultValue);
  }

  public final <T> Optional<T> getToggle(String key, Function<String, T> converter) {
    return getToggle(key).map(converter);
  }

  public final <T> T getToggle(FeatureToggle<T> featureToggle) {
    return getToggle(
        featureToggle.getKey(), featureToggle.getConverter(), featureToggle.getDefaultValue());
  }

  public final <T> T getToggle(String key, Function<String, T> converter, T defaultValue) {
    return getToggle(key, converter).orElse(defaultValue);
  }
}
