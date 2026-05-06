package com.iceservices.musicmetadata.artist;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistAliasRepository extends JpaRepository<ArtistAlias, UUID> {

	Page<ArtistAlias> findByArtistId(UUID artistId, Pageable pageable);

	List<ArtistAlias> findByArtistIdOrderByAliasNameAsc(UUID artistId);

	boolean existsByArtistIdAndAliasNameIgnoreCase(UUID artistId, String aliasName);
}
