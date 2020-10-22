insert into einkaufszettel (eid, created, modified, name, version )
values
('a0eebc999c0b4ef8bb6d6bb9bd380a11' ,     '1999-01-08 04:05:06', '1999-01-08 04:05:06', 'erster Zettel', 1 ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',  '2019-12-24 23:06:00', '2019-12-24 23:06:00', 'zweiter Zettel', 2 ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33',  '2016-10-01 04:05:06', '2016-10-01 04:05:06', 'dritter Zettel', 1 );



insert into items (eid, cid, item_name )
values
('a0eebc999c0b4ef8bb6d6bb9bd380a11',         1, 'gurken' ),
('a0eebc999c0b4ef8bb6d6bb9bd380a11',         1, 'tomaten' ),
('a0eebc999c0b4ef8bb6d6bb9bd380a11',         1, 'oliven' ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',     1, 'gurken' ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',     1, 'tomaten' ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',     2, 'bananen' ),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33',     5, 'Klopapier' );


insert into items (eid, item_name)
values
('A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A33', 'Batterien');
