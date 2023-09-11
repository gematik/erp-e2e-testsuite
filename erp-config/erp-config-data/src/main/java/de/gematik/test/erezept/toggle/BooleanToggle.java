package de.gematik.test.erezept.toggle;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;

public class BooleanToggle implements FeatureToggle<Boolean> {

  private static final List<String> BOOL_TRUE_VALUES = List.of("yes", "1");

  @Getter private final String key;
  @Getter private final boolean defaultValue;

  protected BooleanToggle(String key, boolean defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Override
  public Function<String, Boolean> getConverter() {
    return this::mapBoolean;
  }

  @Override
  public Boolean getDefaultValue() {
    return defaultValue;
  }

  private boolean mapBoolean(String value) {
    if (BOOL_TRUE_VALUES.contains(value.toLowerCase())) {
      return true;
    } else {
      return Boolean.parseBoolean(value);
    }
  }

  public static BooleanToggle forKey(String key) {
    return forKey(key, false);
  }

  public static BooleanToggle forKey(String key, boolean defaultValue) {
    return new BooleanToggle(key, defaultValue);
  }
}
