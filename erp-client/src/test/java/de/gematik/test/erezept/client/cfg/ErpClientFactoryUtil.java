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

package de.gematik.test.erezept.client.cfg;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.rest.fd.MediaType;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.testutils.VauCertificateGenerator;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.HealthInsuranceConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.IdpCentralConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import java.util.function.Supplier;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.mockito.stubbing.Answer;

public class ErpClientFactoryUtil {

  public static ErpClientFactory createMockedErpClientFactory() {
    return createMockedErpClientFactory(SmartcardArchive.fromResources());
  }

  public static ErpClientFactory createMockedErpClientFactory(FhirCodec codec) {
    val smartcards = SmartcardArchive.fromResources();
    return createMockedErpClientFactory(smartcards, codec);
  }

  public static ErpClientFactory createMockedErpClientFactory(SmartcardArchive smartcards) {
    val codec = FhirCodec.forR4().andDummyValidator();
    return createMockedErpClientFactory(smartcards, codec);
  }

  public static ErpClientFactory createMockedErpClientFactory(
      SmartcardArchive smartcards, FhirCodec codec) {
    val env = new EnvironmentConfiguration();
    env.setName("TU");

    val route = new BackendRouteConfiguration();
    route.setFdBaseUrl("https://fd.tu.gematik.de");
    route.setUserAgent("ErpClientFactoryUtilTest");
    route.setXapiKey("test-x-api-key");
    env.setTi(route);
    env.setInternet(route);

    val idp = new IdpCentralConfiguration();
    idp.setClientId("test-client-id");
    idp.setRedirectUrl("https://test-redirect-url");
    idp.setDiscoveryDocument("https://test-discovery-document");
    route.setIdp(idp);

    Supplier<ErpClientFactory> factoryBuilder =
        () ->
            ErpClientFactory.forEnvironment(env)
                .withSmartcardArchive(smartcards)
                .withVauCertificate(VauCertificateGenerator.generateRandomVauCertificate())
                .build();
    return callInMockedStaticContext(env, smartcards, codec, factoryBuilder);
  }

  /**
   * Calls the given supplier in a mocked static context of the ErpClientFactory, so that all calls
   * to ErpClientFactory.forEnvironment() return a mocked factory that returns a mocked ErpClient.
   * This is useful for testing code that uses the ErpClientFactory without actually making any
   * network calls.
   *
   * @param config the PrimsysConfigurationDto to use
   * @param sca the SmartcardArchive to use
   * @param codec the FhirCodec to use
   * @param supplier the Supplier to call in the mocked context
   * @return the result of the supplier
   * @param <T> the type of the result
   */
  public static <T> T callInMockedStaticContext(
      PrimsysConfigurationDto config, SmartcardArchive sca, FhirCodec codec, Supplier<T> supplier) {

    val activeEnv =
        config.getEnvironments().stream()
            .filter(it -> it.getName().equalsIgnoreCase(config.getActiveEnvironment()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Active environment not found"));
    return callInMockedStaticContext(activeEnv, sca, codec, supplier);
  }

  public static <T> T callInMockedStaticContext(
      EnvironmentConfiguration activeEnv,
      SmartcardArchive sca,
      FhirCodec codec,
      Supplier<T> supplier) {

    val smcb = sca.getSmcB(0);
    val egk = sca.getEgk(0);
    val erpClientMock = createMockedErpClient(codec);
    val dummyVauCertificate = VauCertificateGenerator.generateRandomVauCertificate();

    val factoryBuilder =
        spy(
            ErpClientFactory.forEnvironment(activeEnv)
                .withVauCertificate(dummyVauCertificate)
                .withSmartcardArchive(sca));
    val factoryMock = mock(ErpClientFactory.class);

    val psBuilderMock = mock(ErpClientPsBuilder.class);
    when(factoryBuilder.withSmartcardArchive(any())).thenReturn(factoryBuilder);
    when(factoryBuilder.build()).thenReturn(factoryMock);

    when(factoryMock.getSmartcards()).thenReturn(sca);
    when(factoryMock.getEnv()).thenReturn(activeEnv);
    when(factoryMock.prepareStagedClientFor(any(PsActorConfiguration.class)))
        .thenReturn(psBuilderMock);
    when(factoryMock.prepareStagedClientFor(any(HealthInsuranceConfiguration.class)))
        .thenReturn(psBuilderMock);
    when(factoryMock.prepareStagedClientFor(any(EuPharmacyConfiguration.class)))
        .thenReturn(psBuilderMock);

    when(factoryMock.prepareStagedClientFor(any(PatientConfiguration.class)))
        .thenReturn(Pair.of(erpClientMock, egk));
    when(factoryMock.prepareStagedClientFor(any(ErpActorConfiguration.class)))
        .thenReturn(Pair.of(erpClientMock, egk));
    when(factoryMock.prepareStagedClientFor(any(PatientConfiguration.class), any()))
        .thenReturn(Pair.of(erpClientMock, egk));
    when(factoryMock.prepareStagedClientWithDelegatedIdp(any(PatientConfiguration.class), any()))
        .thenReturn(Pair.of(erpClientMock, egk));
    when(factoryMock.prepareStagedClientFor(any(ErpActorConfiguration.class), any()))
        .thenReturn(Pair.of(erpClientMock, egk));

    when(psBuilderMock.getSmcb()).thenReturn(smcb);
    when(psBuilderMock.createWithExternalAuthentication(any(), any())).thenReturn(erpClientMock);
    when(psBuilderMock.createWithSmcBAuthentication()).thenReturn(erpClientMock);

    try (val erpClientFactoryStatic = mockStatic(ErpClientFactory.class)) {
      erpClientFactoryStatic
          .when(() -> ErpClientFactory.forEnvironment(any()))
          .thenReturn(factoryBuilder);

      return supplier.get();
    }
  }

  public static ErpClient createMockedErpClient(FhirCodec codec) {
    val erpClientMock = mock(ErpClient.class);
    when(erpClientMock.getFhir()).thenReturn(codec);
    when(erpClientMock.encode(any(), any()))
        .thenAnswer(
            (Answer<String>)
                invocationOnMock -> {
                  val args = invocationOnMock.getArguments();
                  return codec.encode((IBaseResource) args[0], (EncodingType) args[1]);
                });
    when(erpClientMock.decode(any(), anyString()))
        .thenAnswer(
            (Answer<Resource>)
                invocationOnMock -> {
                  val args = invocationOnMock.getArguments();
                  return codec.decode((Class<? extends Resource>) args[0], (String) args[1]);
                });

    // don't care about the actual mime types, just return some valid ones
    when(erpClientMock.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_XML);
    when(erpClientMock.getSendMime()).thenReturn(MediaType.FHIR_XML);
    when(erpClientMock.getClientType()).thenReturn(ClientType.PS);
    return erpClientMock;
  }
}
