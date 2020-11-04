<h1 align="center">einkaufszettel-server</h1>

<p align="center">
<a href="https://github.com/corona-warn-app/cwa-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>


<p align="center">
<a href="#Voraussetzungen">Voraussetzungen</a> • 
<a href="#Konfiguration">Konfiguration</a> • 
<a href="#API">API</a> • 
<a href="#Changelog">Changelog</a> 
</p>

[english version](README_EN.md)

* **Einkaufszettel Server befindet sich noch in Entwicklung.**
* **Die Dokumentation wird fortlaufend erweitert.**

**einkaufszettel-Server** ist die Serverkomponente für eine verteilte Anwendung zum anonymen Teilen
von Einkaufszetteln. Um einen Einkaufszettel zu teilen ist keine Anmeldung nötig. Entweder man
betreibt seinen eigenen *einkaufszettel-server* um mithilfe der [EZApp (ToDo)]
Einkaufszettel zu teilen, oder man verwendet den öffentlichen einkaufszettel-server unter
[https://ez.nachtsieb.de (TODO)].


## Voraussetzungen

**einkaufszettel-server** ist in java implementiert und setzt eine funktionierende PostgreSQL
Installation voraus. Außerdem wird Apache Maven zum Übersetzen und Testen der Anwendung benötigt.

**verwendete Bibliotheken**

* JAX-RS (Jersey)
* JDBC
* jsaon-validator
* HikariCP
* log4j
* junit
* Jackson


## Übersetzen und Testen

    mvn clean compile package

### Datenbank vorbereiten

In diesem Abschnitt wird schematisch gezeigt, wie auf PostgreSQL-Server unter GNU/Linux ein
Datenbanknutzer und eine entsprechende Datenbank für den **einkaufszettel-server** erstellt wird. 

#### 1. Datenbank und Datenbanknutzer erstellen

1. Nutzer mit Passwort erstellen:

    $ sudo -u postgres createuser -P ezuser
    Enter password for new role:
    Enter it again:

2. Datenbank erstellen und Eigentümer setzen:

    $ sudo -u postgres createdb -O ezuser ezdatabase


## Konfiguration

Die Konfiguration ist in zwei Teile aufgeteilt:

1. Konfiguration des servers: `/etc/ez-server/server.propoerties`

```
BASE_URI=http://HOSTNAME:PORT/r0/
LOG_LEVEL=LEVEL
```

Der Server verwendet die Adresse und den Port aus `BASE_URI` um auf eingehende Anfragen zu
lauschen.

Das `LOG_LEVEL` kann `WARN`, `INFO` oder `DEBUG` sein.  


2. Konfiguration der Datenbankverbindung: `/etc/ez-server/db.propoerties`

```
jdbcUrl=jdbc:postgresql://HOSTNAME:PORT/DB-NAME
dataSource.user=DB-USER
dataSource.password=PASSWORD
dataSource.cachePrepStmts=true
dataSource.prepStmtCacheSize=250
dataSource.prepStmtCacheSqlLimit=2048
```


### Konfiguration testen

    mvn clean compile test


## Benutzen

Starten des Servers: 

    java -jar target/einkaufszettelServer-0.1.0-alpha-jar-with-dependencies.jar

### starten über systemd

## API

Die Dokumentation der einkaufszettel API steht als OpenAPI 3 - Spezifikation zur Verfügung: [API-Dokumentation](openapi.yaml)

Die Spezifikation eines Einkaufszettels is als [JSON Schema](https://json-schema.org/) draft
version 4 verfasst: [Einkaufszettel-Spezifikation](https://nachtsieb.de/ez-schema.json).

[Interaktive API-Dokumentation (Swagger)](https://nachtsieb.de/ez-swagger) (TODO)


## Changelog

[CHANGELOG](CHANGELOG.md)