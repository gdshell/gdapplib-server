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
	CONSTRAINT archive_pk PRIMARY KEY (id),
	CONSTRAINT archive_fk FOREIGN KEY (app_id) REFERENCES app_mgr.app(id) ON DELETE CASCADE
);

-- app_mgr.user definition

-- Drop table

-- DROP TABLE app_mgr.user;

CREATE TABLE app_mgr.user (
	id varchar NOT NULL,
	username varchar NOT NULL,
	email varchar NOT NULL,
	"password" varchar NOT NULL,
	registered_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	email_verified boolean NOT NULL DEFAULT false,
	CONSTRAINT user_pkey PRIMARY KEY (id)
);

-- app_mgr.refresh_token definition

-- Drop table

-- DROP TABLE app_mgr.refresh_token;

CREATE TABLE app_mgr.refresh_token (
	id serial4 NOT NULL,
	created_at int4 NOT NULL DEFAULT floor(date_part('epoch'::text, now())),
	token varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT refresh_token_pkey PRIMARY KEY (id),
	CONSTRAINT refresh_token_fk FOREIGN KEY (user_id) REFERENCES app_mgr."user"(id) ON DELETE CASCADE
);

-- app_mgr."role" definition

-- Drop table

-- DROP TABLE app_mgr."role";

CREATE TABLE app_mgr."role" (
	id serial4 NOT NULL,
	"name" varchar(255) NOT NULL,
	CONSTRAINT role_pkey PRIMARY KEY (id)
);

-- app_mgr.user_role definition

-- Drop table

-- DROP TABLE app_mgr.user_role;

CREATE TABLE app_mgr.user_role (
	role_id int4 NOT NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id),
	CONSTRAINT role_id_fkey FOREIGN KEY (role_id) REFERENCES app_mgr."role"(id) ON DELETE CASCADE,
	CONSTRAINT user_id_fkey FOREIGN KEY (user_id) REFERENCES app_mgr."user"(id) ON DELETE CASCADE
);