
# Build

REST-Server mit allen Abhängigkeiten bauen
```shell
mvn clean package
```

Docker-Image für die lokale Ausführung bauen
```shell
mvn docker:build
```

Docker-Image für die Ausführung gegen TU-Umgebung bauen
```shell
mvn docker:build -Ddeployconf=ibm_tu_config.yaml
```

Run Docker-Container 
```shell
docker run -it -p9095:9095 --network=generated_default primsys-rest:latest
```
_Note:_ Das Netzwerk `generated_default` ist das Netzwerk, indem der FD und IDP laufen