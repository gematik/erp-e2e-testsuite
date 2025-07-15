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

package de.gematik.test.erezept.cli.converter;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.exceptions.CliException;
import java.util.List;
import java.util.stream.Stream;
import picocli.CommandLine.ITypeConverter;

public class StringListConverter implements ITypeConverter<List<String>> {

  @Override
  public List<String> convert(String value) throws Exception {
    if (isNullOrEmpty(value)) {
      throw new CliException(format("Given List-Argument is invalid: {0}", value));
    }
    return Stream.of(value.split(",")).filter(v -> !isNullOrEmpty(v)).map(String::trim).toList();
  }

  private boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty() || value.isBlank();
  }
}
