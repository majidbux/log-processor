package com.log.processor.events;

import java.nio.file.Path;

public class StartOfFile {
	
	private final Path filePath;
	
	public StartOfFile(Path filePath) {
		this.filePath = filePath;
	}

	public Path getFilePath() {
		return filePath;
	}
}