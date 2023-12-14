package de.gematik.test.erezept.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModuleConfigurationScope implements ConfigurationScope {
    KONNEKTOR_CLIENT("konnektor.client", "konnektor");

    private final String scopePrefix;
    private final String defaultDirectoryName;
}
