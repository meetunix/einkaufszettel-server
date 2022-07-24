<h1 align="center">einkaufszettel-server</h1>

<p align="center">
<a href="https://github.com/meetunix/einkaufszettel-server/blob/main/LICENSE" title="License">
<img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
<a href="https://ez.nachtsieb.de/swagger" title="swagger-ui">
<img src="https://img.shields.io/swagger/valid/3.0?specUrl=https%3A%2F%2Fraw.githubusercontent.com%2Fmeetunix%2Feinkaufszettel-server%2Fmain%2Fopenapi.yaml"></a>
</p>


<p align="center">
<a href="#Changelog">Changelog</a>  •
<a href="https://nachtsieb.de/docs/ezschema.json">Spezifikation (JSON-Schema)</a>  •
<a href="https://swagger.nachtsieb.de">API (Swagger)</a>
</p>

[english version](README_EN.md)

**einkaufszettel-Server** ist die Serverkomponente für eine verteilte Anwendung zum
einfachen und anonymen Teilen von Einkaufszetteln. Eine Anmeldung ist nicht nötig. Ein
Einkaufszettel wird eindeutig über eine UUID identifiziert, über diese kann der Einkaufszettel
von einem einkaufszettel-server geteilt werden. Jeder der die eindeutige ID des Einkaufszettels
kennt, kann ihn lesen, verändern oder löschen. Eine Testinstanz steht unter
`https//:ez.nachtsieb.de` zur Verfügung.

Eine Client-Anwendung in Form einer [PWA](https://en.wikipedia.org/wiki/Progressive_web_application)
befindet sich in Planung.

## Installation

Für ein einfaches Deployment stehen [Docker-Compose-Files](deployment/) bereit. Es existiert
ein deployment, das eine PostgreSQL-Datenbank (*docker-compose-postgresql.yml*), sowie ein
Deployment das eine eingebettete H2-Datenbank verwendet (*docker-compose-h2.yml*).
Beide Datenbanksysteme speichern ihre Datenbanken persistent in
Docker-Volumes.

```
docker-compose -f deployment/docker-compose-h2.yml up
```

Der einkaufszettel-server steht im Anschluss unter URL `http://127.0.0.1:18080/r0` zur Verfügung.

Möchte man die Applikation hingegen ohne Container betreiben, benötigt man mindestens Version 11
des Java Runtime Environments. Eine Anleitung um einkaufszettel-server ohne Docker zu betreiben
befindet sich [hier](doc/configuration_de.md). Dieser Anleitung befasst sich auch mit der
Konfiguration der Datenbank und enthält eine Service-Definition für Systemd.

## Einkaufszettel (Spezifikation)

Die Spezifikation eines Einkaufszettels is als [JSON Schema](https://json-schema.org/) draft
version 4 verfasst: [Einkaufszettel-Spezifikation](https://nachtsieb.de/docs/ezschema.json).

## RAST-API

die interaktive [API-Dokumentation (Swagger)](https://swagger.nachtsieb.de) greift auf die
Testinstanz `https://ez.nachtsieb.de` zu.

Die Dokumentation der einkaufszettel API steht als [OpenAPIv3-Spezifikation](openapi.yaml) bereit.

## Changelog

[CHANGELOG](CHANGELOG.md)

## Verwendete Bibliotheken

* [Eclipse Jersey (JAX-RS)](https://projects.eclipse.org/projects/ee4j.jersey)
* [json-schema-validator](https://github.com/networknt/json-schema-validator)
* [HikariCP](https://github.com/brettwooldridge/HikariCP)
* [log4j2](https://logging.apache.org/log4j/2.x/)
* [Jackson](https://github.com/FasterXML/jackson)
* [JDBC-Postgres](https://jdbc.postgresql.org/)
* [H2](https://h2database.com/html/main.html)

## Geplante Features

* Speicherung der Einkaufszettel als JSONB (postgres) and JSON (H2)
* Persistente Speicherung von Einkaufszettel-Versionen
