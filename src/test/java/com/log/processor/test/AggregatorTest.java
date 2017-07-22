package com.log.processor.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.log.processor.actors.Aggregator;
import com.log.processor.events.Line;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;

public class AggregatorTest {

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
	public void testAggregatorsProps() {
		final Props props = Props.create(Aggregator.class, Paths.get("file1"));
		Assert.assertEquals(props.actorClass(), Aggregator.class);
	}
	
	@Test
	public void testFilePathIsSame() throws IOException {
		final Props props = Props.create(Aggregator.class, Paths.get("/testFile.text"));
		final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "aggregator-1");
		Assert.assertEquals(ref.underlyingActor().getFilePath(), Paths.get("/testFile.text"));
	}
	
	
	@Test
	public void testWordCountCalculation() throws IOException {
		final File tempFile = tempFolder.newFile("tempFile.txt");
		final Props props = Props.create(Aggregator.class, Paths.get(tempFile.getPath()));
		final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "aggregator-2");
		system.eventStream().publish(new Line(Paths.get(tempFile.getPath()), "hello world"), ref);
		Assert.assertEquals(ref.underlyingActor().getWordsCount(), 2);
		system.eventStream().publish(new Line(Paths.get(tempFile.getPath()), "how are you"), ref);
		Assert.assertEquals(ref.underlyingActor().getWordsCount(), 5);
	}

}
