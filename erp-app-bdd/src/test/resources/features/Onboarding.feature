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

@iosNightly
@onboarding
Funktionalität: Onboarding
  # Beim ersten Start der E-Rezept-App wird dem User ein Onboarding angezeigt. Alle Schritte des Onboarding müssen vom User durchlaufen werden.
  # Erst nach Bestätigung der Nutzungs- und Datenschutzbedingungen wird das Onboarding - auch beim erneuten Start der App - nicht mehr angezeigt.

#  Grundlage:
#    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet

#  Epics:
  @Epic:Tutorial_und_Onboarding
#  ERA-278 Tutorial und Onboarding
  @Epic:Mobile_Analytics
#  ERA-953 Mobile Analytics

#  Stories:
#  ERA-277 Tutorial
#  ERA-492 Tutorial (iOS)
#  ERA-771 Onboarding im Fullscreen und neuer Content (iOS)
#  ERA-1178 Opt-in zu Analytics in Onboarding (iOS)
#  ERA-1195 Opt-in zu Analytics in Onboarding (Android)

  Szenario:
    Angenommen die GKV Versicherte Alice überspringt das Onboarding
    Dann sieht der User den Mainscreen

  Szenario:
    Angenommen die GKV Versicherte Alice öffnet das Onboarding
    Dann kann der User das Onboarding erfolgreich durchlaufen
    Und der User sieht den Mainscreen

  Szenario:
    Angenommen die GKV Versicherte Alice öffnet das Onboarding
    Dann kann der User jede Seite des Onboarding erreichen

  Szenario:
    Angenommen die GKV Versicherte Alice öffnet das Onboarding
    Dann kann der User die Nutzungsbedingungen und Datenschutzbestimmungen lesen

  Szenario:
    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und die Versicherte schließt und öffnet die App
    Dann wird das Onboarding nicht wieder angezeigt

  Szenario:
    Angenommen die GKV Versicherte Alice öffnet das Onboarding
    Dann kann der User kein schwaches Passwort verwenden


  ##############################################################################################################################
  Szenario: Onboarding wird beim 1. App-Start angezeigt
    Dann der User sieht den Onboarding Screen 1

  Szenario: Onboarding wird nach App-Neustart weiterhin angezeigt, solange es noch nicht beendet wurde
    Angenommen der User sieht das Onboarding
    Und der User schliesst die App
    Wenn ich starte die Anwendung
    Und der User sieht das Onboarding

  Szenario: Onboarding-Navigation via Swipe
    Angenommen der User sieht den Onboarding Screen 1
    Wenn der User navigiert via 'Swipe nach rechts'
    Dann der User sieht den Onboarding Screen 2
    Wenn der User navigiert via 'Swipe nach rechts'
    Dann der User sieht den Onboarding Screen 3
    Wenn der User navigiert via 'Swipe nach links'
    Dann der User sieht den Onboarding Screen 2
    Wenn der User navigiert via 'Swipe nach links'
    Dann der User sieht den Onboarding Screen 1
#   TODO: add further steps

  Szenariogrundriss: Onboarding Screen <Screen> wird angezeigt
    Angenommen der User sieht das Onboarding
    Wenn der User navigiert zu Onboarding Screen <Screen>
    Dann der User sieht den Onboarding Screen <Screen>

    Beispiele:
      | Screen |
      | 2      |
      | 3      |
      | 4      |

  Szenario: Onboarding Screen 4 >>> Checkboxen und Bestätigen-Button sind initial deaktiviert
    Angenommen der User sieht das Onboarding
    Wenn der User navigiert zu Onboarding Screen 4
    Dann der User sieht die Checkbox "Nutzungsbedingungen akzeptieren" deaktiviert
    Und der User sieht die Checkbox "Datenschutzbedingungen akzeptieren" deaktiviert
    Und der User sieht den Bestätigen Button deaktiviert

  Szenariogrundriss: Onboarding Screen 4 >>> Bestätigen-Button wird erst aktiviert nachdem BEIDE Checkboxen aktiviert wurden
    Angenommen der User sieht das Onboarding
    Wenn der User navigiert zu Onboarding Screen 4
    Und der User aktiviert die Checkbox "<Checkbox>"
    Dann der User sieht den Bestätigen Button deaktiviert

    Beispiele:
      | Checkbox               |
      | Nutzungsbedingungen    |
      | Datenschutzbedingungen |

  Szenario: Onboarding Screen 4 >>> aktivierter Bestätigen-Button lässt den User auf den Mainscreen navigieren
    Angenommen der User sieht das Onboarding
    Und der User navigiert zu Onboarding Screen 4
    Wenn ich die Nutzungsbedingungen akzeptiere
    Und ich die Datenschutzbedingungen akzeptiere
    Und ich das Onboarding beende
    Dann der User sieht den Mainscreen

  Szenario: Onboarding wird nach App-Neustart nicht mehr angezeigt, nachdem es beendet wurde
    Angenommen der User beendet das Onboarding
    Und der User schliesst die App
    Wenn ich starte die Anwendung
    Dann ich sehe nicht das Onboarding Tutorial

  Szenario: Onboarding Screen 4 >>> Zu schwaches Passwort = Weiter-Button inaktiv + Fehlermeldung
    Angenommen der User sieht den Onboarding Screen 1
    Und der User navigiert zu Onboarding Screen 3
    Wenn der User gibt 2x ein zu schwaches Passwort ein
    Dann der User sieht den Weiter Button deaktiviert
    Und der User sieht eine Fehlermeldung "Sicherheitsstufe des gewählten Kennworts nicht ausreichend"

  Szenario: Onboarding Screen 4 >>> Ausreichend starkes Passwort = Weiter-Button aktiv + keine Fehlermeldung
    Angenommen der User sieht den Onboarding Screen 1
    Und der User navigiert zu Onboarding Screen 3
    Wenn der User gibt 2x ein ausreichend starkes Passwort ein
    Dann der User sieht den Weiter Button aktiviert
    Und der User sieht keine Fehlermeldung bzgl. der Passwortstärke

  Szenario: Nutzungsbedingungen werden angezeigt und können geschlossen werden
    Angenommen der User navigiert zu Onboarding Screen 4
    Und der User sieht die Nutzungsbedingungen nicht
    Wenn der User öffnet die Nutzungsbedingungen
    Dann der User sieht die Nutzungsbedingungen
    Wenn der User schließt die Nutzungsbedingungen
    Dann der User sieht die Nutzungsbedingungen nicht

  Szenario: Datenschutzbestimmungen werden angezeigt und können geschlossen werden
    Angenommen der User navigiert zu Onboarding Screen 4
    Und der User sieht die Datenschutzbestimmungen nicht
    Wenn der User öffnet die Datenschutzbestimmungen
    Dann der User sieht die Datenschutzbestimmungen
    Wenn der User schließt die Datenschutzbestimmungen
    Dann der User sieht die Datenschutzbestimmungen nicht
