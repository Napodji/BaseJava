DROP TABLE IF EXISTS contact;
DROP TABLE IF EXISTS resume;
DROP TABLE IF EXISTS section;

CREATE TABLE resume (
  uuid      UUID PRIMARY KEY,
  full_name TEXT NOT NULL
);

CREATE TABLE contact (
  id          SERIAL,
  resume_uuid UUID NOT NULL REFERENCES resume (uuid) ON DELETE CASCADE,
  type        TEXT NOT NULL,
  value       TEXT NOT NULL
);

CREATE UNIQUE INDEX contact_uuid_type_index
  ON contact (resume_uuid, type);

CREATE TABLE section (
  id          SERIAL,
  resume_uuid UUID NOT NULL REFERENCES resume (uuid) ON DELETE CASCADE,
  type        TEXT NOT NULL,
  content     TEXT NOT NULL
);

CREATE UNIQUE INDEX section_uuid_type_index
  ON section (resume_uuid, type);