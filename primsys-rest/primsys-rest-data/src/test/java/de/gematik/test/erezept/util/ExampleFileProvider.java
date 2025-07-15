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

package de.gematik.test.erezept.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;

public class ExampleFileProvider {

  private static final ClassLoader CLASS_LOADER = ExampleFileProvider.class.getClassLoader();

  public static Stream<File> getCoverageExamples() {
    return readResourceFiles("examples/coverages");
  }

  public static Stream<File> getEvdgaRequestExamples() {
    return readResourceFiles("examples/evdgabundlerequest");
  }

  public static Stream<File> getPznMedicationExamples() {
    return readResourceFiles("examples/medications/pzn");
  }

  public static Stream<File> getDispensedPznMedicationExamples() {
    return readResourceFiles("examples/medications/dispensed");
  }

  public static Stream<File> getPatientExamples() {
    return readResourceFiles("examples/patients");
  }

  public static Stream<File> getMedicationRequestExamples() {
    return readResourceFiles("examples/medicationrequests");
  }

  public static Stream<File> getHealthAppRequestExamples() {
    return readResourceFiles("examples/healthapprequests");
  }

  public static Stream<File> getAcceptedExamples() {
    return readResourceFiles("examples/accepted");
  }

  public static Stream<File> getChargeItemExamples() {
    return readResourceFiles("examples/chargeitems");
  }

  public static Stream<File> getInfoExamples() {
    return readResourceFiles("examples/info");
  }

  public static Stream<File> getActorSummaries() {
    return readResourceFiles("examples/actors/summary");
  }

  public static Stream<File> readResourceFiles(String dir) {
    return readResourceFiles(dir, Integer.MAX_VALUE);
  }

  @SneakyThrows
  public static Stream<File> readResourceFiles(String dir, int maxDepth) {
    val pathUrl = CLASS_LOADER.getResource(dir).toURI();
    val path = Path.of(String.valueOf(Paths.get(pathUrl)));
    return Files.find(path, maxDepth, (filePath, fileAttr) -> fileAttr.isRegularFile())
        .map(Path::toFile);
  }
}
