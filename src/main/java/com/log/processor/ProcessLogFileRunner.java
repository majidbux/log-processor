package com.log.processor;

import com.log.processor.actors.FileScanner;
import com.log.processor.messages.Scan;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
/**
 * Application startup class
 * @author majidali
 *
 */
public class ProcessLogFileRunner {
    public static void main(String[] args) {
        String directoryToScan = System.getProperty("user.home");
    	if(args.length > 0){
        	directoryToScan = args[0];
        }
    	ActorSystem system = ActorSystem.create("process-log-files");
        final ActorRef fileScanner = system.actorOf(FileScanner.props(directoryToScan), "file-scanner");
        fileScanner.tell(new Scan(), ActorRef.noSender());
    }
}
