/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.cli.indexmap;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@Slf4j
public class ExampleDetailsMap {

  private Meta meta;
  private List<ExampleEntry> entries = new LinkedList<>();

  public void addEntry(ExampleEntry entry) {
    entries.add(entry);
  }

  public void write(Path path) {
    write(path, "example_details.json");
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing file by intention
  public void write(Path path, String fileName) {
    val mapper = new ObjectMapper();
    val summary = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);

    val out = Path.of(path.toAbsolutePath().toString(), fileName).toFile();
    path.toAbsolutePath().toFile().mkdirs();
    log.trace(format("Write Example Details to {0}", out.getAbsolutePath()));
    try (val writer = new BufferedWriter(new FileWriter(out))) {
      writer.write(summary);
    }
  }

  public static ExampleDetailsMap forAuthor(String author) {
    val meta = Meta.createMeta(author);
    val edm = new ExampleDetailsMap();
    edm.setMeta(meta);
    return edm;
  }

  public static ExampleDetailsMap forCurrentUser() {
    return forAuthor(System.getProperty("user.name"));
  }
}
