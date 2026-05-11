package com.jadelomeiri.documentgenerator.track;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, UUID> {

	Page<Track> findByArtistId(UUID artistId, Pageable pageable);

	boolean existsByIsrc(String isrc);
}
