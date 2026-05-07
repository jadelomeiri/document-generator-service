package com.iceservices.musicmetadata.common.error;

import com.iceservices.musicmetadata.artist.ArtistNotFoundException;
import com.iceservices.musicmetadata.artist.DuplicateArtistAliasException;
import com.iceservices.musicmetadata.homepage.ArtistOfTheDayNotFoundException;
import com.iceservices.musicmetadata.track.DuplicateTrackIsrcException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ArtistNotFoundException.class)
	ResponseEntity<ProblemDetail> handleArtistNotFound(ArtistNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://music-metadata-service/errors/artist-not-found"));
		problem.setTitle("Artist not found");
		problem.setDetail("No artist exists with id " + ex.getArtistId() + ".");
		problem.setProperty("artistId", ex.getArtistId());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(DuplicateArtistAliasException.class)
	ResponseEntity<ProblemDetail> handleDuplicateAlias(DuplicateArtistAliasException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setType(URI.create("https://music-metadata-service/errors/duplicate-artist-alias"));
		problem.setTitle("Duplicate artist alias");
		problem.setDetail("Alias '" + ex.getAlias() + "' already exists for this artist.");
		problem.setProperty("artistId", ex.getArtistId());
		problem.setProperty("alias", ex.getAlias());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	@ExceptionHandler(DuplicateTrackIsrcException.class)
	ResponseEntity<ProblemDetail> handleDuplicateTrackIsrc(DuplicateTrackIsrcException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setType(URI.create("https://music-metadata-service/errors/duplicate-track-isrc"));
		problem.setTitle("Duplicate track ISRC");
		problem.setDetail("Track with ISRC '" + ex.getIsrc() + "' already exists.");
		problem.setProperty("isrc", ex.getIsrc());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	@ExceptionHandler(ArtistOfTheDayNotFoundException.class)
	ResponseEntity<ProblemDetail> handleArtistOfTheDayNotFound() {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://music-metadata-service/errors/artist-of-the-day-not-found"));
		problem.setTitle("Artist of the Day unavailable");
		problem.setDetail("Artist of the Day cannot be selected because no canonical artists exist.");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://music-metadata-service/errors/invalid-request-parameter"));

		problem.setProperty("parameter", ex.getName());
		problem.setProperty("invalidValue", ex.getValue());

		if (ex.getRequiredType() == UUID.class) {
			problem.setTitle("Invalid path parameter");
			problem.setDetail("Parameter '" + ex.getName() + "' must be a valid UUID.");
		} else {
			problem.setTitle("Invalid request parameter");
			problem.setDetail("Parameter '" + ex.getName() + "' has an invalid value.");
		}

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = validationProblem();

		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
				errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		problem.setProperty("errors", errors);

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
		ProblemDetail problem = validationProblem();
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getConstraintViolations().forEach(violation ->
				errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	private ProblemDetail validationProblem() {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://music-metadata-service/errors/validation-failed"));
		problem.setTitle("Validation failed");
		problem.setDetail("Request validation failed.");
		return problem;
	}
}
