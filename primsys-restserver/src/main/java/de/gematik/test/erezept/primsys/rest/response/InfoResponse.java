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

package de.gematik.test.erezept.primsys.rest.response;

import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.rest.data.TelematikData;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.val;

@Data
@XmlRootElement
public class InfoResponse {

  private int doctors;
  private int pharmacies;
  private BuildInfoData build;
  private Map<String, Object> fhir;
  private TelematikData ti;

  public static InfoResponse getInfo(ActorContext ctx) {
    val buildInfo = new BuildInfoData();
    buildInfo.setBuildDate(System.getenv().getOrDefault("BUILD_DATE", "n/a"));
    buildInfo.setHash(System.getenv().getOrDefault("COMMIT_HASH", "n/a"));
    buildInfo.setVersion(System.getenv().getOrDefault("VERSION", "n/a"));

    val info = new InfoResponse();
    info.setBuild(buildInfo);
    info.setFhir(createFhirInfo());
    info.setDoctors(ctx.getDoctors().size());
    info.setPharmacies(ctx.getPharmacies().size());

    val ti = new TelematikData();
    ti.setEnvironment(ctx.getEnvironment().getName());
    ti.setFachdienst(ctx.getEnvironment().getTi().getFdBaseUrl());
    ti.setDiscoveryDocument(ctx.getEnvironment().getTi().getDiscoveryDocumentUrl());
    ti.setTsl(ctx.getEnvironment().getTslBaseUrl());
    info.setTi(ti);
    
    return info;
  }

  private static Map<String, Object> createFhirInfo() {
    val profilesInfos = new HashMap<String, String>();

    val conf = ParserConfigurations.getInstance();
    val defaultConf = conf.getDefaultConfiguration().orElse(conf.getProfileSettings().get(0));

    defaultConf
        .getProfiles()
        .forEach(profile -> profilesInfos.put(profile.getName(), profile.getVersion()));

    val envSetVersion =
        System.getProperty(
            ParserConfigurations.SYS_PROP_TOGGLE, System.getenv(ParserConfigurations.ENV_TOGGLE));

    val fhir = new HashMap<String, Object>();
    if (envSetVersion == null || envSetVersion.isEmpty() || envSetVersion.isBlank()) {
      fhir.put("configured", "default");
    } else {
      fhir.put("configured", envSetVersion);
    }
    fhir.put("configuration", profilesInfos);

    return fhir;
  }
}
