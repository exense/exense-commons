package ch.exense.commons.core.model.utils;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.core.mongo.MongoClientSession;

public abstract class EmbeddedMongoTestbench{

	private static EmbeddedMongo mongo = EmbeddedMongo.getInstance();
	protected static MongoClientSession session;

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedMongoTestbench.class);

	protected static int port = 27987;
	
	@BeforeClass
	public static void init(){
		logger.info("initializing mongo backend & client");
		try {
			// arbitrary port should be exposed through dev profile (i.e our dynamic external test inputs)
			mongo.start("testing", "localhost", port);
			session = new MongoClientSession("localhost", port, null, null, 200, "testing");
			session.getMongoDatabase().drop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void exit() {
		logger.info("terminating mongo backend & client");
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mongo.stop();
	}

}