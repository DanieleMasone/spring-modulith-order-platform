-- Spring Modulith 2.1.1 JDBC v2 PostgreSQL schema; Flyway owns initialization.
-- https://github.com/spring-projects/spring-modulith/blob/2.1.1/spring-modulith-events/spring-modulith-events-jdbc/src/main/resources/org/springframework/modulith/events/jdbc/schemas/v2/schema-postgresql.sql
CREATE TABLE event_publication (
    id UUID NOT NULL PRIMARY KEY,
    listener_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    status TEXT,
    completion_attempts INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);

-- Existing notification intents have no recoverable source identifier and remain unchanged.
ALTER TABLE notifications ADD COLUMN source_id UUID;
ALTER TABLE notifications ADD CONSTRAINT notifications_source_type_unique UNIQUE (source_id, type);
