-- DROP SCHEMA app_mgr;

CREATE SCHEMA IF NOT EXISTS app_mgr AUTHORIZATION postgres;
SET search_path TO app_mgr,public;

-- app_mgr.app definition

-- Drop table

-- DROP TABLE app_mgr.app;

CREATE TABLE app_mgr.app (
	id serial4 NOT NULL,
	"name" varchar NOT NULL,
	developer varchar NOT NULL,
	registered_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	updated_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	published boolean NOT NULL DEFAULT false,
    CONSTRAINT app_pkey PRIMARY KEY (id)
);

-- app_mgr.archive definition

-- Drop table

-- DROP TABLE app_mgr.archive;

CREATE TABLE app_mgr.archive (
	id varchar NOT NULL,
	app_id int4 NOT NULL,
	"version" varchar NOT NULL,
	archive varchar NOT NULL,
	"size" int8 NOT NULL,
	hash varchar NOT NULL,
	chunks int2 NOT NULL,
	created_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	updated_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	completed boolean NOT NULL DEFAULT false,
	CONSTRAINT archive_pk PRIMARY KEY (id)
);

-- app_mgr.archive foreign keys

ALTER TABLE app_mgr.archive ADD CONSTRAINT archive_fk FOREIGN KEY (app_id) REFERENCES app_mgr.app(id) ON DELETE CASCADE;