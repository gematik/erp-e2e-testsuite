/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.primsys.rest.data;

import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.val;

@Data
@XmlRootElement
public class DoctorData extends ActorData {

  private String officeName;
  private String hba;
  private String smcb;
  private String docNumber;
  private String docNumberType;
  private String docQualificationType;
  private String bsnr;
  private String phone;
  private String email;
  private String city;
  private String postal;
  private String street;

  public static DoctorData fromKbvBundle(Doctor signingDoc, KbvErpBundle bundle) {
    val org = bundle.getMedicalOrganization();
    val doc = bundle.getPractitioner();
    val d = new DoctorData();
    d.setName(doc.getFullName());
    d.setType(signingDoc.getBaseData().getType());
    d.setId(signingDoc.getBaseData().getId());
    d.setTi(signingDoc.getBaseData().getTi());

    d.officeName = org.getName();
    d.hba = signingDoc.getBaseData().getHba();
    d.smcb = signingDoc.getBaseData().getSmcb();
    d.docNumberType = doc.getANRType().name();
    d.docNumber = doc.getANR().getValue();
    d.docQualificationType = QualificationType.DOCTOR.getDisplay();
    d.bsnr = org.getBsnr().getValue();
    d.phone = org.getPhone();
    d.email = org.getMail();
    d.city = org.getCity();
    d.postal = org.getPostalCode();
    d.street = org.getStreet();

    return d;
  }
}
