/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.cli.indexmap;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.val;

@Data
public class Meta {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
  private MetaVersion metaJsonVersion = MetaVersion.V1_0_0;
  private String lastUpdated;
  private String author;
  private String source;

  public void setLastUpdated(Instant instant) {
    this.lastUpdated = FORMATTER.format(instant);
  }

  public void setLastUpdated(String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public static Meta createMeta(String author) {
    val meta = new Meta();
    meta.setMetaJsonVersion(MetaVersion.V1_0_0);
    meta.setAuthor(author);
    meta.setSource("https://github.com/gematik/eRezept-Examples");
    meta.setLastUpdated(Instant.now());
    return meta;
  }
}
