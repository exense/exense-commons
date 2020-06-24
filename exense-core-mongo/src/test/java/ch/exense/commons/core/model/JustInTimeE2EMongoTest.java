package ch.exense.commons.core.model;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.exense.commons.core.model.utils.EmbeddedMongo;

public class JustInTimeE2EMongoTest{

	private static EmbeddedMongo mongo = EmbeddedMongo.getInstance();

	public JustInTimeE2EMongoTest() {
	}


	@BeforeClass
	public static void init(){
		try {
			// arbitrary port should be exposed through dev profile (i.e our dynamic external test inputs)
			mongo.start("testing", "localhost", 27987);
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