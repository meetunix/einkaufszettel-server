# Changelog

All notable changes to this project will be documented in this file.

## [0.4.0] - 2024-01-12

### changed

- changing schema for an einkaufszettel (breaking change)
- bumped some dependencies to newer versions


## [0.3.1] - 2023-06-04

### changed

- bumped some dependencies to newer versions

## [0.3.0] - 2022-10-30

### changed

- H2 is the only supported database, postgresql no longer supported
- log4j to 2.19.0, json-validator to 1.0.73 and H2 to 2.1.214

## [0.2.6] - 2022-07-30

### changed

- erased error and conflict messages

### fixed

- transparent serialization through jersey
- json-validation using jersey interceptors

## [0.2.5] - 2022-07-25

### Changed

- code cleanup
- precalculated regexes
- bumped Jersey to 2.36 and log4j to 2.18

### fixed

- tests using own server configuration with in memory database

## [0.2.4] - 2022-01-11

### Changed

- new and faster JSON validator
- bumped Jersey to 2.35, H2 to Version 2 and log4j to 2.17.1

### fixed

- slf4j misconfiguration fixed

## [0.2.3] - 2021-12-13

### Fixed

- log4j vulnerability CVE-2021-45105
- log4j vulnerability CVE-2021-45046
- log4j vulnerability CVE-2021-44228

## [0.2.2] - 2021-12-13

### Fixed

- log4j vulnerability CVE-2021-44228

## [0.2.1] - 2021-05-16

### Fixed

- performance issues with big databases fixed

## [0.2.0] - 2020-11-12

### Added

- H2 database may be used with einkaufszettel-server

### Fixed

- some configuration validation was implemented

## [0.1.0] - 2020-11-10

### Added

- compressed payload for receiving and sending (shrinks an Einkaufszettel up to 70%)
    - if a request header contains "Accept-Encoding: gzip", the response body will be gzip
      encoded. (for GET-requests)

    - If a request header contains "Content-Encoding: gzip" einkaufszettel-server assumes that the
      message body is gzip encoded.


- starting from systemd is now possible

- from now on a single configuration file is used

- the path to the configuration file can be passed by cli argument `-c PATH`

## [0.1.0-alpha] - 2020-11-01

### Added

- Test for benchmarking the application

### Fixed

- replaced a database trigger with a daily executed thread for cleaning up the database

## [0.0.1-alpha] - 2020-10-18

initial version
