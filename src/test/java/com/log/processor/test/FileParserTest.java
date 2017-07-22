package com.log.processor.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.log.processor.actors.FileParser;
import com.log.processor.messages.Parse;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;

public class FileParserTest {

	static ActorSystem system;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void setup() throws IOException {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testFileParserProps() {
		final Props props = Props.create(FileParser.class, Paths.get("file1"));
		Assert.assertEquals(props.actorClass(), FileParser.class);
	}
	
	@Test
	public void testUnRecognizedMessage() {
		final TestProbe probe = new TestProbe(system);
		final Props props = Props.create(FileParser.class, Paths.get("/testFile.text"));
		final ActorRef fileParser = system.actorOf(props, "file-parser-1");
		fileParser.tell("Hi", probe.ref());
		probe.expectMsg("Unrecognized message");
	}
	
	@Test
	public void testOnInvalidFile() {
		final Props props = Props.create(FileParser.class, Paths.get("/testFile.text"));
		final TestActorRef<FileParser> ref = TestActorRef.create(system, props, "file-parser-2");
		Parse parse = new Parse();
		try {
			ref.receive(parse, ActorRef.noSender());
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "Unable to read file " + Paths.get("/testFile.text"));
		}
	}
	
	@Test
	public void testFilePathIsSame() throws IOException {
		final Props props = Props.create(FileParser.class, Paths.get("/testFile.text"));
		final TestActorRef<FileParser> ref = TestActorRef.create(system, props, "file-parser-3");
		Assert.assertEquals(ref.underlyingActor().getFilePath(), Paths.get("/testFile.text"));
	}
	
	@Test
	public void testOnValidFile() throws IOException {
		final File tempFile = tempFolder.newFile("tempFile.txt");
		FileUtils.writeStringToFile(tempFile, "hello world");
		final TestProbe probe = new TestProbe(system);
		final Props props = Props.create(FileParser.class, Paths.get(tempFile.getPath()));
		final ActorRef FileParser = system.actorOf(props, "file-parser-4");
		FileParser.tell(new Parse(), ActorRef.noSender());
		probe.expectNoMsg();
	}
}
