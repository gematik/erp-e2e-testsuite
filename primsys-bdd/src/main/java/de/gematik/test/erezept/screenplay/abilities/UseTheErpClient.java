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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.cfg.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhirdump.*;
import java.time.*;
import java.time.format.*;
import javax.annotation.*;
import lombok.*;
import lombok.experimental.Delegate;
import net.serenitybdd.core.*;
import net.serenitybdd.screenplay.*;
import org.hl7.fhir.r4.model.Resource;

public class UseTheErpClient implements Ability {

  @Delegate(excludes = DelegateExclude.class)
  private final ErpClient client;

  private final ErpClientConfiguration config;

  private UseTheErpClient(ErpClient client, ErpClientConfiguration config) {
    this.client = client;
    this.config = config;
  }

  public static UseTheErpClient with(ErpClientConfiguration config) {
    val client = ErpClientFactory.createErpClient(config);
    return new UseTheErpClient(client, config);
  }

  public void authenticateWith(UseTheKonnektor withKonnector) {
    this.client.authenticateWith(
        withKonnector.getSmcbAuthCertificate().getPayload(),
        challenge -> withKonnector.externalAuthenticate(challenge).getPayload());
  }

  public <R extends Resource> ErpResponse request(ICommand<R> command) {
    reportRequest(command);
    val response = this.client.request(command);
    reportResponse(response);
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

  private void reportResponse(ErpResponse response) {
    val resource = response.getResource();
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
      reportFhirResource(title, resource);
    }
  }

  protected String reportFhirResource(String title, @Nullable Resource resource) {
    var content = "[]";
    if (resource != null) {
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
        config.getClientType(), config.getFdBaseUrl());
  }

  /**
   * This interface will allow overriding certain methods from the ErpClient with a slightly
   * different implementation
   */
  private interface DelegateExclude {
    <R extends Resource> ErpResponse request(ICommand<R> command);
  }
}
