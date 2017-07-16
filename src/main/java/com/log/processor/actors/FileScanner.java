package com.log.processor.actors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.log.processor.messages.Parse;
import com.log.processor.messages.Scan;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
/**
 * FileScanner actor will check if there is any file in predefined directory
 * sends a parse message to a FileParser actor in order to initiate the parsing
 * @author majidali
 *
 */
public class FileScanner extends AbstractLoggingActor {

	private final String directoryAddress;

	public FileScanner(String directoryAddress){
		this.directoryAddress = directoryAddress;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Scan.class, this::onScan)
							  .matchAny(otherMessage -> log().info("received unknown message {}", otherMessage)).build();
	
	}
	
	private void onScan(Scan message) throws IOException {
		log().info("FileScanner received scan message for log directory : {}", this.directoryAddress);
		try (Stream<Path> paths = Files.walk(Paths.get(this.directoryAddress))) {
			List<Path> allFiles = paths.filter(Files::isRegularFile).collect(Collectors.toList());
			initiateParsing(allFiles);
		}catch (IOException e) {
			throw new IOException("Unable to read directory: "+ e.getMessage());
		}
	}

	private void initiateParsing(List<Path> allFiles) {
		int fileCount = 1;
		for (Path path : allFiles) {
			log().info("File scanner initiating parsing for file {} ...", path);
			final ActorRef fileParser = getContext().actorOf(FileParser.props(path), "file-parser_" + fileCount);
			fileParser.tell(new Parse(), ActorRef.noSender());
			fileCount++;
		}
	}

	public static Props props(String directoryAddress) {
		return Props.create(FileScanner.class, directoryAddress);
	}
}
