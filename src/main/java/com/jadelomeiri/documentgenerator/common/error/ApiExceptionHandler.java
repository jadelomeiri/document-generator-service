package com.jadelomeiri.documentgenerator.common.error;

import com.jadelomeiri.documentgenerator.document.GeneratedDocumentNotFoundException;
import com.jadelomeiri.documentgenerator.generation.GenerationRequestNotFoundException;
import com.jadelomeiri.documentgenerator.template.DocumentTemplateNotFoundException;
import com.jadelomeiri.documentgenerator.template.DocumentTemplateVersionNotFoundException;
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

	@ExceptionHandler(DocumentTemplateNotFoundException.class)
	ResponseEntity<ProblemDetail> handleDocumentTemplateNotFound(DocumentTemplateNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://document-generator-service/errors/document-template-not-found"));
		problem.setTitle("Document template not found");
		problem.setDetail("No document template exists with id " + ex.getTemplateId() + ".");
		problem.setProperty("templateId", ex.getTemplateId());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(DocumentTemplateVersionNotFoundException.class)
	ResponseEntity<ProblemDetail> handleDocumentTemplateVersionNotFound(DocumentTemplateVersionNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://document-generator-service/errors/document-template-version-not-found"));
		problem.setTitle("Document template version not found");
		problem.setDetail("No document template version exists with id " + ex.getTemplateVersionId() + ".");
		problem.setProperty("templateVersionId", ex.getTemplateVersionId());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(GenerationRequestNotFoundException.class)
	ResponseEntity<ProblemDetail> handleGenerationRequestNotFound(GenerationRequestNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://document-generator-service/errors/generation-request-not-found"));
		problem.setTitle("Generation request not found");
		problem.setDetail("No document generation request exists with id " + ex.getRequestId() + ".");
		problem.setProperty("requestId", ex.getRequestId());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(GeneratedDocumentNotFoundException.class)
	ResponseEntity<ProblemDetail> handleGeneratedDocumentNotFound(GeneratedDocumentNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setType(URI.create("https://document-generator-service/errors/generated-document-not-found"));
		problem.setTitle("Generated document not found");
		if (ex.getGenerationRequestId() == null) {
			problem.setDetail("No generated document exists with id " + ex.getDocumentId() + ".");
			problem.setProperty("documentId", ex.getDocumentId());
		} else {
			problem.setDetail("No generated document exists for generation request "
					+ ex.getGenerationRequestId() + ".");
			problem.setProperty("generationRequestId", ex.getGenerationRequestId());
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://document-generator-service/errors/invalid-request-parameter"));

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
				errors.putIfAbsent(
						clientFacingParameterName(violation.getPropertyPath().toString()),
						violation.getMessage()));
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	private String clientFacingParameterName(String propertyPath) {
		int lastSeparator = propertyPath.lastIndexOf('.');
		if (lastSeparator == -1) {
			return propertyPath;
		}
		return propertyPath.substring(lastSeparator + 1);
	}

	private ProblemDetail validationProblem() {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setType(URI.create("https://document-generator-service/errors/validation-failed"));
		problem.setTitle("Validation failed");
		problem.setDetail("Request validation failed.");
		return problem;
	}
}
