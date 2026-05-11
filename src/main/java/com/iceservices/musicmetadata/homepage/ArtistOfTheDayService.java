package com.iceservices.musicmetadata.homepage;

import com.iceservices.musicmetadata.artist.Artist;
import com.iceservices.musicmetadata.artist.ArtistRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArtistOfTheDayService {

	private static final LocalDate EPOCH_DATE = LocalDate.of(2024, 1, 1);
	private static final Sort ROTATION_ORDER = Sort.by(
			Sort.Order.asc("createdAt"),
			Sort.Order.asc("id"));

	private final ArtistRepository artistRepository;
	private final Clock clock;

	public ArtistOfTheDayService(ArtistRepository artistRepository, Clock clock) {
		this.artistRepository = artistRepository;
		this.clock = clock;
	}

	public Artist getArtistOfTheDay() {
		long artistCount = artistRepository.count();
		if (artistCount == 0) {
			throw new ArtistOfTheDayNotFoundException();
		}

		LocalDate todayUtc = LocalDate.now(clock.withZone(ZoneOffset.UTC));
		long daysSinceEpoch = ChronoUnit.DAYS.between(EPOCH_DATE, todayUtc);
		long selectedIndex = Math.floorMod(daysSinceEpoch, artistCount);

		return artistRepository.findAll(PageRequest.of(Math.toIntExact(selectedIndex), 1, ROTATION_ORDER))
				.stream()
				.findFirst()
				.orElseThrow(ArtistOfTheDayNotFoundException::new);
	}
}
