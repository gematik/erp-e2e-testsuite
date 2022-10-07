# E-Rezept Fachdienst TestSuite

---

Die E-Rezept Fachdienst TestSuite ist in mehrere Teile untergliedert und soll den oberen Teil der Testpyramide abdecken.

![Teststufen](docs/images/testsuite_scopes.png "Teststufen")

## primsys-restserver
Dient primär als Testdatengenerator für die Unterstützung beim **manuellen/explorativen Testen** des E-Rezeptes. 

## primsys-bdd
Ist die Ende-zu-Ende Testsuite und fokussiert überwiegend auf **Akzeptanz-Tests**.
Hierfür werden die Testfälle als Szenarien mit der Beschreibungssprache „Gherkin“ beschrieben. 

Die Testszenarien als Feature-Files unter `primsys-bdd/src/test/resources/features` abgelegt.

## erp-fd-product-test
Ist die _"Produkttestsuite"_ mit dem Fokus auf dem **System-Test** und der Anforderungsabdeckung.

Die Testfälle der _"Produkttestsuite"_ sind als JUnit5-Testfälle unter `erp-fd-product-test/src/integration-test` abgelegt.


# How to

---

## Voraussetzungen:

- Entwicklungsumgebung
  empfohlen: "IntelliJ IDEA" (JetBrains s.r.o.) sowie Cucumber Plugins
- Konnektor mit Terminal
  (_optional kann ein Simulator oder Mock verwendet werden_)
- Kartenterminal
  (_optional kann ein Simulator oder Mock verwendet werden_)
- entsprechende Smartcards mit Zertifikaten _(HBA/SMC-B)_
- Verbindung zur IDP Instanz (URI)
  (_optional kann hier ein Mock verwendet werden_)
- Verbindung zur Fachdienstinstanz (URI)

## Gematik intern

- [Build Pipeline](https://jenkins.prod.ccs.gematik.solutions/job/test_erp/)
- [Testsuite Pipeline](https://jenkins.prod.ccs.gematik.solutions/view/ERX/job/E-Rezept%20MultiBranch%20E2E%20Testsuite/)
- [SonarQube™ Scan](https://sonar.prod.ccs.gematik.solutions/dashboard?branch=Development_1.x&id=de.gematik.test.erezept%3Aerezept-testsuite)


## Build

Das Projekt kann lokal per maven gebaut und getestet werden:

````
  mvn clean package
````

bzw. um Unit-Tests auszuführen

````
  mvn clean test
````

## Testausführung

Neben der Ausführung über die [Testsuite Pipeline](https://jenkins.prod.ccs.gematik.solutions/view/ERX/job/E-Rezept%20MultiBranch%20E2E%20Testsuite/) können die Testsuite auch lokal ausgeführt werden. 
Hier ist allerdings zu beachten, dass das SUT (FD, IDP und eventuell benötigte Konnektoren) erreichbar ist.

Ausführung der E2E Testsuite
````
  mvn -f primsys-bdd/pom.xml clean verify -Dskip.unittests
````

bzw. Ausführung der Produkttestsuite
````
  mvn -f erp-fd-product-test/pom.xml clean verify -Dskip.unittests
````

## Grobarchitektur

![Grobarchitektur](docs/images/overview.png "Architektur")