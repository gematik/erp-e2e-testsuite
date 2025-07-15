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

package de.gematik.test.erezept.apimeasure;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import java.nio.file.Path;
import java.util.*;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class DumpingStopwatch implements ApiCallStopwatch {

  private final Map<ClientType, List<ApiCallMeasurement>> measurements;
  private final String name;

  public DumpingStopwatch(String name) {
    this.measurements = new EnumMap<>(ClientType.class);
    this.name = name;
  }

  @Override
  public <T extends Resource> void measurement(
      ClientType type, ICommand<T> command, ErpResponse<T> response) {
    val m = new ApiCallMeasurement();
    m.setFhirResource(command.getFhirResource());
    m.setRestPath(command.getRequestLocator());
    m.setHttpMethod(command.getMethod().name());
    m.setRequestBodyType(this.getResourceType(command));
    m.setResponseBodyType(this.getResourceType(response));
    m.setReturnCode(response.getStatusCode());
    m.setDuration(response.getDuration().toMillis());

    val tm = measurements.computeIfAbsent(type, k -> new LinkedList<>());
    tm.add(m);
  }

  @SneakyThrows
  public void close() {
    val basePath = Path.of(System.getProperty("user.dir"), "target", "stopwatch");
    basePath.toFile().mkdirs();

    val mapper = new ObjectMapper();
    val objectWriter = mapper.writerWithDefaultPrettyPrinter();
    val indexFile = basePath.resolve(format("{0}.json", this.name)).toFile();
    objectWriter.writeValue(indexFile, this.measurements);
  }
}
