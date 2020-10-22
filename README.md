# Einkaufszettel Server

[english version](README_EN.md)

**Einkaufszettel Server befindet sich noch in Entwicklung. Die Dokumentation wird fortlaufend
erweitert.**

*einkaufszettel-Server* ist die Serverkomponente für eine verteilte Anwendung zum anonymen teilen
von Einkaufszetteln. Um einen Einkaufszettel zu teilen ist keine Anmeldung nötig. Entweder man
betreibt seinen eigenen *einkaufszettel-server* um mithilfe der [EZApp (ToDo)]
Einkaufszettel zu teilen, oder man verwendet den öffentlichen einkaufszettel-server unter
[https://ez.nachtsieb.de (TODO)].

[API-Dokumentation](openapi.yaml)

[Einkaufszettel-Spezifikation](https://nachtsieb.de/ez-schema.json)

[Interaktive API-Dokumentation (Swagger)](https://nachtsieb.de/ez-swagger) (TODO)

## Voraussetzungen

*einkaufszettel-server* ist in java implementiert und setzt eine funktionierende PostgreSQL
installation voraus. Außerdem wird Apache Maven zum übersetzen und testen der Anwendung benötigt.

**verwendete Bibliotheken**

* JAX-RS (Jersey)
* JDBC
* jsaon-validator
* HikariCP
* log4j
* junit
* Jackson


## Installation

    mvn clean compile package

### Datenbank vorbereiten

    HOW-TO initialise databse (TODO)
    

## Konfiguration

Die Konfiguration ist in zwei Teile aufgeteilt:

1. Konfiguration des servers: `/etc/ez-server/server.propoerties`

    BASE_URI=http://HOSTNAME:PORT/r0/
    LOG_LEVEL=LEVEL

Der Server verwendet die Adresse und den port aus `BASE_URI` um auf eingehende Anfragen zu
lauschen.

Das `LOG_LEVEL` kann `WARN`, `INFO` oder `DEBUG` sein.  


2. Konfiguration der Datenbankverbindung: `/etc/ez-server/db.propoerties`

    jdbcUrl=jdbc:postgresql://HOSTNAME:PORT/DB-NAME
    dataSource.user=DB-USER
    dataSource.password=PASSWORD
    dataSource.cachePrepStmts=true
    dataSource.prepStmtCacheSize=250
    dataSource.prepStmtCacheSqlLimit=2048


### Konfiguration testen

    mvn clean compile test


## Benutzen

Starten des Servers: 

    java -jar target/einkaufszettelServer-0.0.1-alpha-jar-with-dependencies.jar

### starten über systemd

## API

Die Dokumentation der einkaufszettel API steht als OpenAPI 3 - Spezifikation zur Verfügung: [API-Dokumentation](openapi.yaml)

Die Spezifikation eines Einkaufszettels is als [JSON Schema](https://json-schema.org/) draft
version 4 verfasst: [Einkaufszettel-Spezifikation](https://nachtsieb.de/ez-schema.json).
