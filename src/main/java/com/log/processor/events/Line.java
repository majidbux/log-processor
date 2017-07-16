package com.log.processor.events;

import java.nio.file.Path;

public class Line {
	  private final Path filePath;
	  private final String line;

	  public Line( Path filePath, String line ) {
	    this.filePath = filePath;
	    this.line = line;
	  }

	public Path getFilePath() {
		return filePath;
	}

	public String getLine() {
		return line;
	}
}