package ch.exense.commons.core.model.utils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.exense.commons.core.mongo.MongoClientSession;

public class EmbeddedMongoTestbench{

	private static EmbeddedMongo mongo = EmbeddedMongo.getInstance();
	protected static MongoClientSession session;
	
	public EmbeddedMongoTestbench() {
	}


	@BeforeClass
	public static void init(){
		try {
			// arbitrary port should be exposed through dev profile (i.e our dynamic external test inputs)
			int port = 27987;
			mongo.start("testing", "localhost", port);
			session = new MongoClientSession("localhost", 27017, null, null, port, "testing");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void before(){
	}
	
	@AfterClass
	public static void exit() {
		mongo.stop();
	}

	@Test
	public void checkTamperedLicense() throws Exception {
	}

	@Test
	public void checkOldschoolLicenseWorks() throws Exception {
		Assert.assertTrue(true);
	}
}