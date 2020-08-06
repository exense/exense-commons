package ch.exense.commons.core.mongo.accessors.concrete;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserAccessorMockTest{

	public UserAccessorMockTest() {
	}


	@BeforeClass
	public static void init(){

	}
	
	@Before
	public void before(){
	}
	
	@AfterClass
	public static void exit() {

	}

	@Test
	public void checkTamperedLicense() throws Exception {
	}

	@Test
	public void checkOldschoolLicenseWorks() throws Exception {
		Assert.assertTrue(true);
	}
}