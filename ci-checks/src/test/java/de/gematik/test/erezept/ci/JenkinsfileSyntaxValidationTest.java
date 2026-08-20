/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.ci;

import static org.junit.jupiter.api.Assertions.*;

import groovy.lang.GroovyShell;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.jupiter.api.Test;

class JenkinsfileSyntaxValidationTest {

  private static final Pattern LIBRARY_ANNOTATION =
      Pattern.compile("(?m)^\\s*@Library\\s*\\(.*\\)\\s*_\\s*$");

  @Test
  void shouldValidateAllJenkinsfilesInCiDirectory() throws IOException {
    var ciDirectory = resolveCiDirectory();
    assertTrue(
        Files.isDirectory(ciDirectory),
        () -> "Missing CI directory: " + ciDirectory.toAbsolutePath());

    var jenkinsfiles = listJenkinsfiles(ciDirectory);
    assertFalse(
        jenkinsfiles.isEmpty(), () -> "No Jenkinsfiles found in " + ciDirectory.toAbsolutePath());

    var failures = validate(jenkinsfiles);

    if (!failures.isEmpty()) {
      fail("Syntax errors found in Jenkinsfiles:\n - " + String.join("\n - ", failures));
    }
  }

  @Test
  void shouldValidateAllJenkinsfilesInRoot() throws IOException {
    var projectRoot = resolveProjectRoot();
    assertTrue(
        Files.isDirectory(projectRoot), () -> "Missing root: " + projectRoot.toAbsolutePath());

    var jenkinsfiles = listJenkinsfiles(projectRoot);
    assertFalse(
        jenkinsfiles.isEmpty(), () -> "No Jenkinsfiles found in " + projectRoot.toAbsolutePath());

    var failures = validate(jenkinsfiles);

    if (!failures.isEmpty()) {
      fail("Syntax errors found in Jenkinsfiles:\n - " + String.join("\n - ", failures));
    }
  }

  @Nonnull
  private static ArrayList<String> validate(List<Path> jenkinsfiles) throws IOException {
    var failures = new ArrayList<String>();
    for (var jenkinsfile : jenkinsfiles) {
      var content = Files.readString(jenkinsfile);
      var sanitizedContent =
          LIBRARY_ANNOTATION.matcher(content).replaceAll("// @Library removed for syntax check");
      try {
        new GroovyShell().parse(sanitizedContent, jenkinsfile.getFileName().toString());
      } catch (CompilationFailedException exception) {
        failures.add(jenkinsfile.getFileName() + ": " + compactError(exception));
      }
    }
    return failures;
  }

  private static List<Path> listJenkinsfiles(Path ciDirectory) throws IOException {
    try (Stream<Path> stream = Files.list(ciDirectory)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(JenkinsfileSyntaxValidationTest::isJenkinsfile)
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .toList();
    }
  }

  private static boolean isJenkinsfile(Path path) {
    var fileName = path.getFileName().toString();
    return fileName.endsWith(".Jenkinsfile") || fileName.equals("JENKINSFILE");
  }

  private static Path resolveCiDirectory() {
    var fromMavenProperty = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."));
    var fromUserDir = Path.of(System.getProperty("user.dir", "."));

    var directCandidate = fromMavenProperty.resolve("CI").normalize();
    if (Files.isDirectory(directCandidate)) {
      return directCandidate;
    }

    var moduleParentCandidate = fromUserDir.resolve("..").resolve("CI").normalize();
    if (Files.isDirectory(moduleParentCandidate)) {
      return moduleParentCandidate;
    }

    return directCandidate;
  }

  private static Path resolveProjectRoot() {
    var fromMavenProperty = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."));
    if (Files.isDirectory(fromMavenProperty.resolve("CI"))) {
      return fromMavenProperty.normalize();
    }

    var fromUserDir = Path.of(System.getProperty("user.dir", "."));
    if (Files.isDirectory(fromUserDir.resolve("CI"))) {
      return fromUserDir.normalize();
    }

    var moduleParentCandidate = fromUserDir.resolve("..").normalize();
    if (Files.isDirectory(moduleParentCandidate.resolve("CI"))) {
      return moduleParentCandidate;
    }

    return fromMavenProperty.normalize();
  }

  private static String compactError(CompilationFailedException exception) {
    return exception
        .getMessage()
        .lines()
        .map(String::trim)
        .filter(line -> !line.isBlank())
        .limit(6)
        .collect(Collectors.joining(" | "));
  }
}
