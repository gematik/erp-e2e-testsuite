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

package de.gematik.test.erezept.cli.cmd.generate;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.cli.description.*;
import de.gematik.test.erezept.cli.exceptions.*;
import de.gematik.test.erezept.cli.indexmap.*;
import de.gematik.test.erezept.cli.param.OutputDirectoryParameter;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.fuzzing.core.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;
import picocli.CommandLine.*;

@Slf4j
@Getter
public abstract class BaseResourceGenerator implements Callable<Integer> {

  @Option(
      names = "-n",
      paramLabel = "<NUM>",
      type = Integer.class,
      defaultValue = "1",
      description = "Number of Resources to produce (default=${DEFAULT-VALUE})")
  protected int numOfElements;

  @Option(
      names = "--encoding",
      paramLabel = "<TYPE>",
      type = EncodingType.class,
      defaultValue = "XML",
      description =
          "Type of the encoding to use. Choose one from ${COMPLETION-CANDIDATES} (default=${DEFAULT-VALUE})")
  protected EncodingType encodingType;

  @Option(
      names = "--minify",
      type = Boolean.class,
      description =
          """
  Minify by preventing pretty print of the output (default=${DEFAULT-VALUE})
  """)
  protected boolean minify = false;

  @Option(
      names = "--invalidate",
      type = Boolean.class,
      description =
          """
          Activate Invalidation of FHIR Resources via predefined Manipulators.
          Attention: depending on the -n option, this might result in a huge amount of generated examples
          """)
  protected boolean invalidate = false;

  @Mixin protected OutputDirectoryParameter outputDirectory;

  public boolean shouldInvalidate() {
    return invalidate;
  }

  protected <R extends Resource> int create(String author, Supplier<R> resourceSupplier) {
    return create(author, resourceSupplier, null, null);
  }

  protected <R extends Resource> int create(
      String author,
      Supplier<R> resourceSupplier,
      @Nullable List<NamedEnvelope<FuzzingMutator<R>>> mutators,
      @Nullable UnaryOperator<R> copyFunction) {

    if (mutators != null && copyFunction == null) {
      // do a quick check beforehand to avoid implementation errors
      throw new CliException("Mutators for invalidation were given without a copy function");
    }

    val fhir = new FhirParser();
    val describer = new FhirResourceDescriber();
    val edm = ExampleDetailsMap.forAuthor(author);
    for (var i = 0; i < this.getNumOfElements(); i++) {
      val resource = resourceSupplier.get();
      log.info(
          format(
              "Create {0} ({1}) {2}",
              resource.getResourceType(), resource.getClass().getSimpleName(), resource.getId()));
      val description = describer.acceptResource(resource);
      val entry = this.writeFhirResource(fhir, resource, description);
      edm.addEntry(entry);

      if (this.shouldInvalidate()) {
        if (mutators == null) {
          log.warn(
              format(
                  "Invalidation Manipulators for {0} not yet implemented",
                  resource.getClass().getSimpleName()));
        } else {
          var index = 1;
          for (val m : mutators) {
            log.info(
                format(
                    "Manipulate {0} {1} with {2}",
                    resource.getClass().getSimpleName(), resource.getId(), m.getName()));
            val fResource = copyFunction.apply(resource);
            m.getParameter().accept(fResource); // perform the Manipulator!!
            val fDescription = format("{0} | Manipulator: {1}", description, m.getName());
            val postfix = String.format("%02d", index++);
            val fEntry = this.writeFhirResource(fhir, fResource, postfix, fDescription);
            edm.addEntry(fEntry);
          }
        }
      }
    }
    edm.write(this.getOutputDirectory().getOut());
    return ExitCode.OK;
  }

  protected ExampleEntry writeFhirResource(FhirParser fhir, Resource resource, String description) {
    return writeFhirResource(fhir, resource, "", description);
  }

  protected ExampleEntry writeFhirResource(
      FhirParser fhir, Resource resource, String fileNamePostfix, String description) {
    val content = fhir.encode(resource, encodingType, !minify);

    val fileName = resource.getId();
    val result = fhir.validate(content);

    val validDir = result.isSuccessful() ? "valid" : "invalid";
    String fileNameWithExt;
    if (fileNamePostfix == null || fileNamePostfix.isBlank() || fileName.isEmpty()) {
      fileNameWithExt = format("{0}.{1}", fileName, encodingType.toFileExtension());
    } else {
      fileNameWithExt =
          format("{0}_{1}.{2}", fileName, fileNamePostfix, encodingType.toFileExtension());
    }
    val relativeFileName = format("{0}/{1}", validDir, fileNameWithExt);

    writeResource(content, fileNameWithExt, result.isSuccessful());

    val entry = new ExampleEntry();
    entry.setFileType(encodingType);
    entry.setFileName(relativeFileName);
    entry.setDescription(description);
    entry.setErrors(result);
    return entry;
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  private void writeResource(String content, String filename, boolean valid) {
    val subdirectory = valid ? "valid" : "invalid";
    outputDirectory.writeFile(filename, subdirectory, content);
  }
}
