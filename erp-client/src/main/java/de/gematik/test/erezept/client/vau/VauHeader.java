/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.client.vau;

import de.gematik.bbriccs.rest.HttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import lombok.Getter;

@Getter
public enum VauHeader implements HttpHeaderKey {
  X_ERP_RESOURCE("X-erp-resource"),
  X_ERP_USER("X-erp-user");

  private final String key;

  VauHeader(final String key) {
    this.key = key;
  }

  @Override
  public HttpHeader createHeader(String value) {
    return new HttpHeader(this.key, value);
  }
}
