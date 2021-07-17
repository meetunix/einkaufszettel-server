<h1 align="center">einkaufszettel-server</h1>

<p align="center">
<a href="https://github.com/meetunix/einkaufszettel-server/blob/main/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
<a href="https://ez.nachtsieb.de/swagger" title="swagger-ui"><img src="https://img.shields.io/swagger/valid/3.0?specUrl=https%3A%2F%2Fraw.githubusercontent.com%2Fmeetunix%2Feinkaufszettel-server%2Fmain%2Fopenapi.yaml"></a>
</p>


<p align="center">
<a href="#Voraussetzungen">Voraussetzungen</a> •
<a href="#Konfiguration">Konfiguration</a> •
<a href="#API">API</a> •
<a href="#Changelog">Changelog</a>
</p>

[english version](README_EN.md)

**einkaufszettel-Server** ist die Serverkomponente für eine verteilte Anwendung zum anonymen Teilen
von Einkaufszetteln. Um einen Einkaufszettel zu teilen ist keine Anmeldung nötig. Entweder man
betreibt seinen eigenen *einkaufszettel-server* um mithilfe der [EZApp (ToDo)]
Einkaufszettel zu teilen, oder man verwendet den öffentlichen einkaufszettel-server unter
[https://ez.nachtsieb.de (TODO)]. Der Zugriff erfolgt über ein RESTful-API.


## Voraussetzungen

1. Java Runtime Environment: Version 8 oder höher
2. Apache Maven: Übersetzen, Testen und zum Erstellen des JAR-Paketes

## Übersetzen

```
mvn clean compile package -DskipTests
```

Nach dem Übersetzen befindet sich die JAR-Datei `einkaufszettelServer-[VERSION]-jar-with-dependencies.jar`
im `target`-Verzeichnis.


## Konfiguration

Die Konfigurationsdatei wird beim Start üder die Option `-c` übergeben. Sollte
Option `-c PATH` nicht verwendet werden, wird die Datei `/etc/ez-server/server.properties` verwendet.

```
java -jar einkaufszettelServer-[VERSION]-jar-with-dependencies.jar -c /PATH/server.properties

```

#### Aufbau der Konfigurationsdatei

```
BASE_URI=http://ADDRESS:PORT/r0/
LOG_LEVEL=LEVEL
LOG_PATH=/var/log/
JDBC_URL=jdbc:postgresql://HOSTNAME:PORT/DB-NAME
DATABASE_USERNAME=DB-USER
DATABASE_PASSWORD=PASSWORD
```

Der Server verwendet die Adresse und den Port aus `BASE_URI` um auf eingehende Anfragen zu
lauschen.

Das `LOG_LEVEL` kann `WARN`, `INFO` oder `DEBUG` sein.

Die Datenbankspezifischen Einstellungen werden im nächsten Abschnitt erläutert.

### Datenbank

**einkaufszettel-server** kann entweder mit PostgreSQL oder mit der eingebetteten Datenbank
[H2](http://h2database.com) betrieben werden. H2 ist eine schlanke Datenbank, die ihre Daten
persistent in Dateien speichert oder ausschließlich im Arbeitsspeicher (*in-memory-mode*).
In beiden Fällen kann nur eine Instanz von **einkaufszettel-server** auf
die Daten zugreifen. Im *in-memory-mode* gehen die Daten nach der Beendigung der Applikation
verloren. H2 ist eine alternative zu SQLite.

#### PostgreSQL als Datenbank

In diesem Abschnitt wird schematisch gezeigt, wie auf einem PostgreSQL-Server unter GNU/Linux ein
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

3. Pfad in der Konfigurationsdatei angeben:

Folgender Ausschnitt aus der Konfigurationsdatei zeigt exemplarisch die Verwendung die Verwendung
von POstgreSQL:

```
...
JDBC_URL=jdbc:postgresql://db.example.org:5432/ez-database
DATABASE_USERNAME=ez-user
DATABASE_PASSWORD=top_secret
```

#### H2 als Datenbank

##### H2 mit persistenter Datenspeicherung

In diesem Modus werden die Daten auf dem lokalen System in einigen Dateien abgelegt. Es eignet
sich besonders für kleine Installationen. Die Konfiguration geschieht ausschließlich über die
Variable `JDBC_URL` aus der Konfigurationsdatei.

Im folgenden Beispiel speichert H2 seine Daten in das Verzeichnis `/var/ez-database/` und verwendet
als dateinamenpräfix `db.*`. Dateiname und Nutzer werden bei der automatischen Erstellung ebenfalls
erstellt und sind für den Zugriff auf die Datenbank notwendig.

```
...
JDBC_URL=jdbc:h2:file:/var/ez-database/db
DATABASE_USERNAME=someuser
DATABASE_PASSWORD=somesecret
```

##### H2 als in-memory Datenbank

Der in-memory-Modus eignet sich nur für Benchmarks und zum schnellen Testen. Nutzer und Password
können ignoriert werden, müssen aber in der Konfiguration vorhanden sein.


```
JDBC_URL=jdbc:h2:file:/var/ez-database/db
```

## starten über systemd

Da das JAR-Paket von **einkaufszettel-server** einen eigenen Webserver enthält kann man
*systemd* zum Verwalten der Anwendung (start, stop, Nutzerkontext) verwenden. Es wird empfohlen
**einkaufszettel-server** mit einem extra dafür angelegtem Nutzerkonto zu betreiben.

1. Neue Systemgruppe/-nutzer unter GNU/Linux anlegen

```
$ sudo groupadd -r ezserver
$ sudo useradd -r -s /bin/false -g ezserver ezserver
```


2. Arbeitsverzeichnis für *systemd* anlegen und Berechtigungen setzen

In diesem Verzeichnis muss das JAR-Paket liegen.

```
$ sudo mkdir /opt/einkaufszettel-server
$ sudo chown ezserver: /opt/einkaufszettel-server
```

3. Systemd unit-file `/etc/systemd/system/einkaufszettel-server.service` erstellen


```
[Unit]
Description=Manage einkaufszettel-server

[Service]
WorkingDirectory=/opt/einkaufszettel-server
ExecStart=/bin/java -jar einkaufszettelServer-[VERSION]-jar-with-dependencies.jar
User=ezserver
Type=simple
Restart=on-failure
RestartSec=10
KillSignal=SIGINT

[Install]
WantedBy=multi-user.target
```

4. Ausführung bei Systemstart und Start

```
sudo systemctl dameon-reload
sudo systemctl start einkufszettel-server
sudo systemctl enable einkufszettel-server
```

## API

[Interaktive API-Dokumentation (Swagger)](https://swagger.nachtsieb.de)

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
