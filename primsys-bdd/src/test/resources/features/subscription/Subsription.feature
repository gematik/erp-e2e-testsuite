#
# Copyright (c) 2022 gematik GmbH
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

@Funktionalität=BenachrichtigungApotheke
@Impl=done
Funktionalität: Subscription Service für Apotheken
  Der Subscription Service informiert die Apotheker bei neuen Nachrichten durch die Versicherten.
  Hierfür werden die Ressourcen ErxCommunicationInfoReq und ErxCommunicationDispReq betrachtet.

  Grundlage:
    Angenommen der Arzt Dr. Schraßer hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK

  @TCID=ERP_EE_Subscrp_01
  @Path=happy
  @Afo=A_22367-01
  @Afo=A_22364
  Szenario: Benachrichtigung beim Eingang einer neuen Zuweisung
  Eine Apotheke wird durch den Subscription Service informiert, wenn ein Versicherter ein E-Rezept mit dem FdV an diese Apotheke zuweist.

    Wenn die Apotheke Am Flughafen sich für die Subscription Communication registriert
    Und die Apotheke Am Flughafen sich mit dem Subscription Service verbindet
    Und der Arzt Dr. Schraßer folgendes E-Rezept an die Versicherte Sina Hüllmann verschreibt:
      | Name          | PZN      | Substitution | Verordnungskategorie | Normgröße | Darreichungsform | Dosierung | Menge | Notdiensgebühr | Zahlungsstatus |
      | Schmerzmittel | 12345678 | false        | 00                   | N1        | TAB              | 1-0-0-1   | 3     | false          | 0              |
    Und die Versicherte Sina Hüllmann das letztes ihm zugewiesene E-Rezept herunterlädt
    Und die Versicherte Sina Hüllmann das letztes heruntergeladene E-Rezept der Apotheke Am Flughafen zuweist
    Dann wird die Apotheke Am Flughafen durch den Subscription Service informiert

  @TCID=ERP_EE_Subscrp_02
  @Path=happy
  @Afo=A_22367-01
  @Afo=A_22364
  Szenario: Benachrichtigung beim Eingang einer neuen Anfrage
  Eine Apotheke wird durch den Subscription Service informiert, wenn ein Versicherter für ein E-Rezept mit dem FdV eine Anfrage an diese Apotheke schickt

    Wenn die Apotheke Am Flughafen sich für die Subscription Communication registriert
    Und die Apotheke Am Flughafen sich mit dem Subscription Service verbindet
    Und der Arzt Dr. Schraßer folgendes E-Rezept an den Versicherten Sina Hüllmann verschreibt:
      | Name          | PZN      | Substitution | Verordnungskategorie | Normgröße | Darreichungsform | Dosierung | Menge | Notdiensgebühr | Zahlungsstatus |
      | Schmerzmittel | 12345678 | false        | 00                   | N1        | TAB              | 1-0-0-1   | 3     | false          | 0              |
    Und die Versicherte Sina Hüllmann das letztes ihr zugewiesene E-Rezept herunterlädt
    Und die Versicherte Sina Hüllmann für das letztes heruntergeladene E-Rezept eine Anfrage an die Apotheke Am Flughafen schickt
    Dann wird die Apotheke Am Flughafen durch den Subscription Service informiert