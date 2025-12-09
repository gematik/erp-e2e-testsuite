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
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de

@PRODUKT:eRp_FdV
@iosNightly
Funktionalität: Apothekensuche

  @TCID:ERP_FDV_APOTHEKENSUCHE_01
  @Funktionalität:Apothekensuche
  Szenario: Apothekensuche
    Wenn die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Dann kann die Versicherte Alice die folgenden Apotheken mit den Belieferungsoptionen in der Apothekensuche finden:
      | Name                 | Versand | Botendienst | Abholung |
      | Adelheid Ulmendorfer | true    | true        | true     |