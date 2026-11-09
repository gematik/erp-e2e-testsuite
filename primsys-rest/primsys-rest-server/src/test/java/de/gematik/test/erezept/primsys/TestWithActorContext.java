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

package de.gematik.test.erezept.primsys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.cfg.ErpClientFactoryUtil;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.primsys.model.ActorContext;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;

public abstract class TestWithActorContext extends ErpFhirParsingTest {

  protected static final PrimsysConfigurationDto primsysConfig;
  protected static final PrimSysRestFactory primsysFactory;
  protected static final ErpClientFactory erpClientFactory;

  static {
    primsysConfig = ConfigurationReader.forPrimSysConfiguration().create();
    val sca = SmartcardArchive.fromResources();

    erpClientFactory = ErpClientFactoryUtil.createMockedErpClientFactory(sca, parser);
    primsysFactory = PrimSysRestFactory.from(primsysConfig, erpClientFactory);

    ActorContext.init(primsysFactory);
  }

  @BeforeEach
  void resetMocks() {
    ActorContext.getInstance()
        .getActors()
        .forEach(
            a -> {
              val erpClient = a.getClient();
              reset(erpClient); // clear all previous mocks from the client

              // now mock the fhir parser back in
              when(erpClient.getFhir()).thenReturn(parser);
              when(erpClient.decode(any(), any()))
                  .thenAnswer(
                      (Answer<IBaseResource>)
                          invocationOnMock -> {
                            val args = invocationOnMock.getArguments();
                            return parser.decode((Class<Resource>) args[0], (String) args[1]);
                          });
              when(erpClient.encode(any(), any()))
                  .thenAnswer(
                      (Answer<String>)
                          invocationOnMock -> {
                            val args = invocationOnMock.getArguments();
                            return parser.encode((IBaseResource) args[0], (EncodingType) args[1]);
                          });
            });
  }
}
