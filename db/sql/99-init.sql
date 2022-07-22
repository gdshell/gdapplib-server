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
	registered_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	updated_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	published boolean NOT NULL DEFAULT false,
    CONSTRAINT app_pkey PRIMARY KEY (id)
);