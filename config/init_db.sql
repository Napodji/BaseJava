DROP TABLE IF EXISTS organization_position;
DROP TABLE IF EXISTS organization;
DROP TABLE IF EXISTS contact;
DROP TABLE IF EXISTS section;
DROP TABLE IF EXISTS resume;

CREATE TABLE resume
(
    uuid      UUID PRIMARY KEY,
    full_name TEXT NOT NULL
);

CREATE TABLE contact
(
    id          SERIAL,
    resume_uuid UUID NOT NULL REFERENCES resume (uuid) ON DELETE CASCADE,
    type        TEXT NOT NULL,
    value       TEXT NOT NULL
);

CREATE UNIQUE INDEX contact_uuid_type_index
    ON contact (resume_uuid, type);

CREATE TABLE section
(
    id          SERIAL,
    resume_uuid UUID NOT NULL REFERENCES resume (uuid) ON DELETE CASCADE,
    type        TEXT NOT NULL,
    content     TEXT NOT NULL
);

CREATE UNIQUE INDEX section_uuid_type_index
    ON section (resume_uuid, type);

CREATE TABLE organization
(
    id           SERIAL PRIMARY KEY,
    resume_uuid  UUID NOT NULL REFERENCES resume (uuid) ON DELETE CASCADE,
    section_type TEXT NOT NULL,
    name         TEXT NOT NULL,
    url          TEXT
);

CREATE TABLE organization_position
(
    id              SERIAL PRIMARY KEY,
    organization_id INT  NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    title           TEXT NOT NULL,
    description     TEXT
);