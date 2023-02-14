Commands 4 Docker:
- mvn package -> Erzeugt Fat Jar
- docker build -t serviceprovider:1.0 -f Dockerfile . // serviceprovider:1.0 - entspricht name:version
- docker run -p 9095:9095 -ti serviceprovider:1.0