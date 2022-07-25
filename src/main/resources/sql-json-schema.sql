create table if not exists einkaufszettel (
    eid             uuid        NOT NULL,
    type            int         NOT NULL,   /* 0 -> Einkaufszettel, 1 -> Category */
    created         timestamp   NOT NULL,   /* 1999-01-08 04:05:06 [-8:00| PST] */
    modified        timestamp   NOT NULL,
    version         integer     NOT NULL,
    data            jsonb       NOT NULL
    primary key(eid)
)

create table if not exists ez_cleanup (
    category_cleanup_time   timestamp   NOT NULL
);

create table if not exists ez_schema_version (
    update_time     timestamp   NOT NULL,
    schema_version  int         NOT NULL
);
