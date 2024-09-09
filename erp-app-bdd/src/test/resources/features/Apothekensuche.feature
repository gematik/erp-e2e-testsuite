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

@PRODUKT:eRp_FdV
@iosNightly
Funktionalität: Apothekensuche

  @TCID:ERP_FDV_APOTHEKENSUCHE_01
  @Funktionalität:Apothekensuche
  Szenario: Apothekensuche ohne vorherige Anmeldung mittels eGK in der TI
    Wenn die Versicherte Alice ihr Smartphone für die Nutzung ohne TI eingerichtet hat
    Dann kann die Versicherte Alice die folgenden Apotheken mit den Belieferungsoptionen in der Apothekensuche finden:
      | Name              | Versand | Botendienst | Abholung |
      | ZoTI_01_TEST-ONLY | false   | false       | false    |
      | ZoTI_02_TEST-ONLY | false   | false       | true     |
      | ZoTI_03_TEST-ONLY | false   | true        | false    |
      | ZoTI_04_TEST-ONLY | true    | false       | false    |
      | ZoTI_05_TEST-ONLY | false   | false       | false    |
      | ZoTI_06_TEST-ONLY | false   | false       | false    |
      | ZoTI_07_TEST-ONLY | false   | false       | false    |
      | ZoTI_08_TEST-ONLY | true    | true        | true     |
      | ZoTI_10_TEST-ONLY | true    | true        | true     |

  @TCID:ERP_FDV_APOTHEKENSUCHE_02
  @Funktionalität:Apothekensuche
  Szenario: Apothekensuche mit vorherige Anmeldung mittels eGK in der TI
    Wenn die GKV Versicherte Alice die E-Rezept App auf ihrem Smartphone eingerichtet hat
    Dann kann die Versicherte Alice die folgenden Apotheken mit den Belieferungsoptionen in der Apothekensuche finden:
      | Name              | Versand | Botendienst | Abholung |
      | ZoTI_01_TEST-ONLY | false   | false       | false    |
      | ZoTI_02_TEST-ONLY | false   | false       | true     |
      | ZoTI_03_TEST-ONLY | false   | true        | false    |
      | ZoTI_04_TEST-ONLY | true    | false       | false    |
      | ZoTI_05_TEST-ONLY | false   | false       | true     |
      | ZoTI_06_TEST-ONLY | false   | true        | false    |
      | ZoTI_07_TEST-ONLY | true    | false       | false    |
      | ZoTI_08_TEST-ONLY | true    | true        | true     |
      | ZoTI_10_TEST-ONLY | true    | true        | true     |
      | ZoTI_12_TEST-ONLY | true    | true        | true     |