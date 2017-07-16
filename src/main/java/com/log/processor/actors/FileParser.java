package com.log.processor.actors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.log.processor.events.EndOfFile;
import com.log.processor.events.Line;
import com.log.processor.events.StartOfFile;
import com.log.processor.messages.Parse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * FileParser actor parses file sends different events to an Aggregator actor,
 * depending on the parser state
 * @author majidali
 *
 */
public class FileParser extends AbstractLoggingActor {

	private final Path filePath;

	public FileParser(Path path) {
		this.filePath = path;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Parse.class, this::onParse)
				.matchAny(otherMessage -> log().info("received unknown message {}", otherMessage)).build();
	}

	private void onParse(Parse message) throws IOException {
		log().info("FileParser received parse message for file : {}", this.filePath);
		ActorRef aggregator = getContext().actorOf(Props.create(Aggregator.class, this.filePath), "aggregator");
		// subscribe actor for events
		context().system().eventStream().subscribe(aggregator, StartOfFile.class);
		context().system().eventStream().subscribe(aggregator, Line.class);
		context().system().eventStream().subscribe(aggregator, EndOfFile.class);

		startFileParsing();
	}

	private void startFileParsing() throws IOException {
		context().system().eventStream().publish(new StartOfFile(this.filePath));
		try (Stream<String> stream = Files.lines(filePath)) {
			stream.forEach(line -> context().system().eventStream().publish(new Line(this.filePath, line)));
		} catch (IOException e) {
			throw new IOException("Error: " + e.getMessage());
		}
		context().system().eventStream().publish(new EndOfFile(this.filePath));
	}

	public static Props props(Path path) {
		return Props.create(FileParser.class, path);
	}

}
