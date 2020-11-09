<h1 align="center">einkaufszettel-server</h1>

<p align="center">
<a href="https://github.com/corona-warn-app/cwa-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>   
<a href="https://ez.nachtsieb.de/swagger" title="swagger-ui"><img src="https://img.shields.io/swagger/valid/3.0?specUrl=https%3A%2F%2Fraw.githubusercontent.com%2Fmeetunix%2Feinkaufszettel-server%2Fmain%2Fopenapi.yaml"></a>
</p>


<p align="center">
<a href="#Voraussetzungen">Voraussetzungen</a> • 
<a href="#Konfiguration">Konfiguration</a> • 
<a href="#API">API</a> • 
<a href="#Changelog">Changelog</a> 
</p>

[english version](README_EN.md)

* **einkaufszettel-server befindet sich noch in Entwicklung.**
* **Die Dokumentation wird fortlaufend erweitert.**

**einkaufszettel-Server** ist die Serverkomponente für eine verteilte Anwendung zum anonymen Teilen
von Einkaufszetteln. Um einen Einkaufszettel zu teilen ist keine Anmeldung nötig. Entweder man
betreibt seinen eigenen *einkaufszettel-server* um mithilfe der [EZApp (ToDo)]
Einkaufszettel zu teilen, oder man verwendet den öffentlichen einkaufszettel-server unter
[https://ez.nachtsieb.de (TODO)].


## Voraussetzungen

1. Java Runtime Environment: Version 8 oder höher
2. PostgeSQL
3. Apache Maven: Übersetzen, Testen und zum Erstellen des JAR-Paketes 

## Übersetzen und Testen

    mvn clean compile package

### Datenbank vorbereiten

In diesem Abschnitt wird schematisch gezeigt, wie auf einem PostgreSQL-Server unter GNU/Linux einn
Datenbanknutzer und eine entsprechende Datenbank für den **einkaufszettel-server** erstellt wird. 

1. Nutzer mit Passwort erstellen:

```
$ sudo -u postgres createuser -P ezuser
Enter password for new role:
Enter it again:
```

2. Datenbank erstellen und Eigentümer setzen:

```
$ sudo -u postgres createdb -O ezuser ezdatabase
```

## Konfiguration

Die Konfiguration ist in zwei Dateien aufgeteilt:

1. Konfiguration des servers **server.properties**: `/etc/ez-server/server.propoerties`

```
BASE_URI=http://HOSTNAME:PORT/r0/
LOG_LEVEL=LEVEL
```

Der Server verwendet die Adresse und den Port aus `BASE_URI` um auf eingehende Anfragen zu
lauschen.

Das `LOG_LEVEL` kann `WARN`, `INFO` oder `DEBUG` sein.  


2. Konfiguration der Datenbankverbindung **db.properties**: `/etc/ez-server/db.propoerties`

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

    java -jar target/einkaufszettelServer-[VERSION]-jar-with-dependencies.jar

### starten über systemd

Da das JAR-Paket von **einkaufszettel-server** einen eigenen Webserver enthält kann man
*systemd* zum Verwalten der Anwendung (start, stop, Nutzerkontext) verwenden. Es wird empfohlen
**einkaufszettel-server** mit einem extra dafür angelegtem Nutzerkonto zu betreiben. 
    
1. Neue Systemgruppe/-nutzer unter GNU/Linux anlegen

    sudo groupadd -r ezserver
    sudo useradd -r -s /bin/false -g ezserver ezserver

2. Arbeitsverzeichnis für *systemd* anlegen und Berechtigungen setzen

In diesem Verzeichnis muss das JAR-Paket liegen.

    sudo mkdir /opt/einkaufszettel-server
    sudo chown ezserver: /opt/einkaufszettel-server

3. Systemd unit-file `/etc/systemd/system/einkaufszettel-server.service` erstellen


```
[Unit]
Description=Manage einkaufszettel-server

[Service]
WorkingDirectory=/opt/einkaufszettel-server
ExecStart=/bin/java -jar myapp.jar
User=ezserver
Type=simple
Restart=on-failure
RestartSec=10
KillSignal=SIGINT

[Install]
WantedBy=multi-user.target
```
    

4. Ausführung bei Systemstart und Start

    sudo systemctl dameon-reload
    sudo systemctl start einkufszettel-server
    sudo systemctl enable einkufszettel-server


    
## API

[Interaktive API-Dokumentation (Swagger)](https://ez.nachtsieb.de/swagger)

Die Dokumentation der einkaufszettel API steht als OpenAPIv3-Spezifikation zur Verfügung: [API-Dokumentation](openapi.yaml)

Die Spezifikation eines Einkaufszettels is als [JSON Schema](https://json-schema.org/) draft
version 4 verfasst: [Einkaufszettel-Spezifikation](https://nachtsieb.de/docs/ezschema.json).

## Changelog

[CHANGELOG](CHANGELOG.md)

## einige verwendete Bibliotheken

* JAX-RS (Jersey)
* JDBC
* jsaon-validator
* HikariCP
* log4j
* junit
* Jackson
