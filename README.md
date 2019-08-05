# Authentication backend

## Prerequisites
- raspberry pi
- raspberry pi camera module
- java 8

## Building
```
chmod a+x gradlew
./gradlew shadowJar
```

## Deploying
- copy `build/libs/qr-scanner-1.0-all.jar` to the raspberry pi
- run `java -jar qr-scanner-1.0-all.jar` on the raspberry pi
