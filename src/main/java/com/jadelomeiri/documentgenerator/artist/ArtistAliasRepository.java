package com.jadelomeiri.documentgenerator.artist;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistAliasRepository extends JpaRepository<ArtistAlias, UUID> {

	List<ArtistAlias> findByArtistIdOrderByAliasNameAsc(UUID artistId);

	boolean existsByArtistIdAndAliasNameIgnoreCase(UUID artistId, String aliasName);
}
