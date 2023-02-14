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

@Funktionalität=Cummunication
@Anwendungsfall=A_18508
@Anwendungsfall=A_18617
@Anwendungsfall=A_18618
@Anwendungsfall=A_19013
@Impl=done
Funktionalität: Nachrichten zwischen Versicherten und Apotheke austauschen

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und der GKV Versicherte Günther Angermänn hat Zugriff auf seine eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |


  @TCID=ERP_EE_Communication_01
  @Path=happy
  @MainActor=Versicherter
   Szenario: Zuweisung per Nachricht
  Der Versicherte weist das gerade erhaltene E-Rezept per Nachricht einer Apotheke zu.
  Diese kann das Rezept erfolgreich akzeptieren und einlösen.

    Wenn die Versicherte Sina Hüllmann ihr letztes E-Rezept der Apotheke Am Flughafen per Nachricht zuweist
    Und die Apotheke Am Flughafen die letzte Zuweisung per Nachricht von Sina Hüllmann akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Und die Versicherte Sina Hüllmann hat genau 1 Medikament erhalten


  @TCID=ERP_EE_Communication_02
  @Path=happy
  @MainActor=Versicherter
  Szenario: Anfrage zu einem E-Rezept und Antwort der Apotheke
  Der Versicherte stellt zum grad erhaltenen Rezept eine Anfrage an die Apotheke. Diese beantwortet die Anfrage.

    Wenn die Versicherte Sina Hüllmann zu ihrem letzten E-Rezept der Apotheke Am Flughafen eine Anfrage schickt
    Und die Apotheke Am Flughafen die letzte Nachricht von Sina Hüllmann beantwortet
    Dann hat die Versicherte Sina Hüllmann eine Antwort von der Apotheke Am Flughafen erhalten


  @TCID=ERP_EE_Communication_03
  @Path=happy
  @Anwendungsfall=A_18781
  @MainActor=Versicherter
  Szenario: Zuweisung an einen Vertreter
  Der Versicherte weist das ausgestellte Rezept einem Vertreter zu. Dieser kann es erfolgreich in der Apotheke einlösen.

    Wenn die Versicherte Sina Hüllmann ihr letztes E-Rezept per Nachricht an den Vertreter Günther Angermänn schickt
    Dann hat der Vertreter Günther Angermänn die Nachricht mit dem Rezept der Versicherten Sina Hüllmann empfangen
    Und der Vertreter Günther Angermänn sein letztes von Sina Hüllmann zugewiesenes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren
    Und die Apotheke hat genau eine Quittung vorliegen



  @TCID=ERP_EE_Communication_04
  @Path=bad
  @Afo=A_20230
  @MainActor=Versicherter
  Szenario: Zuweisung an einen Vertreter nicht möglich nach dem Einlösen
  Der Versicherte weist das ausgestellte Rezept einem Vertreter zu. Dieser kann es erfolgreich in der Apotheke einlösen.

    Wenn die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann kann die Versicherte Sina Hüllmann ihr letztes E-Rezept nicht per Nachricht an den Vertreter Günther Angermänn schicken


  @TCID=ERP_EE_Communication_05
  @Path=bad
  @Afo=A_20231
  @MainActor=Versicherter
  Szenario: Zuweisung an sich selbst nicht möglich
  Der E-Rezept-Fachdienst muss eine Nachricht mit Empfänger gleich Absender ablehnen

    Dann kann die Versicherte Sina Hüllmann ihr letztes E-Rezept nicht per Nachricht an den Vertreter Sina Hüllmann schicken


  @TCID=ERP_EE_Communication_06
  @Path=happy
  @Afo=A_20513
  @MainActor=Fachdienst
  Szenario: Fachdienst löscht alle Nachrichten zu einem E-Rezept nach dem Einlösen
  Der Testfall überprüft, ob alle Nachrichten, die sich auf ein E-Rezept beziehen, nach dem erfolgreichem Einlösen vom Fachdienst gelöscht wurden.

    Wenn die Versicherte Sina Hüllmann ihr letztes E-Rezept der Apotheke Am Flughafen per Nachricht zuweist
    Und die Apotheke Am Flughafen die letzte Zuweisung per Nachricht von Sina Hüllmann akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann kann die Versicherte Sina Hüllmann nicht mehr die Nachrichten zu ihrem letzten E-Rezept abrufen


  @TCID=ERP_EE_Communication_07
  @Path=happy
  @Anwendungsfall=A_20260
  @Afo=A_20258
  @MainActor=Versicherter
  Szenario: Versicherte löscht alle Nachrichten
    Wenn die Versicherte Sina Hüllmann zu ihrem letzten E-Rezept der Apotheke Am Flughafen eine Anfrage schickt
    Und die Apotheke Am Flughafen die letzte Nachricht von Sina Hüllmann beantwortet
    Und die Versicherte Sina Hüllmann alle ihre versendeten Nachrichten löscht
    Dann kann die Versicherte Sina Hüllmann keine ihrer versendeten Nachrichten mehr abrufen


  @TCID=ERP_EE_Communication_08
  @Path=happy
  @Anwendungsfall=A_20776
  @Afo=A_20258
  @MainActor=Apotheke
  Szenario: Apotheke löscht Nachricht
    Wenn die Versicherte Sina Hüllmann zu ihrem letzten E-Rezept der Apotheke Am Flughafen eine Anfrage schickt
    Und die Apotheke Am Flughafen die letzte Nachricht von Sina Hüllmann beantwortet
    Und die Apotheke Am Flughafen ihre letzte versendete Nachricht löscht
    Dann hat die Versicherte Sina Hüllmann keine Antwort von der Apotheke Am Flughafen für das letzte E-Rezept erhalten


  @TCID=ERP_EE_Communication_09
  @Path=bad
  @Afo=A_19446-01
  @MainActor=Arztpraxis
  Szenario: Arztpraxis darf keine Nachricht versenden
  Der Versicherte schreibt eine Nachricht an eine Arztpraxis "Keine Apotheke". Der Fachdienst muss die Antwort ablehnen,
  weil nur Apotheken und Versicherte Nachrichten versenden dürfen.

    Angenommen die Apotheke Keine Apotheke hat Zugriff auf ihre SMC-B
    Wenn die Versicherte Sina Hüllmann zu ihrem letzten E-Rezept der Apotheke Keine Apotheke eine Anfrage schickt
    Dann kann Keine Apotheke die letzte Nachricht von Sina Hüllmann nicht beantworten, weil sie keine Apotheke ist


  @TCID=ERP_EE_Communication_10
  @Path=happy
  @Anwendungsfall=A_20260
  @Afo=A_20258
  @MainActor=Versicherter
  Szenario: Versicherte löscht eine Nachricht
    Wenn die Versicherte Sina Hüllmann zu ihrem letzten E-Rezept der Apotheke Am Flughafen eine Anfrage schickt
    Und die Versicherte Sina Hüllmann ihre letzte versendete Nachricht löscht
    Dann kann die Apotheke Am Flughafen die letzte Nachricht von Sina Hüllmann nicht abrufen, weil die Nachricht bereits gelöscht wurde
