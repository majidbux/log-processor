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

import com.log.processor.actors.Aggregator;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

public class AggregatorTest {

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
	public void testAggregatorsProps() {
		final Props props = Props.create(Aggregator.class, Paths.get("file1"));
		Assert.assertEquals(props.actorClass(), Aggregator.class);
	}


}
