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

package de.gematik.test.erezept.client.usecases.eu;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.usecases.BaseCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class EuGetPrescriptionPostCommand extends BaseCommand<EuPrescriptionBundle> {

  private final EuGetPrescriptionInput requestBody;

  public EuGetPrescriptionPostCommand(EuGetPrescriptionInput euGetPrescriptionInput) {
    super(EuPrescriptionBundle.class, HttpRequestMethod.POST, "$get-eu-prescriptions");
    this.requestBody = euGetPrescriptionInput;
  }

  /**
   * Constructor with optional QueryParams
   *
   * @param euGetPrescriptionInput
   * @param searchParameters
   */
  public EuGetPrescriptionPostCommand(
      EuGetPrescriptionInput euGetPrescriptionInput, List<IQueryParameter> searchParameters) {
    super(EuPrescriptionBundle.class, HttpRequestMethod.POST, "$get-eu-prescriptions");
    Optional.ofNullable(searchParameters).ifPresent(queryParameters::addAll);
    this.requestBody = euGetPrescriptionInput;
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(requestBody);
  }

  public static EuGetPrescriptionPostCommand forDemographics(
      EuGetPrescriptionInput euGetPrescriptionInput) {
    return new EuGetPrescriptionPostCommand(
        euGetPrescriptionInput, IQueryParameter.search().withCount(1).createParameter());
  }

  public static EuGetPrescriptionPostCommand forEuPrescriptions(
      EuGetPrescriptionInput euGetPrescriptionInput) {
    return new EuGetPrescriptionPostCommand(euGetPrescriptionInput);
  }

  public static EuGetPrescriptionPostCommand forEuPrescriptions(
      EuGetPrescriptionInput euGetPrescriptionInput, int count) {
    return new EuGetPrescriptionPostCommand(
        euGetPrescriptionInput, IQueryParameter.search().withCount(count).createParameter());
  }
}
