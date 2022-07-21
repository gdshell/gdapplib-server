-- DROP SCHEMA app_mgr;

CREATE SCHEMA IF NOT EXISTS app_mgr AUTHORIZATION postgres;
-- app_mgr.app definition

-- Drop table

-- DROP TABLE app_mgr.app;

CREATE TABLE app_mgr.app (
	id serial4 NOT NULL,
	"name" varchar NOT NULL,
	developer varchar NOT NULL,
	published_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	updated_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
    CONSTRAINT app_pkey PRIMARY KEY (id)
);

-- app_mgr.app_metadata definition

-- Drop table

-- DROP TABLE app_mgr.app_metadata;

CREATE TABLE app_mgr.app_metadata (
	app_id int4 NOT NULL,
	archive_name varchar NOT NULL,
	archive_format varchar NOT NULL,
	"hash" varchar NOT NULL,
	size int8 NOT NULL,
    CONSTRAINT app_metadata_pkey PRIMARY KEY (app_id),
    CONSTRAINT app_metadata_fkey FOREIGN KEY (app_id) REFERENCES app_mgr.app(id)
);
