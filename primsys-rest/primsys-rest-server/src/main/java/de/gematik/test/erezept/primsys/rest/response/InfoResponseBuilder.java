/*
 * Copyright 2024 gematik GmbH
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
 */

package de.gematik.test.erezept.primsys.rest.response;

import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.primsys.data.info.BuildInfoDto;
import de.gematik.test.erezept.primsys.data.info.FhirInfoDto;
import de.gematik.test.erezept.primsys.data.info.InfoDto;
import de.gematik.test.erezept.primsys.data.info.TelematikInfoDto;
import de.gematik.test.erezept.primsys.model.ActorContext;
import lombok.val;

public class InfoResponseBuilder {

  private InfoResponseBuilder() throws IllegalAccessException {
    throw new IllegalAccessException("utility class");
  }

  public static InfoDto getInfo(ActorContext ctx) {
    val buildInfo = new BuildInfoDto();
    buildInfo.setBuildDate(System.getenv().getOrDefault("BUILD_DATE", "n/a"));
    buildInfo.setHash(System.getenv().getOrDefault("COMMIT_HASH", "n/a"));
    buildInfo.setVersion(System.getenv().getOrDefault("VERSION", "n/a"));

    val info = new InfoDto();
    info.setBuild(buildInfo);
    info.setFhir(createFhirInfo());
    info.setDoctors(ctx.getDoctors().size());
    info.setPharmacies(ctx.getPharmacies().size());

    val ti = new TelematikInfoDto();
    ti.setEnvironment(ctx.getEnvironment().getName());
    ti.setFachdienst(ctx.getEnvironment().getTi().getFdBaseUrl());
    ti.setDiscoveryDocument(ctx.getEnvironment().getTi().getDiscoveryDocumentUrl());
    ti.setTsl(ctx.getEnvironment().getTi().getTslBaseUrl());
    info.setTi(ti);

    return info;
  }

  private static FhirInfoDto createFhirInfo() {
    val fhirInfo = new FhirInfoDto();

    val conf = ParserConfigurations.getInstance();
    val defaultConf = conf.getDefaultConfiguration().orElse(conf.getProfileSettings().get(0));

    defaultConf
        .getProfiles()
        .forEach(
            profile -> fhirInfo.getConfiguration().put(profile.getName(), profile.getVersion()));

    val envSetVersion =
        System.getProperty(
            ParserConfigurations.SYS_PROP_TOGGLE, System.getenv(ParserConfigurations.ENV_TOGGLE));

    if (envSetVersion == null || envSetVersion.isEmpty() || envSetVersion.isBlank()) {
      fhirInfo.setConfigured("default");
    } else {
      fhirInfo.setConfigured(envSetVersion);
    }

    return fhirInfo;
  }
}
