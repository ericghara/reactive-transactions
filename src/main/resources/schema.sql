;DROP TABLE IF EXISTS link;
;DROP TABLE IF EXISTS b_table;
;DROP TABLE IF EXISTS a_table;

;CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


;CREATE TABLE IF NOT EXISTS a_table (
	id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4()
);

;CREATE TABLE IF NOT EXISTS b_table (
	id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4()
);

;CREATE TABLE IF NOT EXISTS link (
	id_a UUID NOT NULL REFERENCES a_table,
	id_b UUID NOT NULL REFERENCES b_table,
	PRIMARY KEY (id_a, id_b)
);