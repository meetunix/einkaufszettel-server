create table if not exists einkaufszettel (
    eid             uuid        NOT NULL,
    created         timestamp   NOT NULL,
    modified        timestamp   NOT NULL,
    version         integer     NOT NULL,
    data            json        NOT NULL
);

create table if not exists ez_schema_version (
    update_time     timestamp   NOT NULL,
    schema_version  int         NOT NULL
);
