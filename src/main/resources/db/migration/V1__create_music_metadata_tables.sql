CREATE TABLE artists (
    id UUID PRIMARY KEY,
    primary_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT artists_primary_name_not_blank CHECK (length(btrim(primary_name)) > 0)
);

CREATE TABLE artist_aliases (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    alias_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT artist_aliases_artist_fk
        FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE,
    CONSTRAINT artist_aliases_alias_name_not_blank CHECK (length(btrim(alias_name)) > 0)
);

CREATE TABLE tracks (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    length_seconds INTEGER NOT NULL,
    isrc VARCHAR(12),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT tracks_artist_fk
        FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE,
    CONSTRAINT tracks_title_not_blank CHECK (length(btrim(title)) > 0),
    CONSTRAINT tracks_length_seconds_positive CHECK (length_seconds > 0),
    CONSTRAINT tracks_isrc_format CHECK (isrc IS NULL OR isrc ~ '^[A-Z]{2}[A-Z0-9]{3}[0-9]{7}$')
);

CREATE INDEX artist_aliases_artist_id_idx ON artist_aliases (artist_id);
CREATE UNIQUE INDEX artist_aliases_artist_id_alias_name_unique_idx ON artist_aliases (artist_id, lower(alias_name));

CREATE INDEX tracks_artist_id_id_idx ON tracks (artist_id, id);
CREATE INDEX tracks_artist_id_title_idx ON tracks (artist_id, title);
CREATE UNIQUE INDEX tracks_isrc_unique_idx ON tracks (isrc) WHERE isrc IS NOT NULL;
