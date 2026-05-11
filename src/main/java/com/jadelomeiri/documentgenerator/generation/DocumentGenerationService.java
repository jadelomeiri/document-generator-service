package com.jadelomeiri.documentgenerator.generation;

import com.jadelomeiri.documentgenerator.document.GeneratedDocumentDraft;
import com.jadelomeiri.documentgenerator.template.DocumentFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class DocumentGenerationService {

	public GeneratedDocumentDraft generate(DocumentGenerationRequest request) {
		String storageReference = "demo://generated-documents/" + request.getId();
		String checksum = checksumFor(request);
		String contentType = contentTypeFor(request.getTemplateVersion().getFormat());
		return new GeneratedDocumentDraft(contentType, checksum, storageReference);
	}

	private String checksumFor(DocumentGenerationRequest request) {
		String checksumSource = request.getId()
				+ "|" + request.getTemplateVersion().getId()
				+ "|" + request.getCustomerReference()
				+ "|" + request.getInputPayloadJson();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(checksumSource.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 checksum algorithm is not available", ex);
		}
	}

	private String contentTypeFor(DocumentFormat format) {
		return switch (format) {
			case PDF -> "application/pdf";
			case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		};
	}
}
