#
# Copyright 2023 gematik GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: de

@PRODUKT:eRp_FD
@Versicherung:GKV
Funktionalität: Längenprüfung der PZN
  Der E-Rezept-Fachdienst muss beim Aktivieren eines Tasks prüfen, ob die übergebene PZN 8-stellig ist.
  Die PZN kann eine führende "0" enthalten. Der Fachdienst bricht das Aktivieren mit Fehlercode 400 und "Länge PZN unzulässig (muss 8-stellig sein)"im OperationOutcome ab.


  @TCID:ERP_EE_PZN_01
  @TESTFALL:negativ
  @AFO-ID:A_22925
  @Hauptdarsteller:Fachdienst
  Szenariogrundriss:  Längenprüfung PZN: 7- bzw. 9-stellig ist ungültig

    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Dann kann die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann kein E-Rezept verschreiben, weil die PZN eine falsche Länge hat
      | Name          | PZN   |
      | Schmerzmittel | <PZN> |

    Beispiele:
      | PZN       |
      | 0123456   |
      | 1234567   |
      | 123456789 |
      | 012345678 |
