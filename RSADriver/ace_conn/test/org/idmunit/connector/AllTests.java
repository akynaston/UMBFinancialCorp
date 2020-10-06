package org.idmunit.connector;

import java.util.HashMap;
import java.util.Map;

import com.trivir.ace.api.AceApi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static final String tokenSerialNum = "000157228469";
    public static final String groupName = "TestGroup1";
    public static final String profileName = "TESTPROFILE1";
    	/*
    	 * Profiles:
    	 * listed at: https://rsa-am81.example.com:7004/console-ims/ListRadiusProfiles.do?pageaction=nvPreSearchAll&ptoken=T70A2KUATMS9X4I6
    	 */
    // copied from Ace API AllTests.java:
    public static final String START_YEAR_61  = "1986";
    public static final String END_YEAR_61  = "1986";
    
    public static final String START_YEAR_52  = "2001";
    public static final String END_YEAR_52  = "2010";

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(AddObjectTests.class));
		suite.addTest(new TestSuite(DeleteObjectTests.class));
		suite.addTest(new TestSuite(ModifyTokenTests.class));
		suite.addTest(new TestSuite(ModifyUserTests.class));
		suite.addTest(new TestSuite(VerifyTokenTests.class));
		suite.addTest(new TestSuite(VerifyUserTests.class));
	    return suite; 
	}
	
	public static Map<String, String> getDefaultRSAConnConfig() {
		Map<String, String> initParams = new HashMap<String, String>();

		//System.getProperty("user.dir")
		initParams.put(AceApi.WEBLOGIC_LIB_DIR, "../jace/lib/rsa8_1");
		initParams.put(AceApi.RSA_KEYSTORE_FILE, "../jace/lib/rsa8_1/trust.jks");
		initParams.put(AceApi.SERVER, "t3s://172.17.2.104:7002");
		initParams.put(AceApi.COMMAND_CLIENT_USERNAME, "CmdClient_5dt475d8");		
		initParams.put(AceApi.COMMAND_CLIENT_PASSWORD, "IzMO8RTWBcS6vhjhlgeuPyg5fQThzT");
		initParams.put(AceApi.USERNAME, "idmuser");
		initParams.put(AceApi.PASSWORD, "Trivir5%");
		initParams.put(AceApi.RSA_REALM, "SystemDomain");
		initParams.put(AceApi.APPEND_SITE_TO_ALL_GROUPS, "false"); // not sure on value of this one
		initParams.put(AceApi.RSA_IDENTITY_SOURCE, "Internal Database"); // Gussing on this
		initParams.put(AceApi.API_VERSION, "81");
						
		return initParams;
	}
}
