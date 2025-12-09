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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.test.erezept.apimeasure.ApiCallStopwatch;
import de.gematik.test.erezept.apimeasure.LoggingStopwatch;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import de.gematik.test.erezept.jwt.JWTDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Ability;
import org.hl7.fhir.r4.model.Resource;

public class UseTheErpClient implements Ability {

  @Getter
  @Delegate(excludes = DelegateExclude.class)
  private final ErpClient client;

  private final ApiCallStopwatch stopwatch;
  private final JWTDecoder jwtDecoder;

  private UseTheErpClient(ErpClient client, ApiCallStopwatch stopwatch) {
    this.client = client;
    this.stopwatch = stopwatch;
    this.jwtDecoder = JWTDecoder.withPrettyPrinter();
  }

  public static UseTheErpClient with(ErpClient client) {
    return with(client, new LoggingStopwatch());
  }

  public static UseTheErpClient with(ErpClient client, ApiCallStopwatch stopwatch) {
    return new UseTheErpClient(client, stopwatch);
  }

  public UseTheErpClient authenticatingWith(UseTheKonnektor konnektor) {
    this.authenticateWith(konnektor);
    return this;
  }

  public UseTheErpClient authenticatingWith(Smartcard smartcard) {
    this.authenticateWith(smartcard);
    return this;
  }

  public void authenticateWith(UseTheKonnektor withKonnektor) {
    this.client.authenticateWith(
        withKonnektor.getSmcbAuthCertificate().getPayload(),
        challenge -> withKonnektor.externalAuthenticate(challenge).getPayload());
  }

  public <R extends Resource> ErpResponse<R> request(ICommand<R> command) {
    reportRequest(command);
    val response = this.client.request(command);
    reportJwt(command, response);
    reportResponse(response);
    stopwatch.measurement(this.getClientType(), command, response);
    return response;
  }

  private void reportRequest(ICommand<?> cmd) {
    val title = format("{0} {1}", cmd.getMethod(), cmd.getRequestLocator());
    val requestBody = cmd.getRequestBody().orElse(null);
    val content = reportFhirResource(title, requestBody);

    if (requestBody != null) {
      val fileExtension = client.getSendMime().toFhirEncoding().toFileExtension();
      val dumper = FhirDumper.getInstance();
      val fileName = format("Request_{0}.{1}", requestBody.getResourceType(), fileExtension);
      dumper.writeDump(title, fileName, content);
    }
  }

  private void reportJwt(ICommand<?> cmd, ErpResponse<?> response) {
    val title = format("JWT for {0} {1}", cmd.getMethod(), cmd.getRequestLocator());
    val content = jwtDecoder.decodeToJson(response.getUsedJwt());
    Serenity.recordReportData().withTitle(title).andContents(content);
  }

  private void reportResponse(ErpResponse<?> response) {
    val resource = response.getAsBaseResource();
    var title = format("Response {0}", response.getStatusCode());
    if (resource != null) {
      var identifier = resource.getId();

      if (identifier == null) {
        // well, not all resources have an ID! use a timestamp in such cases
        identifier = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
      }
      title += format(": {0} {1}", resource.getResourceType(), identifier);
      val content = reportFhirResource(title, resource);

      val fileExtension = client.getAcceptMime().toFhirEncoding().toFileExtension();
      val dumper = FhirDumper.getInstance();
      val fileName = format("Response_{0}.{1}", resource.getResourceType(), fileExtension);
      dumper.writeDump(title, fileName, content);
    } else {
      reportFhirResource(title, null);
    }
  }

  protected String reportFhirResource(String title, @Nullable Resource resource) {
    var content = "[EMPTY BODY]";
    if (resource != null && !(resource instanceof EmptyResource)) {
      val fhir = client.getFhir();
      val encodingType = client.getAcceptMime().toFhirEncoding();
      content = fhir.encode(resource, encodingType, true);
    }
    Serenity.recordReportData().withTitle(title).andContents(content);
    return content;
  }

  public <T extends Resource> T decode(Class<T> expectedClass, @NonNull final String content) {
    return this.client.getFhir().decode(expectedClass, content);
  }

  @Override
  public String toString() {
    return format(
        "E-Rezept Client vom Typ {0} für die Kommunikation mit {1}",
        this.client.getClientType(), this.client.getBaseFdUrl());
  }

  /**
   * This interface will allow overriding certain methods from the ErpClient with a slightly
   * different implementation
   */
  private interface DelegateExclude {
    <R extends Resource> ErpResponse<R> request(ICommand<R> command);
  }
}
