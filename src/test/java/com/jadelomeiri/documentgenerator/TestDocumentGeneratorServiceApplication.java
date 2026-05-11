package com.jadelomeiri.documentgenerator;

import org.springframework.boot.SpringApplication;

public class TestDocumentGeneratorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(DocumentGeneratorServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
