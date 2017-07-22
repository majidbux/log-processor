package com.log.processor.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.log.processor.actors.FileScanner;
import com.log.processor.messages.Scan;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;

public class FileScannerTest {

	static ActorSystem system;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void setup() throws IOException {
		final File tempFile = tempFolder.newFile("tempFile.txt");
		FileUtils.writeStringToFile(tempFile, "hello world");
		system = ActorSystem.create();
	}

	@AfterClass
	public static void teardown() {
		TestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testFileScannerProps() {
		final Props props = Props.create(FileScanner.class, "directoryToScan");
		Assert.assertEquals(props.actorClass(), FileScanner.class);
	}

	@Test
	public void testUnRecognizedMessage() {
		final TestProbe probe = new TestProbe(system);
		final Props props = Props.create(FileScanner.class, tempFolder.getRoot().getAbsolutePath());
		final ActorRef fileScanner = system.actorOf(props, "file-scanner-1");
		fileScanner.tell("Hi", probe.ref());
		probe.expectMsg("Unrecognized message");
	}

	@Test
	public void testDirectoryIsSame() {
		final Props props = Props.create(FileScanner.class, tempFolder.getRoot().getAbsolutePath());
		final TestActorRef<FileScanner> ref = TestActorRef.create(system, props, "file-scanner-2");
		Assert.assertEquals(ref.underlyingActor().getDirectoryAddress(),tempFolder.getRoot().getAbsolutePath());

	}
	
	@Test
	public void testOnInvalidDirectory() {
		final Props props = Props.create(FileScanner.class, "directoryToScan");
		final TestActorRef<FileScanner> ref = TestActorRef.create(system, props, "file-scanner-3");
		Scan scanMessage = new Scan();
		try {
			ref.receive(scanMessage, ActorRef.noSender());
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "Unable to read directory: directoryToScan");
		}
	}

	@Test
	public void testOnValidDirectory() {
		final TestProbe probe = new TestProbe(system);
		final Props props = Props.create(FileScanner.class, tempFolder.getRoot().getAbsolutePath());
		final ActorRef fileScanner = system.actorOf(props, "file-scanner-4");
		fileScanner.tell(new Scan(), ActorRef.noSender());
		probe.expectNoMsg();
	}

}
