package de.gematik.test.erezept.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TestsuiteconfigurationScope implements ConfigurationScope {
  ERP_APP("erp.app", "erp-app"),
  ERP_PRIMSYS("erp.primsys", "primsys");

  private final String scopePrefix;
  private final String defaultDirectoryName;
}
