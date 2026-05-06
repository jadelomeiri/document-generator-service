package com.iceservices.musicmetadata.common.error;

import com.iceservices.musicmetadata.artist.ArtistNotFoundException;
import com.iceservices.musicmetadata.artist.DuplicateArtistAliasException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://music-metadata-service/errors/invalid-path-parameter"));
		problem.setTitle("Invalid path parameter");
		problem.setDetail("Parameter '" + ex.getName() + "' must be a valid UUID.");
		problem.setProperty("parameter", ex.getName());
		problem.setProperty("invalidValue", ex.getValue());
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://music-metadata-service/errors/validation-failed"));
		problem.setTitle("Validation failed");
		problem.setDetail("Request validation failed.");

		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
				errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		problem.setProperty("errors", errors);

		return ResponseEntity.badRequest().body(problem);
	}
}
