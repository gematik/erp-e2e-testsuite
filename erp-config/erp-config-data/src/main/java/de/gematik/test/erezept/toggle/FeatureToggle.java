package de.gematik.test.erezept.toggle;

import java.util.function.Function;

public interface FeatureToggle<T> {

  String getKey();

  Function<String, T> getConverter();

  T getDefaultValue();
}
