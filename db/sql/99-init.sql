-- DROP SCHEMA app_mgr;

CREATE SCHEMA IF NOT EXISTS app_mgr AUTHORIZATION postgres;
-- app_mgr.app definition

-- Drop table

-- DROP TABLE app_mgr.app;

CREATE TABLE app_mgr.app (
	id serial4 NOT NULL,
	"name" varchar NOT NULL,
	developer varchar NOT NULL,
	archive varchar NOT NULL,
	published_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	updated_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
    CONSTRAINT app_pkey PRIMARY KEY (id)
);