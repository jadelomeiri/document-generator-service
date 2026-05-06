package com.iceservices.musicmetadata;

import org.springframework.boot.SpringApplication;

public class TestMusicMetadataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(MusicMetadataServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
