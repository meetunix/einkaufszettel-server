# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
This project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [unreleased] - 2020-11-10

### Added

- compressed payload for receiving and sending (shrinks a Einkaufszettel up to 70%)
    - if a request header contains "Accept-Encoding: gzip", the response body will be gzip encoded. (for GET-requests)

    - If a request header contains "Content-Encoding: gzip" einkaufszettel-server assumes that the message body is gzip encoded.
    

- starting from systemd is now possible

- from now on a single configuration file is used

- the path to the configuration file can be passed by cli arguemnt `-c PATH` 



## [0.1.0-alpha] - 2020-11-01

### Added

- Test for benchmarking the application

### Fixed

- replaced a database trigger with a daily executed thread for cleaning up the database



## [0.0.1-alpha] - 2020-10-18

initial version
