package com.log.processor.events;

import java.nio.file.Path;

public class EndOfFile {
	
	private final Path filePath;	

	public EndOfFile(Path filePath) {
		this.filePath = filePath;
	}

	public Path getFilePath() {
		return filePath;
	}
}