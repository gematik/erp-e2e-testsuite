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

package de.gematik.test.smartcard.factory;

import static org.junit.Assert.*;

import lombok.val;
import org.junit.Test;

public class LdapReaderTest {

  @Test
  public void getReferenceOwnerData() {
    String subjectBernd =
        "GIVENNAME=Bernd + SURNAME=Claudius + SERIALNUMBER=16.80276001011699910102 + CN=Arzt Bernd Claudius TEST-ONLY, C=DE";
    String subjectGunther =
        "SURNAME=Gunther + GIVENNAME=Gündüla + SERIALNUMBER=11.80276001081699900578 + CN=Dr. med. Gündüla Gunther ARZT TEST-ONLY, C=DE";
    //        String subjectAmanda = "GIVENNAME=Amanda + SURNAME=Albrecht +
    // SERIALNUMBER=11.80276001081699900579 + CN=Dr. Amanda Albrecht APO TEST-ONLY, C=DE";
    //        String subjectBernd2 = "CN=Arztpraxis Bernd Claudius TEST-ONLY, GIVENNAME=Bernd,
    // SURNAME=Claudius, O=202110001 NOT-VALID, C=DE";

    val bernd = LdapReader.getOwnerData(subjectBernd);
    assertEquals("Bernd", bernd.getGivenName());
    assertEquals("Claudius", bernd.getSurname());
    assertEquals("Arzt Bernd Claudius TEST-ONLY", bernd.getCommonName());

    val gunther = LdapReader.getOwnerData(subjectGunther);
    assertEquals("Gündüla", gunther.getGivenName());
    assertEquals("Gunther", gunther.getSurname());
    assertEquals("Dr. med. Gündüla Gunther ARZT TEST-ONLY", gunther.getCommonName());
  }
}
