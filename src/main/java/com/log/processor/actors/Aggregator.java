package com.log.processor.actors;

import java.nio.file.Path;

import com.log.processor.events.EndOfFile;
import com.log.processor.events.Line;
import com.log.processor.events.StartOfFile;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
/**
 * Aggregator actor counts the number of words in a file
 * @author majidali
 *
 */
public class Aggregator extends AbstractLoggingActor {

	private final Path filePath;

	private int wordsCount = 0;

	
	public Aggregator(Path filePath) {
		this.filePath = filePath;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(StartOfFile.class, this::onStartOfFile).match(Line.class, this::onLine)
				.match(EndOfFile.class, this::onEndOfFile)
				.matchAny(event -> log().info("received unknown event {}", event)).build();
	}

	private void onStartOfFile(StartOfFile event) {
		if (this.filePath.equals(event.getFilePath())) {
			log().info("Aggregator received StartOfFile event for file : {}", event.getFilePath());
		}
	}

	private void onEndOfFile(EndOfFile event) {
		if (this.filePath.equals(event.getFilePath())) {
			log().info("Aggregator received EndOfFile event for file : {}", event.getFilePath());
			log().info("File " + event.getFilePath() + " has " + wordsCount + " words.");
		}
	}

	private void onLine(Line event) {
		if (this.filePath.equals(event.getFilePath())) {
			log().info("Aggregator received Line event for file : {}", event.getFilePath());
			if (event.getLine() != null) {
				wordsCount = wordsCount + event.getLine().split(" ").length;
			}
		}
	}

	public int getWordsCount() {
		return wordsCount;
	}

	
	public Path getFilePath() {
		return filePath;
	}

	public static Props props(Path filePath) {
		return Props.create(Aggregator.class, filePath);
	}
}
