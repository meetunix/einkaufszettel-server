create table if not exists einkaufszettel (
    eid             uuid        NOT NULL,
    created         timestamp   NOT NULL,   /* 1999-01-08 04:05:06 [-8:00| PST] */
    modified        timestamp   NOT NULL,
    name            text        NOT NULL,
    version         integer     NOT NULL,
    primary key(eid)
);


create table if not exists category (
    cid             uuid        NOT NULL,
    color           text        NOT NULL CHECK(color ~ '^[0-9a-fA-F]{6}$'),
    description     text        NOT NULL,
    primary key(cid)
);


create table if not exists items (
    iid             uuid        NOT NULL,
    eid             uuid        NOT NULL,
    cid             uuid        NOT NULL,
    item_name       text        NOT NULL,
    ordinal         integer     NOT NULL DEFAULT 1,     /* new domain */
    amount          integer     NOT NULL DEFAULT 1,
    size            real        NOT NULL DEFAULT 1,
    unit            text        NOT NULL DEFAULT 'Stück',  /*new domain */
    primary key(iid),
    foreign key(eid) references einkaufszettel(eid),
    foreign key(cid) references category(cid)
);

create table if not exists ez_cleanup (
    category_cleanup_time   timestamp   NOT NULL
);

create table if not exists ez_schmea_version (
    update_time     timestamp   NOT NULL,
    schema_version  int         NOT NULL
);


create index if not exists idx_items_eid ON items (eid);
create index if not exists idx_items_cid ON items (cid);

/*
CREATE OR REPLACE FUNCTION delete_orphaned_categories() RETURNS TRIGGER AS
    $$
    BEGIN
        DELETE FROM category WHERE cid IN (SELECT cid FROM category EXCEPT (SELECT cid FROM items));
        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;
    
DROP TRIGGER IF EXISTS delete_orphaned_categories_on_ez_delete ON einkaufszettel;
    
CREATE TRIGGER delete_orphaned_categories_on_ez_delete
    AFTER DELETE on einkaufszettel
    FOR EACH STATEMENT
    EXECUTE FUNCTION delete_orphaned_categories();
*/
