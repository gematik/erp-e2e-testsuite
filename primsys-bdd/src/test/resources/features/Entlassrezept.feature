#
# Copyright (c) 2023 gematik GmbH
# 
# Licensed under the Apache License, Version 2.0 (the License);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: de

@Funktionalität=Entlassrezept
@Path=happy
@Impl=open
Funktionalität:  Entlassrezept
  Abweichender Wert für Task.AcceptDate der Rezepts, wenn die Entlassmanagement-Kennzeichen "04" oder "14" gesetzt sind (C_10721)
  Das in der http-POST-Operation /Task/<id>/$activate übergebene, gültig signierte E-Rezept-Bundle muss in der
  Extension https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Rechtsgrundlage  inBundle.Composition den
  code="04" oder "14" des Code-Systems https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN ("Entlassmanagement-Kennzeichen [...]") enthalten

  Task.AcceptDate = <Datum der QES.Erstellung Bundle.signature.when> + 3 Werktage (Montag bis Samstag, ausgenommen bundeseinheitliche Feiertage)

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK

  @TCID=ERP_IOP_C_10721_01
  @Path=happy
  @Afo=A_19517-02
  @MainActor=Arztpraxis
  Szenariogrundriss: Korrektes AcceptDate für Entlassrezepte
  GF Im Entlassrezept ist das Task-AcceptDate bei Entlassmanagement-Kennzeichen "04" oder "14" richtig gesetzt

    Wenn der Arzt Dr. Schraßer folgendes E-Rezept an die Versicherte Sina Hüllmann verschreibt:
      | KBV_Statuskennzeichen |
      | <Kennzeichen>         |
    Dann hat die Versicherte Sina Hüllmann ein letztes Rezept mit angepasstem Task.AcceptDate vorliegen

    Beispiele:
      | Kennzeichen |
      | 04          |
      | 14          |


  @TCID=ERP_IOP_C_10721_02
  @Path=happy
  @Afo=A_19517-02
  @MainActor=Arztpraxis
  Szenariogrundriss: AcceptDate ist identisch zum Signatur-Datum für Entlassrezepte
  Im E-Rezept entspricht das Task-AcceptDate dem Datum der QES-Erstellung, wenn weder Entlassmanagement-Kennzeichen "04" oder "14" gesetzt wurde.

    Wenn der Arzt Dr. Schraßer folgendes E-Rezept an die Versicherte Sina Hüllmann verschreibt:
      | KBV_Statuskennzeichen |
      | <Kennzeichen>         |
    Dann hat die Versicherte Sina Hüllmann hat ein letztes Rezept mit Task.AcceptDate = Bundle.signature.when vorliegen

    Beispiele:
      | Kennzeichen |
      | 04          |
      | 14          |
